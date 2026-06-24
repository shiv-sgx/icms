package com.sgx.icms.config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialises the HikariCP connection pool on web-app startup and closes it on
 * shutdown, preventing connection leaks across redeploys. Registered in web.xml.
 */
public class AppContextListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info("ICMS starting up — initialising data source");
        DataSourceProvider.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("ICMS shutting down — closing data source");
        DataSourceProvider.close();
    }
}
