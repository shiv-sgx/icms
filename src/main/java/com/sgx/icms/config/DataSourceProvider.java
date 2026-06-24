package com.sgx.icms.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Owns the single application-wide HikariCP connection pool.
 *
 * <p>Lifecycle is driven by {@link AppContextListener}: {@link #init()} on context
 * startup, {@link #close()} on shutdown. All connections are obtained from this pool;
 * nothing opens raw {@code DriverManager} connections.
 */
public final class DataSourceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceProvider.class);

    private static volatile HikariDataSource dataSource;

    private DataSourceProvider() {
    }

    public static synchronized void init() {
        if (dataSource != null) {
            return;
        }
        AppConfig cfg = AppConfig.get();

        HikariConfig hc = new HikariConfig();
        hc.setPoolName("icms-pool");
        // Set the driver class explicitly: in a servlet container the JDBC driver is
        // not auto-registered with DriverManager via SPI, so resolving it from the URL
        // alone yields "No suitable driver". Naming the class forces it to load/register.
        hc.setDriverClassName(cfg.get("db.driver", "com.mysql.cj.jdbc.Driver"));
        hc.setJdbcUrl(cfg.require("db.url"));
        hc.setUsername(cfg.require("db.user"));
        hc.setPassword(cfg.get("db.password", ""));
        hc.setMaximumPoolSize(cfg.getInt("db.pool.max", 10));
        hc.setMinimumIdle(cfg.getInt("db.pool.min", 2));
        hc.setConnectionTimeout(cfg.getInt("db.pool.connectionTimeoutMs", 30000));
        hc.setMaxLifetime(cfg.getInt("db.pool.maxLifetimeMs", 1800000));
        // Fail fast at startup if the DB is unreachable, but don't hang forever.
        hc.setInitializationFailTimeout(cfg.getInt("db.pool.initFailTimeoutMs", 10000));

        dataSource = new HikariDataSource(hc);
        LOG.info("HikariCP pool '{}' initialised (max={}, min={})",
                hc.getPoolName(), hc.getMaximumPoolSize(), hc.getMinimumIdle());
    }

    public static DataSource dataSource() {
        HikariDataSource ds = dataSource;
        if (ds == null) {
            throw new IllegalStateException("DataSource not initialised; AppContextListener must run first");
        }
        return ds;
    }

    /** Exposed for the admin monitoring dashboard. */
    public static HikariDataSource hikari() {
        return dataSource;
    }

    public static synchronized void close() {
        if (dataSource != null) {
            LOG.info("Closing HikariCP pool '{}'", dataSource.getPoolName());
            dataSource.close();
            dataSource = null;
        }
    }
}
