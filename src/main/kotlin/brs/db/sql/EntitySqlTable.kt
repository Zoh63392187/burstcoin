package brs.db.sql

import brs.db.BurstKey
import brs.db.EntityTable
import brs.db.getUsingDslContext
import brs.db.useDslContext
import brs.entity.DependencyProvider
import brs.util.db.fetchAndMap
import org.jooq.*
import org.jooq.impl.DSL
import org.jooq.impl.TableImpl

internal abstract class EntitySqlTable<T> internal constructor(
    table: String,
    tableClass: TableImpl<*>,
    dbKeyFactory: BurstKey.Factory<T>,
    private val multiversion: Boolean,
    private val dp: DependencyProvider
) : DerivedSqlTable(table, tableClass, dp), EntityTable<T> {
    internal val dbKeyFactory = dbKeyFactory as SqlDbKey.Factory<T>
    private val defaultSort: MutableList<SortField<*>> = mutableListOf()

    private fun getCache(): Map<BurstKey, T> = dp.db.getCache(table)

    override val count
        get() = dp.db.getUsingDslContext<Int> { ctx ->
            val r = ctx.selectCount().from(tableClass)
            (if (multiversion) r.where(latestField?.isTrue) else r).fetchOne(0, Int::class.javaPrimitiveType)
        }

    override val rowCount
        get() = dp.db.getUsingDslContext<Int> { ctx ->
            ctx.selectCount().from(tableClass).fetchOne(0, Int::class.javaPrimitiveType)
        }

    internal constructor(
        table: String,
        tableClass: TableImpl<*>,
        dbKeyFactory: BurstKey.Factory<T>,
        dp: DependencyProvider
    ) : this(table, tableClass, dbKeyFactory, false, dp)

    init {
        if (multiversion) {
            for (column in this.dbKeyFactory.pkColumns) {
                defaultSort.add(tableClass.field(column, Long::class.java).asc())
            }
        }
        defaultSort.add(heightField.desc())
    }

    protected abstract fun load(ctx: DSLContext, record: Record): T

    internal open fun save(ctx: DSLContext, t: T) {
        // TODO no no-op
    }

    internal open fun save(ctx: DSLContext, ts: Array<T>) {
        for (t in ts) {
            save(ctx, t)
        }
    }

    internal open fun defaultSort(): Collection<SortField<*>> {
        return defaultSort
    }

    override fun ensureAvailable(height: Int) {
        require(!multiversion || height >= dp.blockchainProcessorService.minRollbackHeight) { "Historical data as of height $height not available, set brs.trimDerivedTables=false and re-scan" }
    }

    override fun get(dbKey: BurstKey): T? {
        val key = dbKey as SqlDbKey
        if (dp.db.isInTransaction()) {
            val t = getCache()[key]
            if (t != null) {
                return t
            }
        }
        return dp.db.getUsingDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            query.addConditions(key.getPKConditions(tableClass))
            if (multiversion) {
                query.addConditions(latestField?.isTrue)
            }
            query.addLimit(1)

            get(ctx, query, true)
        }
    }

    override fun get(dbKey: BurstKey, height: Int): T? {
        val key = dbKey as SqlDbKey
        ensureAvailable(height)

        return dp.db.getUsingDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            query.addConditions(key.getPKConditions(tableClass))
            query.addConditions(heightField.le(height))
            if (multiversion) {
                val innerTable = tableClass.`as`("b")
                val innerQuery = ctx.selectQuery()
                innerQuery.addConditions(innerTable.field("height", Int::class.java).gt(height))
                innerQuery.addConditions(key.getPKConditions(innerTable))
                query.addConditions(latestField?.isTrue?.or(DSL.field(DSL.exists(innerQuery))))
            }
            query.addOrderBy(heightField.desc())
            query.addLimit(1)

            get(ctx, query, false)
        }
    }

    override fun getBy(condition: Condition): T? {
        return dp.db.getUsingDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            query.addConditions(condition)
            if (multiversion) {
                query.addConditions(latestField?.isTrue)
            }
            query.addLimit(1)

            get(ctx, query, true)
        }
    }

    override fun getBy(condition: Condition, height: Int): T? {
        ensureAvailable(height)
        return dp.db.getUsingDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            query.addConditions(condition)
            query.addConditions(heightField.le(height))
            if (multiversion) {
                val innerTable = tableClass.`as`("b")
                val innerQuery = ctx.selectQuery()
                innerQuery.addConditions(innerTable.field("height", Int::class.java).gt(height))
                dbKeyFactory.applySelfJoin(innerQuery, innerTable, tableClass)
                query.addConditions(latestField?.isTrue?.or(DSL.field(DSL.exists(innerQuery))))
            }
            query.addOrderBy(heightField.desc())
            query.addLimit(1)
            get(ctx, query, false)
        }
    }

    private fun get(ctx: DSLContext, query: SelectQuery<Record>, cache: Boolean): T? {
        val doCache = cache && dp.db.isInTransaction()
        val record = query.fetchOne() ?: return null
        var t: T? = null
        var dbKey: SqlDbKey? = null
        if (doCache) {
            dbKey = dbKeyFactory.newKey(record) as SqlDbKey
            t = this.getCache()[dbKey]
        }
        return if (t == null) {
            t = load(ctx, record)
            if (doCache && dbKey != null) {
                dp.db.getCache<T>(table)[dbKey] = t
            }
            t
        } else {
            t
        }
    }

    override fun getManyBy(condition: Condition, from: Int, to: Int): Collection<T> {
        return getManyBy(condition, from, to, defaultSort())
    }

    override fun getManyBy(condition: Condition, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        return dp.db.getUsingDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            query.addConditions(condition)
            query.addOrderBy(sort)
            if (multiversion) {
                query.addConditions(latestField?.isTrue)
            }
            SqlDbUtils.applyLimits(query, from, to)
            getManyBy(ctx, query, true)
        }
    }

    override fun getManyBy(condition: Condition, height: Int, from: Int, to: Int): Collection<T> {
        return getManyBy(condition, height, from, to, defaultSort())
    }

    override fun getManyBy(
        condition: Condition,
        height: Int,
        from: Int,
        to: Int,
        sort: Collection<SortField<*>>
    ): Collection<T> {
        ensureAvailable(height)
        return dp.db.getUsingDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            query.addConditions(condition)
            query.addConditions(heightField.le(height))
            if (multiversion) {
                val innerTableB = tableClass.`as`("b")
                val innerQueryB = ctx.selectQuery()
                innerQueryB.addConditions(innerTableB.field("height", Int::class.java).gt(height))
                dbKeyFactory.applySelfJoin(innerQueryB, innerTableB, tableClass)

                val innerTableC = tableClass.`as`("c")
                val innerQueryC = ctx.selectQuery()
                innerQueryC.addConditions(
                    innerTableC.field("height", Int::class.java).le(height).and(
                        innerTableC.field("height", Int::class.java).gt(heightField)
                    )
                )
                dbKeyFactory.applySelfJoin(innerQueryC, innerTableC, tableClass)

                query.addConditions(
                    latestField?.isTrue?.or(
                        DSL.field(
                            DSL.exists(innerQueryB).and(DSL.notExists(innerQueryC))
                        )
                    )
                )
            }
            query.addOrderBy(sort)

            SqlDbUtils.applyLimits(query, from, to)
            getManyBy(ctx, query, true)
        }
    }

    override fun getManyBy(ctx: DSLContext, query: SelectQuery<out Record>, cache: Boolean): Collection<T> {
        val doCache = cache && dp.db.isInTransaction()
        return query.fetchAndMap<Record, T> { record ->
            var t: T? = null
            var dbKey: SqlDbKey? = null
            if (doCache) {
                dbKey = dbKeyFactory.newKey(record) as SqlDbKey
                t = this.getCache()[dbKey]
            }
            if (t == null) {
                t = load(ctx, record)
                if (doCache && dbKey != null) {
                    dp.db.getCache<T>(table)[dbKey] = t
                }
                t
            } else t
        }
    }

    override fun getAll(from: Int, to: Int): Collection<T> {
        return getAll(from, to, defaultSort())
    }

    override fun getAll(from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        return dp.db.getUsingDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            if (multiversion) {
                query.addConditions(latestField?.isTrue)
            }
            query.addOrderBy(sort)
            SqlDbUtils.applyLimits(query, from, to)
            getManyBy(ctx, query, true)
        }
    }

    override fun getAll(height: Int, from: Int, to: Int): Collection<T> {
        return getAll(height, from, to, defaultSort())
    }

    override fun getAll(height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        ensureAvailable(height)
        return dp.db.getUsingDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            query.addConditions(heightField.le(height))
            if (multiversion) {
                val innerTableB = tableClass.`as`("b")
                val innerQueryB = ctx.selectQuery()
                innerQueryB.addConditions(innerTableB.field("height", Int::class.java).gt(height))
                dbKeyFactory.applySelfJoin(innerQueryB, innerTableB, tableClass)

                val innerTableC = tableClass.`as`("c")
                val innerQueryC = ctx.selectQuery()
                innerQueryC.addConditions(
                    innerTableC.field("height", Int::class.java).le(height).and(
                        innerTableC.field(
                            "height",
                            Int::class.java
                        ).gt(heightField)
                    )
                )
                dbKeyFactory.applySelfJoin(innerQueryC, innerTableC, tableClass)

                query.addConditions(
                    latestField?.isTrue?.or(
                        DSL.field(
                            DSL.exists(innerQueryB).and(
                                DSL.notExists(
                                    innerQueryC
                                )
                            )
                        )
                    )
                )
            }
            query.addOrderBy(sort)
            query.addLimit(from, to)
            getManyBy(ctx, query, true)
        }
    }

    override fun insert(t: T) {
        check(dp.db.isInTransaction()) { "Not in transaction" }
        val dbKey = dbKeyFactory.newKey(t) as SqlDbKey
        val cachedT = getCache()[dbKey]
        if (cachedT == null) {
            dp.db.getCache<T>(table)[dbKey] = t
        } else check(!(t !== cachedT)) {
            // not a bug
            "Different instance found in Db cache, perhaps trying to save an object " + "that was read outside the current transaction"
        }
        dp.db.useDslContext { ctx ->
            if (multiversion) {
                val query = ctx.updateQuery(tableClass)
                query.addValue(
                    latestField,
                    false
                )
                query.addConditions(dbKey.getPKConditions(tableClass))
                query.addConditions(latestField?.isTrue)
                query.execute()
            }
            save(ctx, t)
        }
    }

    override fun rollback(height: Int) {
        super.rollback(height)
        dp.db.getCache<Any>(table).clear()
    }
}
