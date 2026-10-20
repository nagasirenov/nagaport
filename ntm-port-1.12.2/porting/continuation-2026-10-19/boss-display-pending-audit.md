# Pending native boss HUD policy, 2026-10-19

Read-only original/native API audit; no implementation or approval. Parent is parking the question while the user is unavailable because boss polish is low priority.

Original BossStatus has one last-rendered global snapshot, immediate health/max and formatted name, and a 100-successful-HUD-draw countdown (not game ticks). Six active NTM callers: RenderBFA/UFO/Quacc use false darkening; RenderHunterChopper/WormHead/MaskMan use true. RenderSiegeCraft's call is commented out and must remain inactive. Seven original NTM boss entities also participate in RenderOverhead thermal classification.

The original boolean pulses boss atmosphere independently of HUD lifetime. Each unpaused renderer update consumes/resets it, increasing darkening 0.05 or decaying 0.0125. Native EntityRenderer retains these constants. Native BossInfoClient UPDATE_PCT interpolates for 100 ms, so an exact snapshot implementation would locally ADD/replace a single NTM-owned UUID, not update percent. Public GuiBossOverlay.read(SPacketUpdateBossInfo) allows this without transmitting packets/reflection/server trackers. Lifetime should count successful BOSSHEALTH draws and respect canceled draws; world/disconnect cleanup removes only owned state. Native fog/music must remain false.

Pending recommendation: native additive coexistence (one last-rendered NTM bar alongside vanilla/other mods' bars) with native artwork. This differs from original global arbitration and is NOT approved. Do not cancel unrelated BOSSHEALTH or clearBossInfos. Native server packets do not identify entity render order, so generic exact old arbitration cannot be inferred safely.

Do not replace IBossDisplayData with isNonBoss()=false overrides merely for compilation. Native portals and Forge setdimension consult isNonBoss(), while original portal collision did not consult the display marker. That would change travel mechanics. A narrow NTM display marker plus explicit vanilla dragon/wither classification is the independent semantic route.

Evidence: scoped pristine NTM source; official original client SHA256 a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce with SRG names; actual native Forge 2768 classes and GuiIngameForge source. Not a live client test. Continue other declaration roots until the policy is resolved.
