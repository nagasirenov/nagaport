#!/usr/bin/env python3
"""Pinned native MC 1.12.2/Forge bytecode proof; no game launch or replacement classes."""
import hashlib
import os
from pathlib import Path
import re
import subprocess
import sys
import zipfile

HERE = Path(__file__).resolve().parent
CP_FILE = Path(os.environ.get('NATIVE_CLASSPATH_FILE', '/tmp/nagaport-ae2-native-classpath.txt'))
JAVAP = os.environ.get('JAVAP', '/workspace/shared/nagaport-toolchain/java8/bin/javap')
CP = CP_FILE.read_text().strip()
CLASSES = {
    'Minecraft': 'net.minecraft.client.Minecraft',
    'PlayerControllerMP': 'net.minecraft.client.multiplayer.PlayerControllerMP',
    'PlayerInteractionManager': 'net.minecraft.server.management.PlayerInteractionManager',
    'NetHandlerPlayServer': 'net.minecraft.network.NetHandlerPlayServer',
    'EnumHand': 'net.minecraft.util.EnumHand',
}

def require(condition, message):
    if not condition:
        raise AssertionError(message)

# Resolve every class using the same classpath and require one, genuine native jar.
# Do not load/initialize Minecraft, compile stubs, rewrite bytecode, or launch a client.
locations = {name: [] for name in CLASSES}
resources = {name: fqcn.replace('.', '/') + '.class' for name, fqcn in CLASSES.items()}
for entry in CP.split(os.pathsep):
    p = Path(entry)
    if p.is_file() and zipfile.is_zipfile(p):
        with zipfile.ZipFile(p) as z:
            names = set(z.namelist())
            for short, resource in resources.items():
                if resource in names:
                    locations[short].append((str(p), hashlib.sha256(z.read(resource)).hexdigest()))
    elif p.is_dir():
        for short, resource in resources.items():
            candidate = p / resource
            if candidate.is_file():
                locations[short].append((str(candidate), hashlib.sha256(candidate.read_bytes()).hexdigest()))

print('NATIVE BYTECODE DISPATCH PROOF')
print('classpath:', CP_FILE)
print('javap:', JAVAP)
texts = {}
for short, fqcn in CLASSES.items():
    require(len(locations[short]) == 1, f'{fqcn}: shadowed/missing native class: {locations[short]}')
    source, digest = locations[short][0]
    require('forgeBin-1.12.2-14.23.5.2768-' in source and source.endswith('.jar'), f'Not expected native jar: {source}')
    print(f'{fqcn}\n  source: {source}\n  class sha256: {digest}')
    text = subprocess.check_output([JAVAP, '-classpath', CP, '-p', '-c', fqcn], text=True)
    texts[short] = text
    # Complete javap is inspected in memory; checked method excerpts are persisted below.

class Method:
    def __init__(self, short, name):
        text = texts[short]
        headers = list(re.finditer(r'^  (?:public |protected |private |static )[^\n]*', text, re.M))
        selected = [i for i, m in enumerate(headers) if name in m.group()]
        require(len(selected) == 1, f'{short}.{name}: missing or overloaded method')
        i = selected[0]
        self.name = short + '.' + name
        self.text = text[headers[i].start():headers[i + 1].start() if i + 1 < len(headers) else len(text)]
        self.ins = {}
        for match in re.finditer(r'^[ \t]+(\d+):[ \t]+([a-z][a-z0-9_]*)[ \t]*(.*?)[ \t]*$', self.text, re.M):
            self.ins[int(match[1])] = (match[2], re.sub(r'\s+', ' ', match[3]).strip())
        self.offsets = list(self.ins)
        self.next = dict(zip(self.offsets, self.offsets[1:]))
        (HERE / (short + '.' + ('static_initializer' if name == 'static {};' else name.replace('(', '')) + '.txt')).write_text(self.text.rstrip() + '\n')

    def at(self, offset, op, contains=None):
        actual = self.ins.get(offset)
        require(actual is not None and actual[0] == op and (contains is None or contains in actual[1]),
                f'{self.name}@{offset}: expected {op} {contains}; got {actual}')

    def count(self, needle):
        return sum(needle in arg for op, arg in self.ins.values() if op.startswith('invoke'))

    def no_back_edges(self):
        for at, (op, arg) in self.ins.items():
            require(op not in ('tableswitch', 'lookupswitch', 'jsr', 'jsr_w', 'ret'), f'{self.name}: unsupported CFG opcode')
            if op.startswith('if') or op in ('goto', 'goto_w'):
                require(int(arg.split()[0]) > at, f'{self.name}: backward/repeat edge @{at}')
        require('Exception table:' not in self.text, f'{self.name}: exceptional CFG unmodeled')

    def path(self, start, choices):
        """Walk a pinned branch path; every conditional encountered needs an explicit choice."""
        path, at = [], start
        while True:
            require(at not in path, f'{self.name}: repeating path @{at}')
            path.append(at)
            op, arg = self.ins[at]
            if op.endswith('return') or op == 'athrow':
                return path
            if op.startswith('if'):
                require(at in choices, f'{self.name}: missing branch premise @{at}')
                at = int(arg.split()[0]) if choices[at] else self.next[at]
            elif op in ('goto', 'goto_w'):
                at = int(arg.split()[0])
            else:
                at = self.next[at]

    def reachable(self, start):
        pending, seen = [start], set()
        while pending:
            at = pending.pop()
            if at in seen:
                continue
            seen.add(at)
            op, arg = self.ins[at]
            if op.endswith('return') or op == 'athrow':
                continue
            require(op not in ('tableswitch', 'lookupswitch'), f'{self.name}: switch in requested subgraph')
            if op.startswith('if'):
                pending += [int(arg.split()[0]), self.next[at]]
            elif op in ('goto', 'goto_w'):
                pending.append(int(arg.split()[0]))
            else:
                pending.append(self.next[at])
        return seen

hands = Method('EnumHand', 'static {};')
hands.at(31, 'iconst_0'); hands.at(32, 'getstatic', 'Field MAIN_HAND:'); hands.at(35, 'aastore')
hands.at(37, 'iconst_1'); hands.at(38, 'getstatic', 'Field OFF_HAND:'); hands.at(41, 'aastore')
hands.at(42, 'putstatic', 'Field $VALUES:')
values = Method('EnumHand', 'values(')
values.at(0, 'getstatic', 'Field $VALUES:'); values.at(3, 'invokevirtual', '.clone:'); values.at(9, 'areturn')
print('PASS 1: EnumHand.values() clones [MAIN_HAND, OFF_HAND], in that order.')

mc = Method('Minecraft', 'rightClickMouse(')
mc.at(43, 'invokestatic', 'EnumHand.values:'); mc.at(50, 'iconst_0'); mc.at(51, 'istore_3')
mc.at(54, 'if_icmpge', '394'); mc.at(57, 'aload_1'); mc.at(58, 'iload_3'); mc.at(59, 'aaload'); mc.at(60, 'astore', '4')
mc.at(242, 'aload', '4'); mc.at(244, 'invokevirtual', 'PlayerControllerMP.processRightClickBlock:')
mc.at(247, 'astore', '8'); mc.at(249, 'aload', '8'); mc.at(251, 'getstatic', 'EnumActionResult.SUCCESS:'); mc.at(254, 'if_acmpne', '307')
mc.at(306, 'return'); mc.at(388, 'iinc', '3, 1'); mc.at(391, 'goto', '52')
reachable = mc.reachable(257)  # SUCCESS makes if_acmpne@254 fall through.
terminals = {at for at in reachable if mc.ins[at][0].endswith('return')}
require(terminals == {306} and 388 not in reachable and 244 not in reachable, 'SUCCESS reaches next hand or another dispatch')
require(all(at <= 306 for at in reachable), 'SUCCESS escapes terminal subgraph')
require(mc.count('PlayerControllerMP.processRightClickBlock:') == 1, 'Unexpected client block dispatch call sites')
print('PASS 2: Minecraft visits hands by ascending array index; block SUCCESS reaches return@306 on every branch, before hand increment@388.')

client = Method('PlayerControllerMP', 'processRightClickBlock(')
client.no_back_edges()
require(client.count('Block.onBlockActivated:') == 1, 'Client activation native call-site count not one')
client.at(306, 'invokevirtual', 'Block.onBlockActivated:'); client.at(309, 'istore', '11')
client.at(311, 'iload', '11'); client.at(313, 'ifeq', '321'); client.at(316, 'getstatic', 'EnumActionResult.SUCCESS:'); client.at(319, 'astore', '13')
client.at(321, 'iload', '11'); client.at(323, 'ifne', '366'); client.at(377, 'aload', '6')
client.at(385, 'invokespecial', 'CPacketPlayerTryUseItemOnBlock.'); client.at(388, 'invokevirtual', 'NetHandlerPlayClient.sendPacket:')
client.at(391, 'iload', '11'); client.at(393, 'ifne', '406')
client.at(408, 'invokevirtual', 'RightClickBlock.getUseItem:'); client.at(411, 'getstatic', 'Event$Result.ALLOW:'); client.at(414, 'if_acmpne', '631')
client.at(631, 'getstatic', 'EnumActionResult.SUCCESS:'); client.at(634, 'areturn')
path = client.path(309, {313: False, 323: True, 393: True, 414: True})
require(path[-1] == 634 and 388 in path and 419 not in path, 'Ordinary true-activation path not SUCCESS/send-once/skip-empty-check')
require(sum('sendPacket:' in client.ins[at][1] for at in path) == 1, 'Client success path does not send once')
print('PASS 3: Once client activation returns true and useItem != ALLOW, native controller sends one block packet and returns SUCCESS; it skips ItemStack.isEmpty.')
client.at(419, 'invokevirtual', 'ItemStack.isEmpty:'); client.at(422, 'ifeq', '429'); client.at(425, 'getstatic', 'EnumActionResult.PASS:'); client.at(428, 'areturn')
path = client.path(309, {313: False, 323: True, 393: True, 414: False, 422: False})
require(path[-1] == 428, 'Forced ALLOW + empty stack exception changed')
print('PASS 4: Exception preserved: useItem == ALLOW forces item handling; empty stack then returns PASS even after client activation=true.')

server = Method('PlayerInteractionManager', 'processRightClickBlock(')
server.no_back_edges()
require(server.count('Block.onBlockActivated:') == 1, 'Server activation native call-site count not one')
server.at(313, 'invokevirtual', 'Block.onBlockActivated:'); server.at(316, 'ifeq', '324'); server.at(319, 'getstatic', 'EnumActionResult.SUCCESS:'); server.at(322, 'astore', '13')
server.at(324, 'aload_3'); server.at(325, 'invokevirtual', 'ItemStack.isEmpty:'); server.at(328, 'ifeq', '335'); server.at(331, 'getstatic', 'EnumActionResult.PASS:'); server.at(334, 'areturn')
for activated in (False, True):
    path = server.path(316, {316: not activated, 328: False})
    require(path[-1] == 334, 'Server empty-stack path no longer PASS')
print('PASS 5: Server activation is before empty-stack test; empty stack yields PASS for both callback booleans, after the eligible callback has already run once.')
server.at(343, 'invokevirtual', 'CooldownTracker.hasCooldown:'); server.at(346, 'ifeq', '353'); server.at(349, 'getstatic', 'EnumActionResult.PASS:'); server.at(352, 'areturn')
server.at(360, 'ifeq', '402'); server.at(406, 'ifeq', '497')
server.at(499, 'getstatic', 'EnumActionResult.SUCCESS:'); server.at(502, 'if_acmpeq', '516')
server.at(518, 'getstatic', 'EnumActionResult.SUCCESS:'); server.at(521, 'if_acmpne', '576')
server.at(526, 'invokevirtual', 'RightClickBlock.getUseItem:'); server.at(529, 'getstatic', 'Event$Result.ALLOW:'); server.at(532, 'if_acmpne', '576')
server.at(576, 'aload', '13'); server.at(578, 'areturn')
path = server.path(316, {316: False, 328: True, 346: True, 360: True, 406: True, 502: True, 521: False, 532: True})
require(path[-1] == 578 and 322 in path, 'Server ordinary nonempty survival activation=true no longer retains SUCCESS')
require(not any('ItemStack.onItemUse:' in server.ins[at][1] for at in path), 'Server normal true-activation unexpectedly uses item')
print('PASS 5b: Nonempty, no-cooldown, ordinary-item survival path retains callback SUCCESS when useItem != ALLOW; empty/cooldown PASS and item overrides prevent a universal true => SUCCESS claim.')
print('PASS 6: Each native client/server block-controller method has one onBlockActivated site and no backward CFG edges: at most one direct activation per invocation, zero when gated/skipped.')

network = Method('NetHandlerPlayServer', 'processTryUseItemOnBlock(')
network.no_back_edges()
require(network.count('PlayerInteractionManager.processRightClickBlock:') == 1, 'Unexpected packet dispatch count')
network.at(28, 'invokevirtual', 'CPacketPlayerTryUseItemOnBlock.getHand:'); network.at(31, 'astore_3'); network.at(222, 'aload_3')
network.at(239, 'invokevirtual', 'PlayerInteractionManager.processRightClickBlock:'); network.at(242, 'pop')
require(network.count('EnumHand.values:') == 0, 'Packet handler gained hand enumeration')
print('PASS 7: Server packet handler uses packet hand, calls interaction manager at most once, discards its return value with pop, and does not enumerate/retry the other hand.')
print('ALL 7 NATIVE BYTECODE CLAIM GROUPS PASS')
print('Boundary: static native-bytecode/control-flow verification only; no client/server launch, no live GUI count, no arbitrary-event/coremod/re-entrant/multiple-packet guarantee.')
