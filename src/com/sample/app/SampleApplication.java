package com.sample.app;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.lib.ShutdownManager;
import com.fererlab.action.ActionHandler;
import com.fererlab.app.Application;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;

import java.io.File;

/**
 * acm | 1/10/13
 */
public class SampleApplication implements Application {

    private ActionHandler actionHandler = new ActionHandler(getClass().getClassLoader().getResource("executionmap.properties"));
    private EbeanServer ebeanServer = null;
    private boolean isDevelopment = false;

    @Override
    public void setDevelopmentMode(boolean isDevelopment) {
        this.isDevelopment = isDevelopment;
    }

    @Override
    public boolean isDevelopmentModeOn() {
        return isDevelopment;
    }

    @Override
    public void start() {
        if (ebeanServer == null) {
            connectToDB();
        }
    }

    @Override
    public Response runApplication(final Request request) {

        // set the cookie secret sign key
        request.getSession().setCookieSignSecretKey("SAMPLE_APPLICATION_SECRET_KEY");

        return actionHandler.runAction(request);
    }

    @Override
    public void stop() {
        if (ebeanServer != null) {
            disconnectDB();
        }
    }

    private void connectToDB() {

        ServerConfig config = new ServerConfig();
        config.setName("pgtest");

        DataSourceConfig postgresDb = new DataSourceConfig();
        postgresDb.setDriver("org.postgresql.Driver");
        postgresDb.setUsername("acm");
        postgresDb.setPassword("123456");
        postgresDb.setUrl("jdbc:postgresql://127.0.0.1:5432/sample");
        postgresDb.setHeartbeatSql("select count(*) from heart_beat");

        config.setDataSourceConfig(postgresDb);
        config.setDdlGenerate(false);
        config.setDdlRun(false);
        config.setDefaultServer(true);
        config.setRegister(true);
        config.addPackage("com.sample.app.model");
        config.setDatabaseSequenceBatchSize(1);

        File ebeansResourceFile = new File("/tmp/ebeans");
        if (!ebeansResourceFile.exists()) {
            if (ebeansResourceFile.mkdirs()) {
                //System.out.println("! directory could no be created");
            }
        }
        config.setResourceDirectory("/tmp/ebeans");
        ebeanServer = EbeanServerFactory.create(config);
    }

    private void disconnectDB() {
        ebeanServer.getServerCacheManager().clearAll();
        ShutdownManager.shutdown();
        ebeanServer = null;
    }


}
