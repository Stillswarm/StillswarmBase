# Room support

Optional artifact: `com.stillswarm.base:foundation-storage-room:0.1.0-SNAPSHOT`.
This module is independent of `foundation-storage` (Preferences DataStore).
Neither preferences, config, nor app-shell pulls Room in automatically.

## Shared writes

Extend `BaseDao<MyEntity>` from an app-owned `@Dao` interface. Room generates the
inherited methods when the concrete app DAO is processed by KSP:

- `insert` / `insertAll`: return SQLite row IDs. Conflicts abort; they never silently replace data.
- `update` / `updateAll`: match primary keys and return affected-row counts. Missing rows are not inserted.
- `delete` / `deleteAll`: delete only the supplied entities and return affected-row counts.
- `upsert` / `upsertAll`: explicitly insert missing primary keys or update existing ones.

Every list method is transactional. Exceptions and coroutine cancellation propagate.
Returned insert row IDs are not necessarily your entity's primary keys, particularly
when those keys are strings. Upsert is distinct from SQLite INSERT OR REPLACE.

Queries stay app-owned: declare `getById`, `getAll`, `observeById`, and `observeAll`
with concrete `@Query` SQL as needed. `get` is one-shot/suspend; `observe` returns Flow.
Choose filtering, ordering, pagination, and deletion policy per feature. There is
no generated generic getAll query, database singleton, or universal repository.
An entity with restricted write permissions need not extend BaseDao at all.

## App setup

1. Add `implementation(project(":libraries:storage-room"))`, or its Maven coordinate.
2. Apply `com.google.devtools.ksp` and `androidx.room` in the module declaring the database.
3. Add `ksp(libs.androidx.room.compiler)` and configure `room { schemaDirectory("$projectDir/schemas") }`.
4. Define entities, concrete DAOs, and a `@Database`; keep its exported schemas in version control.
5. Create one database per file with application context and inject DAOs into app repositories.

Runtime/compiler/plugin versions must match. This repository uses Room 2.8.4 and
KSP2 2.3.12. The foundation module needs no processor because it owns no database.

Apps own migrations. Increment the database version and provide/test migrations
when changing schemas. Do not enable destructive migration fallback by default.
Keep disk access off the main thread using suspend queries and Flow. For operations
spanning multiple DAO calls, use an app-owned @Transaction method or withTransaction.
Backup policy, encryption, and account separation are also app decisions.

## Working example and tests

The demo app's `room` package contains a note entity, DAO, version-1 database,
repository, ViewModel, and screen. Open **Try Room storage** in the component gallery.
Add text, update a note using the input, and delete after confirmation. Notes survive
relaunch. Read queries specify stable ordering; app navigation owns the screen.

`BaseDaoTest` runs against generated Room code on Android, testing CRUD, conflict
handling, batch rollback, upserts, Flow invalidation, and file persistence.
There is no Room fake: Room's in-memory database executes real SQLite for DAO tests.
