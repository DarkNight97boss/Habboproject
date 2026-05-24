# EMU source — decompiled & patched to compile

`src/main/java` is the **Arcturus Morningstar 3.5.3** source recovered from
`Habbo-3.5.3.jar` with **Vineflower**, then hand-patched (~26 small fixes) so it
compiles cleanly. The security audit found this build is vanilla Arcturus
(only cosmetic "Zabbo MS" branding) — no backdoors.

## Build
```
cd EMU
mvn -B package        # -> target/Habbo-3.5.3-jar-with-dependencies.jar
```
Requires JDK 8+ and Maven (the pom targets Java 8). Verified: `mvn package` = BUILD SUCCESS.

## Patches applied (decompiler artifacts)
Vineflower lost some type info; the fixes were mechanical except where noted:
- Re-added generics on raw casts (AchievementManager, PacketManager,
  RequestDeleteRoomEvent, PetBreedingResultComposer).
- Fixed catch-variable names (EffectsComponent, YoutubeManager).
- `PreparedStatement` cast in CleanerThread; raw `TObjectHashIterator` in PluginManager.
- `boolean`/`int` fixes (CatalogBuyItemAsGiftEvent), final capture in a lambda
  (WiredEffectChangeFurniDirection), `invalidTriggersx` typo in 4 wired effects.
- **WiredHandler.java** had a heavily-mangled DB method: the 5 `return (boolean)randomNumber`
  were replaced with `return randomNumber != 0;` to compile. **Review this method
  against upstream if wired conditions misbehave at runtime.**

## For a guaranteed-clean source
Use the official upstream and build it: https://git.krews.org/morningstar/Arcturus-Community
