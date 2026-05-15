package org.testcontainers.cubrid;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.JdbcDatabaseContainerProvider;
import org.testcontainers.jdbc.ConnectionUrl;
import org.testcontainers.utility.DockerImageName;

/**
 * Factory for {@link CubridContainer}, registered as a {@link JdbcDatabaseContainerProvider}
 * via the JDBC URL SPI.
 * <p>
 * Allows URLs of the form {@code jdbc:tc:cubrid:<tag>://<host>/<db>} to resolve to
 * a managed CUBRID container.
 */
public class CubridContainerProvider extends JdbcDatabaseContainerProvider {

    private static final String USER_PARAM = "user";
    private static final String PASSWORD_PARAM = "password";

    @Override
    public boolean supports(String databaseType) {
        return CubridContainer.NAME.equals(databaseType);
    }

    @Override
    public JdbcDatabaseContainer newInstance() {
        return newInstance(CubridContainer.DEFAULT_TAG);
    }

    @Override
    public JdbcDatabaseContainer newInstance(String tag) {
        if (tag != null) {
            return new CubridContainer(DockerImageName.parse(CubridContainer.IMAGE).withTag(tag));
        }
        return newInstance();
    }

    @Override
    public JdbcDatabaseContainer newInstance(ConnectionUrl url) {
        String tag = url.getImageTag().orElse(CubridContainer.DEFAULT_TAG);
        CubridContainer container = new CubridContainer(
            DockerImageName.parse(CubridContainer.IMAGE).withTag(tag));

        container.withReuse(url.isReusable());
        url.getDatabaseName().ifPresent(container::withDatabaseName);

        String user = url.getQueryParameters().get(USER_PARAM);
        if (user != null) {
            container.withUsername(user);
        }
        String password = url.getQueryParameters().get(PASSWORD_PARAM);
        if (password != null) {
            container.withPassword(password);
        }
        return container;
    }
}
