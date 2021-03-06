/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.flyway.core.dbsupport.hsql;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.Table;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.SQLException;

/**
 * Hsql-specific table.
 */
public class HsqlTable extends Table {
    private static final Log LOG = LogFactory.getLog(HsqlDbSupport.class);

    /**
     * Flag indicating whether we are running against the old Hsql 1.8 instead of the newer 2.x.
     */
    private boolean version18;

    /**
     * Creates a new Hsql table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public HsqlTable(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name) {
        super(jdbcTemplate, dbSupport, schema, name);

        try {
            int majorVersion = jdbcTemplate.getMetaData().getDatabaseMajorVersion();
            version18 = majorVersion < 2;
        } catch (SQLException e) {
            throw new FlywayException("Unable to determine the Hsql version", e);
        }
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + dbSupport.quote(schema.getName(), name) + " CASCADE");
    }

    @Override
    protected boolean doExists() throws SQLException {
        return exists(null, schema, name);
    }

    @Override
    protected boolean doExistsNoQuotes() throws SQLException {
        return exists(null, dbSupport.getSchema(schema.getName().toUpperCase()), name.toUpperCase());
    }

    @Override
    protected void doLock() throws SQLException {
        if (version18) {
            LOG.debug("Unable to lock " + this + " as Hsql 1.8 does not support locking. No concurrent migration supported.");
        } else {
            jdbcTemplate.execute("LOCK TABLE " + this + " WRITE");
        }
    }
}
