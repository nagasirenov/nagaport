# Проверка изменений этапа core/loot/NBT

Область: текущий diff относительно dff345a. Сопоставлены pristine, production-код, диагностические тесты и точный mapped Forge API. Проверка исходников выполнялась отдельно от запуска Gradle.

Исправленные находки:

- Порядок инициализации: registry names предметов ещё не доступны в preInit. Handler сохраняет Item/weight, а LootEntry создаёт только в LootTableLoadEvent.
- P2: count0 у прототипа из JSON игнорировался неверно. Теперь нормализуется копия, raw item/metadata/NBT сохраняются, исходный объект и EMPTY не мутируются. Native regression проверяет RNG3/5/2, включая unstackable и отсутствие предмета.
- P2: добавление no9 в bonus chest main вытесняло гарантированный топор. Теперь только bonus chest использует pool3. Регрессия читает настоящий target JSON через Forge parser и проверяет все четыре пула.

После исправлений независимая повторная проверка не нашла дополнительных конкретных дефектов в этих изменениях. По 28 NBT hooks, ID компонентов и ProceduralStructureStart также не найдено дополнительных дефектов; сохранён payload и проверены целевые signatures/dispatch.

Границы: полные классы компонентов и потребителей лута не скомпилированы из-за оставшихся общих ошибок. Minecraft API harness вызывает обработчик напрямую; FML-transformed event bus и lifecycle полного мода не тестировались. Нет проверки клиента, сервера или сохранений мира. Финальный отдельный импорт Side проверен по исходнику и повторной полной clean compile; класс рендера не получил самостоятельный PASS.

Канонические результаты: linux-final-core-loot-compileJava-2026-10-19.log (2291 errors; exit1), linux-core-target-probe-final-2026-10-19.log (isolated compile/tests; exit0), structure-nbt-parity-2026-10-19.log, weighted-loot-source-parity-2026-10-19.log.
