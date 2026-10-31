# Prompt for continuing the NTM 1.12.2 port in a new chat

Ты продолжаешь порт HBM’s Nuclear Tech Mod на Forge 1.12.2.

Проект: `C:\Users\nagas\Desktop\port\ntm-port-1.12.2`

## Первое, что нужно сделать

1. Прочитай `porting/CURRENT.md`, `git status --short` и текущий `git diff` (не полагайся на старые отчёты: они гипотезы).
2. Учти, что `porting/CURRENT.md` **ещё не обновлён** этой сессией. Последние правки существуют только в рабочем дереве.
3. Проверь, что нет незавершённых Gradle-процессов/блокировок, и не трогай пользовательские изменения.

## Состояние на момент передачи

Проверка завершённых батчей выполнена адресно. Массовые миграции (PersistentRegistryRef по всем классам, EMPTY по всем контейнерам, metadata codecs, assets, sound fallback) заново **не делать**.

После последних правок:

- `gradlew.bat --offline compileJava --console=plain` — **exit 0, BUILD SUCCESSFUL**; лог: `porting/session-compile2.log`.
- `reobfJar` после последних правок **ещё не запускался**. JAR `build/libs/HBM-NTM-1.12.2-port.0.1.0.jar` (105 998 037 байт, 22:23:36) — **устаревший**, SHA-256 `8C1BAAF4F42007D0A4385201C9DA53E3A7A8DFD0BD21E7E2508724CF47CC4D19` относится к сборке до этих правок. Не выдавать его за итоговый.
- `git diff --check` после правок — **exit 0**.
- Runtime-проверки в этой сессии **не выполнялись**.

## Что уже подтверждено и исправлено в этой сессии

### 1. Legacy numeric raw-теги

Подтверждено: `PersistentRegistryRef` читает только registry name, numeric-only трактует как unresolved и сохраняет через `legacyOnly`/`preserveLegacy`. Исправлены найденные потери сопутствующих legacy-полей:

- `BlockRebar`: при unresolved block теперь сохраняются raw `block` + `meta`.
- `BlockICF`: сохраняются raw `block`, `meta`, `cX`, `cY`, `cZ`.
- `BlockPWR`: сохраняются raw `block`, `cX`, `cY`, `cZ`.
- `BlockBedrockOreTE`: при legacy numeric `0id` сохраняются исходные raw `size` и `meta` (раньше fallback `powder_iron` перезаписывал их на 1/0).
- `EntityRequestDrone`: исправлено чтение `dict`-программы — теперь `data.getString("dict")`, а не корневой `nbt` (иначе dict-программа терялась после save/load).

### 2. ItemStack.EMPTY и правила слотов

Подтверждено: нормализация `getStackInSlot`/`removeStackFromSlot`/`decrStackSize` в целом не меняет количества и sided rules. Найдены и исправлены реальные регрессии из-за проверок `!= null` на EMPTY:

- `ContainerArmorTable`: `onContainerClosed` больше не вызывает `ArmorModHandler.removeMod` для пустых upgrade-слотов. Раньше EMPTY проходил проверку `!= null`, и при закрытии GUI снимались все моды с брони. Добавлены проверки `isEmpty()` и защита пустой брони.
- `ArmorModHandler`: `applyMod`/`removeMod`/`clearMods`/`isApplicable` теперь отвергают empty stacks и не могут записать NBT в канонический `ItemStack.EMPTY`.
- `TileEntityDroneCrate`: `loadItems`/`unloadItems` больше не считают EMPTY грузом; не создают пустые записи в слотах и корректно завершают разгрузку.
- `MagicRecipes`: пустые слоты `InventoryCrafting` (`ItemStack.EMPTY`) больше не добавляются как `ComparableStack(ModItems.nothing)` — это ломало все magic-рецепты с пустыми ячейками.
- `ContainerAnvil`, `ContainerBook`, `ContainerLemegeton`, `ContainerWeaponTable`: close-пути не дёргают drop/uninstall для EMPTY.

### 3. QuantityField

- `TileEntityDiFurnace.QuantityField` отсутствует; количество только `getCount()`/`isEmpty()`, `BlastFurnaceRecipes.addRecipe` отвергает null/empty output.
- Удалён оставшийся `ItemInventory.QuantityField` (reflection в `ItemStack.stackSize`). `ItemInventory.quantity` теперь просто `stack.getCount()`; поведение для валидных стеков не менялось.

### 4. ScheduledMessageHandler / payload lifetime

- `NetworkHandler.registerMessage` действительно оборачивает все пакеты в `ScheduledMessageHandler`; сервер — `player.getServerWorld().addScheduledTask`, клиент — reflectively `ClientPacketScheduler`.
- Проверено: ни один зарегистрированный `onMessage` сейчас не возвращает не-null reply, поэтому потеря reply в текущем коде не воспроизводится. Это остаётся ограничением на будущее: если появится reply-handler, обёртка его не отправит.
- `BufPacket`/`EntityBufPacket` копируют payload в `fromBytes` и освобождают в `finally`, включая missing-world/missing-entity пути.
- `ModSoundEvents` fallback: `hbm:missing` создаётся через `create()` и попадает в `SOUNDS`, поэтому `registerSounds` его регистрирует. Reobf-bytecode `ModSoundEvents` не содержит вызова `getSoundName`; `resolve` использует registry lookup и `MISSING`.
- Ограничение: многие имена с существующими `sounds.json` (например `hbm:block.debris`, `hbm:weapon.tesla`) не имеют статически зарегистрированного `SoundEvent` и сейчас уходят в silent `MISSING`. Это не ошибка fallback, но отдельный незакрытый звуковой gate.

### 5. Metadata codecs

Адресно проверены `BlockMachineBase.FACING` и codecs: `FACING` 0..5 у MachineBattery/Boiler/CustomMachine/MachineFan/CMAnchor/FluidPump; packed `META` 0..15 у MassStorage; bool `ACTIVE/OPEN` у CableSwitch/Detector, FluidValve/Switch; `META` 0..9 Radiobox, 0..7 BlockSeal, `ACTIVE` BlockTNTBase; `CTContext` 0..15 у ICF/PWR. Диапазоны соответствуют значениям, которые пишут `onBlockPlacedBy`/toggle-пути; `NukeAntimatter`/`NukeBalefire` вызывают `super.onBlockPlacedBy`. Отдельного дефекта не найдено.

### 6. «Exited with code 1»

Эта строка не является результатом compile/reobf. Относящийся лог: `porting/dedicated-server-dev3.log` обрывается на `> Task :compileJava`; в Gradle daemon log `C:\Users\nagas\.gradle\daemon\4.10.3\daemon-3528.out.log` видно `client disconnection detected, canceling the build` / `Gradle build daemon ... stopped` для `--offline runServer`. То есть это прерванный runServer, а не провал сборки.

## Изменённые в этой сессии файлы

- `src/main/java/com/hbm/blocks/generic/BlockBedrockOreTE.java`
- `src/main/java/com/hbm/blocks/generic/BlockRebar.java`
- `src/main/java/com/hbm/blocks/machine/BlockICF.java`
- `src/main/java/com/hbm/blocks/machine/BlockPWR.java`
- `src/main/java/com/hbm/entity/item/EntityRequestDrone.java`
- `src/main/java/com/hbm/handler/ArmorModHandler.java`
- `src/main/java/com/hbm/inventory/container/ContainerAnvil.java`
- `src/main/java/com/hbm/inventory/container/ContainerArmorTable.java`
- `src/main/java/com/hbm/inventory/container/ContainerBook.java`
- `src/main/java/com/hbm/inventory/container/ContainerLemegeton.java`
- `src/main/java/com/hbm/inventory/container/ContainerWeaponTable.java`
- `src/main/java/com/hbm/inventory/recipes/MagicRecipes.java`
- `src/main/java/com/hbm/items/ItemInventory.java`
- `src/main/java/com/hbm/tileentity/network/TileEntityDroneCrate.java`

## Что осталось сделать в новом чате

### Обязательный build/verify gate

```powershell
cd C:\Users\nagas\Desktop\port\ntm-port-1.12.2
& .\gradlew.bat --offline reobfJar --console=plain *> porting\session-reobf-final.log
$LASTEXITCODE
git diff --check
```

Затем:

1. Проверить JAR `build/libs/HBM-NTM-1.12.2-port.0.1.0.jar`: zip integrity, полный SHA-256, размер, наличие актуальных классов (`ScheduledMessageHandler`, `ClientPacketScheduler`, `PersistentRegistryRef`, `BufPacket`, `EntityBufPacket`, `ModSoundEvents`, `TileEntityDiFurnace`, `BlastFurnaceRecipes`, `ArmorModHandler`, `TileEntityDroneCrate`).
2. Reobf spot-check через `javap`: в `ModSoundEvents` нет `getSoundName`; `ScheduledMessageHandler` вызывает `WorldServer.func_152344_a`/reflection; `BufPacket` использует `ByteBuf.copy/release`; `ArmorModHandler`/`ContainerArmorTable` не содержат dev-name ошибок.
3. Убедиться, что после reobf SHA изменился и записать новый полный SHA-256.

### Runtime gates (отдельная тестовая среда, не основной launcher instance)

- Dedicated server: `runServer` (в `run/`, eula=true) после последних правок; проверить, что нет `EntityPlayerSP`/client-class загрузки на SERVER, `NoSuchMethodError`, packet-thread исключений.
- Client cold start в отдельной копии инстанса: смотреть `latest.log` на model/texture/sound errors, 9 fallback-маршрутов, `hbm:missing` warnings; F3+T.
- Inventory runtime: открыть/закрыть Armor Table с установленными модами (убедиться, что моды не снимаются), Drone Crate (пустой дрон не застревает), Magic Recipes (3–4 компонента), shift-click/empty cursor/crate/RBMK.
- Legacy NBT: загрузить/сохранить объект с numeric-only `block`/`item`/`0id`, убедиться, что raw-теги остались после повторного save/load; проверить `BlockRebar/ICF/PWR/BedrockOre`.

### Затем

Обновить `porting/CURRENT.md`: подтверждённая причина и исправленный механизм, изменённые файлы, выполненные проверки и результаты, оставшиеся проблемы/ограничения, путь и SHA-256 итогового JAR. После этого остановиться.

## Ограничения

- Не выполнять commit/push/reset/checkout/restore.
- Сохранять существующие пользовательские изменения, кодировки и CRLF в Java.
- Не менять gameplay/registry names (`hbm:tile.*`, `hbm:item.*`), не добавлять dummy assets/fallback, не исключать классы из сборки.
- Исправлять только подтверждённые общие механизмы минимальными связанными изменениями.
- Рабочие миры и основной launcher instance не изменять.
- Не объявлять успех/провал сборки по строке «Exited with code 1» без соответствующего лога.
- Субагенты разрешены без повторного согласования, только `model="chatgpt-web/gpt-6-sol"`, `reasoning_effort="high"`; если агент не читает checkout — передавать фрагменты исходников и не выдавать его анализ за локальную проверку.
