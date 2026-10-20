# Horizontal door icon value view

Run `python3 porting/tests/door-icon-native/run.py --java-home JAVA8 --classpath NATIVE_CLASSPATH --legacy-client ORIGINAL_CLIENT`. No download; original client hash is checked and the game JAR is not distributed. Windows is untested.

The sole two IconFlipped constructions in the scoped original source are BlockModDoor upper/lower registration, both horizontal=true, vertical=false. Replace this removed engine dependency with the small NTM-owned HorizontallyFlippedIcon value view over the existing NTM IIcon contract. It owns no renderer, state machine, registry or platform API. Original name/dimensions and live delegate coordinates are retained. The interpolation deliberately subtracts float endpoints and casts the coordinate before division/multiplication, matching original operation order rather than delegating to a potentially different native interpolation.

2,189 checks compare the actual unchanged original `re`/`rf` bytecode through an isolated loader with native TextureAtlasSprite and the real existing LegacyTextureAtlasSprite adapter. Both two-icon registration statements are extracted unchanged into an explicit NTM binding fixture and executed. Tests include changing atlas bounds, reversed ranges, negative/wrapping coordinates, signed zero, infinities and NaN. Only the original horizontal=true/vertical=false domain is implemented; no unneeded vertical-flip API is introduced or original unused vertical quirk changed.

The complete BlockModDoor lifecycle and its actual native models/registration remain pending. This is a small declaration dependency correction, not a claim that the door is playable, visible or server/save safe. No recipe, drop, placement, redstone, sound or material change.
