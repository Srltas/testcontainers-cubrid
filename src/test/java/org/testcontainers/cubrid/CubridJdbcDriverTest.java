package org.testcontainers.cubrid;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

class CubridJdbcDriverTest {

    @Test
    void shouldConnectViaTcUrlWithCustomDatabaseName() throws SQLException {
        String url = "jdbc:tc:cubrid:11.4://localhost/myappdb";
        try (Connection conn = DriverManager.getConnection(url)) {
            DatabaseMetaData md = conn.getMetaData();
            assertThat(md.getURL()).contains(":myappdb:");

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }
        }
    }
}
