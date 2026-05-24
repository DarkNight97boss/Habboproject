# EMU source — decompiled, made compilable, key files restored from upstream

`src/main/java` is the **Arcturus Morningstar 3.5.3** source recovered from
`Habbo-3.5.3.jar` with **Vineflower**, then made to compile. The security audit
found this build is vanilla Arcturus (only cosmetic "Zabbo MS" branding) — no backdoors.

## Build
```
cd EMU
mvn -B package        # -> target/Habbo-3.5.3-jar-with-dependencies.jar
```
Requires JDK 8+ and Maven (the pom targets Java 8). Verified: `mvn package` = **BUILD SUCCESS**.

## How the source was produced
1. Decompiled `Habbo-3.5.3.jar` with Vineflower (much more recompilable than jadx).
2. The decompiler still mangled ~26 spots; the worst was `WiredHandler` (a DB method
   where the probability logic `randomNumber = nextInt(101)` was lost).
3. **The 15 files that needed semantic fixes were then REPLACED with the authentic
   upstream Arcturus 3.5.3 source** (git tag `3-5-3` from
   https://git.krews.org/morningstar/Arcturus-Community), which builds 100% cleanly.
   So `WiredHandler` and the other touched files are now the real upstream code, not guesses.

The remaining files are Vineflower-decompiled (readable, compile cleanly). For a
fully-authentic, commented, maintainable tree, build directly from the upstream repo above.
