# Radar power callback pair

Baseline: CP80 a0dc7d14f453a1d6f01b0d5abdcd455358bf2714.
Exactly two production owners, MachineRadar and MachineRadarLarge; nine guarded source
fragments. Three callbacks each: capability, weak output, strong forwarding to weak.
Additionally, obsolete scalar IBlockAccess tile/state reads inside these callbacks become
native BlockPos reads. No visual, geometry, collision, model, packet, scan, notification,
GUI or tile production implementation changes.

The original large-radar callback reads metadata from the access, returns zero below 6,
then reads the neighbor in ForgeDirection.getOrientation(m), without reversing the side.
This is retained using EnumFacing.getIndex. Both original and native World pass the query
side unchanged; their neighbor scans use DOWN/UP/NORTH/SOUTH/WEST/EAST identically. Forge1614
World's shouldCheckWeakPower patch also preserves this side unchanged. Original integer
coordinate additions are retained, including overflow behavior. The small radar does not
inspect the side; no unnecessary side dereference was introduced there. Strong calls weak
virtually, retaining native subclass dispatch. Native side domain is six EnumFacing values;
invalid old integer directions and null native side are not asserted as engine behavior.

Closure: neither radar owner has descendants; BlockDummyable has no old or native power
bridge; no project caller invokes either old weak/strong method except the selected strong
forwarders. PowerDetector and BlockModDoor's old capability caller are separate remaining
roots and were intentionally excluded. Existing radio-torch native callbacks are untouched.

Run:

    python run.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt
    python audit-engine.py

Native run: 57,858 assertions, 11,520 parameterized cases plus 240 exact tile-formula
checks. Covers all metadata values, all six directions, weak/strong, missing/wrong/subclass
tiles, values -1/0/1/7/15/16, lookup exceptions, exact read order and number, native
World.getRedstonePower/getStrongPower, and native isBlockPowered neighbor orientation.
Four mutants reject opposite facing, metadata 6 exclusion, lost strong output, and x/z
substitution. Native compilation of the original methods fails at exactly six overrides
and three obsolete scalar IBlockAccess calls, matching nine exact CP80 diagnostics.

Test honesty: native Block/IBlockState/World/EnumFacing are actual engine classes, not
replacement platform classes. Owner shells retain the original nonopaque/nonfull shape
predicates; complete real owner classes still have unrelated port errors. Ordinary domain
tile fixtures supply values to exhaust the boundary. An additional exact production
TileEntityMachineRadarNT.getRedPower method is compiled unchanged on a native TileEntity
subclass and exercised through real callback dispatch: empty/filter/max-tier, proximity,
range and rounding cases. Its entry records and configurable range are bounded fixtures;
full scan/configuration/tick or gameplay execution is not claimed. The old exact methods
have only the IBlockAccess parameter type rebound to a named test LegacyAccess interface,
implemented by a separate oracle adapter. No fake old overloads exist on NativeWorld or
any production class. Actual production ForgeDirection and WorldBlockState compile
unchanged in the harness.

baseline.zip freezes both files. selection.json and source-proof.json record byte-exact
CP80 inverse. For composition, --allow-unrelated still requires exact original callback
methods after inverse, but reports full-file inverse false if independent edits exist.
apply-fragments.py preflights every fragment and preserves unrelated bytes.
The original/native engine bytecode, original ForgeDirection, Forge1614 power patches,
MCP mappings and input hashes are retained for the orientation audit. The full project
compile remains the coordinator's separate validation boundary.
