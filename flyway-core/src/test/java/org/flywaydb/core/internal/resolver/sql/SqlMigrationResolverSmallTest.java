/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.internal.resolver.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Scanner;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.flywaydb.core.internal.util.scanner.filesystem.FileSystemResource;
import org.junit.Test;

/**
 * Testcase for SqlMigration.
 */
public class SqlMigrationResolverSmallTest {

    private final Scanner scanner = new Scanner(Thread.currentThread().getContextClassLoader());

    @Test
    public void resolveMigrations() {
        SqlMigrationResolver sqlMigrationResolver = 
                new SqlMigrationResolver(null, scanner,
                        new Location("migration/subdir"), new ResolvedMigrationComparator(), PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "V", "R", "__", ".sql", false);
        Collection<ResolvedMigration> migrations = sqlMigrationResolver.resolveMigrations();

        assertEquals(3, migrations.size());

        List<ResolvedMigration> migrationList = new ArrayList<ResolvedMigration>(migrations);

        assertEquals("1", migrationList.get(0).getVersion().toString());
        assertEquals("1.1", migrationList.get(1).getVersion().toString());
        assertEquals("2.0", migrationList.get(2).getVersion().toString());

        assertEquals("dir1/V1__First.sql", migrationList.get(0).getScript());
        assertEquals("V1_1__Populate_table.sql", migrationList.get(1).getScript());
        assertEquals("dir2/V2_0__Add_foreign_key.sql", migrationList.get(2).getScript());
    }

    @Test
    public void resolveMigrationsRoot() {
        SqlMigrationResolver sqlMigrationResolver = 
                new SqlMigrationResolver(null, scanner, new Location(""), new ResolvedMigrationComparator(),
                        PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "CheckValidate", "X", "__", ".sql", false);

        // changed to 3 as new test cases are added for SybaseASE and DB2
        assertEquals(3, sqlMigrationResolver.resolveMigrations().size());
    }

    @Test
    public void resolveMigrationsNonExisting() {
        SqlMigrationResolver sqlMigrationResolver = 
                new SqlMigrationResolver(null, scanner,
                        new Location("non/existing"), new ResolvedMigrationComparator(), PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8",
                            "CheckValidate", "R", "__", ".sql", false);

        sqlMigrationResolver.resolveMigrations();
    }

    @Test
    public void extractScriptName() {
        SqlMigrationResolver sqlMigrationResolver = 
                new SqlMigrationResolver(null, scanner,
                        new Location("db/migration"), new ResolvedMigrationComparator(), PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "db_", "R", "__", ".sql", false);

        assertEquals("db_0__init.sql", sqlMigrationResolver.extractScriptName(
                new ClassPathResource("db/migration/db_0__init.sql", Thread.currentThread().getContextClassLoader())));
    }

    @Test
    public void extractScriptNameRootLocation() {
        SqlMigrationResolver sqlMigrationResolver = 
                new SqlMigrationResolver(null, scanner, new Location(""), new ResolvedMigrationComparator(),
                        PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8", "db_", "R", "__", ".sql", false);

        assertEquals("db_0__init.sql", sqlMigrationResolver.extractScriptName(
                new ClassPathResource("db_0__init.sql", Thread.currentThread().getContextClassLoader())));
    }

    @Test
    public void extractScriptNameFileSystemPrefix() {
        SqlMigrationResolver sqlMigrationResolver = 
                new SqlMigrationResolver(null, scanner,
                        new Location("filesystem:/some/dir"), new ResolvedMigrationComparator(), PlaceholderReplacer.NO_PLACEHOLDERS, "UTF-8",
                            "V", "R", "__", ".sql", false);

        assertEquals("V3.171__patch.sql", sqlMigrationResolver.extractScriptName(new FileSystemResource("/some/dir/V3.171__patch.sql")));
    }

    @Test
    public void isSqlCallback() {
        assertTrue(SqlMigrationResolver.isSqlCallback("afterMigrate.sql", ".sql"));
        assertFalse(SqlMigrationResolver.isSqlCallback("V1__afterMigrate.sql", ".sql"));
    }

    @Test
    public void calculateChecksum() {
        assertEquals(SqlMigrationResolver.calculateChecksum(null, "abc\ndef efg\nxyz"),
                SqlMigrationResolver.calculateChecksum(null, "abc\r\ndef efg\nxyz\r\n"));
    }
}
