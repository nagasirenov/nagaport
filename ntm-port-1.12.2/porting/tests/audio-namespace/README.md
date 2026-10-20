# Native audio namespace resource evidence

This folder is suitable for `ntm-port-1.12.2/porting/tests/audio-namespace`.
It contains no generated JAR, classes, or duplicate OGG tree.

Run against the actual migrated module:

    python probe-audio-namespaces.py --module <ntm-port-1.12.2> --java-home <JDK8> --classpath <native-1.12.2-classpath>

The pristine original module defaults to the sibling `ntm-original-pristine`; override with `--original <path>` when needed. The dependency classpath uses the host operating system's path separator. No dependency is downloaded.

The runner checks exact manifested migrated JSON, reverses only the 456 token edits to the CP37 baseline SHA-256, verifies all other JSON fields and representation unchanged, and checks all 593 current assets against both manifest SHA-256 and pristine original asset bytes. It verifies all exact filename spellings and rejects case-folded collisions, including on Windows.

It then builds temporary folder/JAR resources from that migrated module, compiles the actual production SoundCategoryResolver with the existing native Forge launcher and this native test, and runs genuine SoundHandler parsing/loading against all 408 events / 571 sample references. It asserts native ResourceLocation normalization, categories, file type, sample order/count, streaming, weight, volume, pitch, headers, accessor weight and actual/missing selected sample. Temporary files are removed automatically.

The three original unresolved FM samples remain unresolved: `fm.clap`, `fm.mug`, `fm.sample`. Their native warnings are expected. All previously qualified samples are unchanged. No event-type sample entry is migrated, and the event ID collision checks cover every event.

The original isolated preparation additionally fully decoded all 564 distinct referenced original HBM files with the installed FFmpeg, with zero errors/warnings. This portable runner verifies the same asset hashes but does not require FFmpeg or repeat full decoding.

No OpenAL, audible playback, full-mod build or launched gameplay claim is made. Use the checkpoint's safe resource migration helper for application, including Windows case-only renames. The historical isolated `apply.py` is an internal Linux staging utility, not the supported user migration workflow.
