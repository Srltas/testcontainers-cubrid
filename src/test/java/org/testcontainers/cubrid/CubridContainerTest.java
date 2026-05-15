package org.testcontainers.cubrid;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CubridContainerTest {

    private static final String IMAGE = "cubrid/cubrid:11.4";

    @Test
    void shouldStartAndQuery() throws SQLException {
        try (CubridContainer cubrid = new CubridContainer(IMAGE)) {
            cubrid.start();

            try (Connection conn = DriverManager.getConnection(
                    cubrid.getJdbcUrl(), cubrid.getUsername(), cubrid.getPassword());
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }
        }
    }

    @Test
    void shouldUseCustomCredentials() throws SQLException {
        try (CubridContainer cubrid = new CubridContainer(IMAGE)
                .withUsername("foo")
                .withPassword("bar")
                .withDatabaseName("mydb")) {
            cubrid.start();

            assertThat(cubrid.getUsername()).isEqualTo("foo");
            assertThat(cubrid.getPassword()).isEqualTo("bar");
            assertThat(cubrid.getDatabaseName()).isEqualTo("mydb");
            assertThat(cubrid.getJdbcUrl()).contains(":mydb:::");

            try (Connection conn = DriverManager.getConnection(
                    cubrid.getJdbcUrl(), "foo", "bar")) {
                assertThat(conn.isValid(5)).isTrue();
            }
        }
    }

    @Test
    void shouldRejectReservedUsername() {
        assertThatThrownBy(() -> new CubridContainer(IMAGE).withUsername("dba"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("dba");

        assertThatThrownBy(() -> new CubridContainer(IMAGE).withUsername("DBA"))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new CubridContainer(IMAGE).withUsername("public"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRunInitScript() throws SQLException {
        try (CubridContainer cubrid = new CubridContainer(IMAGE)
                .withInitScript("init.sql")) {
            cubrid.start();

            try (Connection conn = DriverManager.getConnection(
                    cubrid.getJdbcUrl(), cubrid.getUsername(), cubrid.getPassword());
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT foo FROM bar")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("hello cubrid");
            }
        }
    }

    @Test
    void shouldRejectNullPassword() {
        assertThatThrownBy(() -> new CubridContainer(IMAGE).withPassword(null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
