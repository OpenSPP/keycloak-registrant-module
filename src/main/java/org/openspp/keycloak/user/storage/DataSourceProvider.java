package org.openspp.keycloak.user.storage;

import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataSourceProvider implements Closeable {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");
    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private HikariDataSource hikariDataSource;

    public DataSourceProvider() {
    }

    synchronized Optional<DataSource> getDataSource() {
        return Optional.ofNullable(hikariDataSource);
    }

    public void configure(String url, JDBC jdbc, String username, String password, String name, int minIdle, int poolSize, long connectionTimeout, long idleTimeout, long lifeTime) {
        HikariConfig hikariConfig = new HikariConfig();

        // https://github.com/brettwooldridge/HikariCP/blob/HikariCP-5.0.1/src/main/java/com/zaxxer/hikari/HikariConfig.java#L50C4-L57C52
        // CONNECTION_TIMEOUT = SECONDS.toMillis(30);
        // VALIDATION_TIMEOUT = SECONDS.toMillis(5);
        // SOFT_TIMEOUT_FLOOR = Long.getLong("com.zaxxer.hikari.timeoutMs.floor", 250L);
        // IDLE_TIMEOUT = MINUTES.toMillis(10);
        // MAX_LIFETIME = MINUTES.toMillis(30);
        // DEFAULT_KEEPALIVE_TIME = 0L;
        // DEFAULT_POOL_SIZE = 10;
        // minIdle = -1;
        // maxPoolSize = -1;

        // The property controls the minimum number of idle connections that HikariCP tries to maintain in the pool, including both idle and in-use connections.
        hikariConfig.setMinimumIdle(minIdle);
        // The property controls the maximum number of connections that HikariCP will keep in the pool, including both idle and in-use connections.
        hikariConfig.setMaximumPoolSize(poolSize);
        // The maximum number of milliseconds that a client will wait for a connection from the pool.
        hikariConfig.setConnectionTimeout(connectionTimeout);
        // This property controls the maximum amount of time (in milliseconds) that a connection is allowed to sit idle in the pool.
        hikariConfig.setIdleTimeout(idleTimeout);
        // This property controls the maximum lifetime of a connection in the pool.
        hikariConfig.setMaxLifetime(lifeTime);

        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setPoolName(
                StringUtils.capitalize(
                        OpenSPPUserStorageProviderFactory.id + name + SIMPLE_DATE_FORMAT.format(new Date())));
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setConnectionTestQuery(jdbc.getTestString());
        hikariConfig.setDriverClassName(jdbc.getDriver());
        HikariDataSource newDS = new HikariDataSource(hikariConfig);
        newDS.validate();
        HikariDataSource old = this.hikariDataSource;
        this.hikariDataSource = newDS;
        disposeOldDataSource(old);
    }

    private void disposeOldDataSource(HikariDataSource old) {
        executor.submit(() -> {
            try {
                if (old != null) {
                    old.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    @Override
    public void close() {
        executor.shutdownNow();
        if (hikariDataSource != null) {
            hikariDataSource.close();
        }
    }
}
