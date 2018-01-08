/*
 * This file is generated by jOOQ.
*/
package brs.schema.tables;


import brs.schema.Db;
import brs.schema.Indexes;
import brs.schema.Keys;
import brs.schema.tables.records.AccountRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.0"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Account extends TableImpl<AccountRecord> {

    private static final long serialVersionUID = 2042071978;

    /**
     * The reference instance of <code>DB.account</code>
     */
    public static final Account ACCOUNT = new Account();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AccountRecord> getRecordType() {
        return AccountRecord.class;
    }

    /**
     * The column <code>DB.account.db_id</code>.
     */
    public final TableField<AccountRecord, Long> DB_ID = createField("db_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>DB.account.id</code>.
     */
    public final TableField<AccountRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>DB.account.creation_height</code>.
     */
    public final TableField<AccountRecord, Integer> CREATION_HEIGHT = createField("creation_height", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>DB.account.public_key</code>.
     */
    public final TableField<AccountRecord, byte[]> PUBLIC_KEY = createField("public_key", org.jooq.impl.SQLDataType.VARBINARY(32), this, "");

    /**
     * The column <code>DB.account.key_height</code>.
     */
    public final TableField<AccountRecord, Integer> KEY_HEIGHT = createField("key_height", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>DB.account.balance</code>.
     */
    public final TableField<AccountRecord, Long> BALANCE = createField("balance", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>DB.account.unconfirmed_balance</code>.
     */
    public final TableField<AccountRecord, Long> UNCONFIRMED_BALANCE = createField("unconfirmed_balance", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>DB.account.forged_balance</code>.
     */
    public final TableField<AccountRecord, Long> FORGED_BALANCE = createField("forged_balance", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>DB.account.name</code>.
     */
    public final TableField<AccountRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(100), this, "");

    /**
     * The column <code>DB.account.description</code>.
     */
    public final TableField<AccountRecord, String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>DB.account.height</code>.
     */
    public final TableField<AccountRecord, Integer> HEIGHT = createField("height", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>DB.account.latest</code>.
     */
    public final TableField<AccountRecord, Boolean> LATEST = createField("latest", org.jooq.impl.SQLDataType.BOOLEAN.nullable(false).defaultValue(org.jooq.impl.DSL.field("1", org.jooq.impl.SQLDataType.BOOLEAN)), this, "");

    /**
     * Create a <code>DB.account</code> table reference
     */
    public Account() {
        this(DSL.name("account"), null);
    }

    /**
     * Create an aliased <code>DB.account</code> table reference
     */
    public Account(String alias) {
        this(DSL.name(alias), ACCOUNT);
    }

    /**
     * Create an aliased <code>DB.account</code> table reference
     */
    public Account(Name alias) {
        this(alias, ACCOUNT);
    }

    private Account(Name alias, Table<AccountRecord> aliased) {
        this(alias, aliased, null);
    }

    private Account(Name alias, Table<AccountRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Db.DB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.ACCOUNT_ACCOUNT_ID_BALANCE_HEIGHT_IDX, Indexes.ACCOUNT_ACCOUNT_ID_HEIGHT_IDX, Indexes.ACCOUNT_ACCOUNT_NAME_IDX, Indexes.ACCOUNT_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<AccountRecord, Long> getIdentity() {
        return Keys.IDENTITY_ACCOUNT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<AccountRecord> getPrimaryKey() {
        return Keys.KEY_ACCOUNT_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<AccountRecord>> getKeys() {
        return Arrays.<UniqueKey<AccountRecord>>asList(Keys.KEY_ACCOUNT_PRIMARY, Keys.KEY_ACCOUNT_ACCOUNT_ID_HEIGHT_IDX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account as(String alias) {
        return new Account(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account as(Name alias) {
        return new Account(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Account rename(String name) {
        return new Account(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Account rename(Name name) {
        return new Account(name, null);
    }
}
