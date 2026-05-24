# Decompiled source — Arcturus Morningstar 3.5.3 (Zabbo MS build)

Decompiled with **jadx** from `../Habbo-3.5.3.jar`, for **reading / security auditing** only.

## Important
- This is GPL-v3 source (Arcturus Morningstar) recovered from the compiled jar.
- **It does NOT recompile as-is.** A `javac` run produced 100+ errors that are
  typical jadx artifacts: generics type-erasure loss (`Object cannot be converted to X`),
  nested-type forward references in `extends` clauses (e.g. `messages/rcon/*`),
  and duplicate local variables. Fixing them by hand is possible but not worth it.
- To actually **build** the emulator, use the official upstream source instead:
  https://git.krews.org/morningstar/Arcturus-Community  (then `mvn package`).
- The runnable artifacts are the prebuilt jars in the parent folder
  (`../Habbo-3.5.3.jar` and `../Habbo-3.5.3-jar-with-dependencies.jar`).

## Layout
- `sources/` — decompiled `.java` (1801 files; package root `com.eu.habbo`)
- `resources/` — non-class resources extracted from the jar
