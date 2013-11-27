package com.sample.app;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.lib.EbeanShutdownHack;
import com.fererlab.app.BaseApplication;
import com.fererlab.db.EM;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.sample.app.model.Product;

import java.io.File;

/**
 * acm | 1/10/13
 */
public class SampleApplication extends BaseApplication {

    private EbeanServer ebeanServer = null;

    @Override
    public void start() {
        // EBean
        if (ebeanServer == null) {
            try {
                ebeanServer = Ebean.getServer("pgtest");
            } catch (Exception e) {
                connectToDB();
            }
        }
        // JPA
        try {
            EM.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public Response runApplication(final Request request) {
        // read the cookie to Session object
        request.getSession().fromCookie(this.getClass().getPackage().getName() + "." + this.getClass().getName(),
                "7a8631cb477c052289b4837bd3c6c611cdc97954-9956608b326e04590c57a839c7fe");
        // run application
        return super.runApplication(request);
    }

    @Override
    public void stop() {
        if (ebeanServer != null) {
            disconnectDB();
        }
        // JPA
        EM.stop();
    }


    private void connectToDB() {
        ServerConfig config = new ServerConfig();
        config.setName("pgtest");

        DataSourceConfig postgresDb = new DataSourceConfig();
        postgresDb.setDriver("org.postgresql.Driver");
        postgresDb.setUsername("alicanmogol");
        postgresDb.setPassword("");
        postgresDb.setUrl("jdbc:postgresql://localhost:5432/bfm");
        postgresDb.setHeartbeatSql("select count(*) from heart_beat");

        config.setDataSourceConfig(postgresDb);
        config.setDdlGenerate(false);
        config.setDdlRun(false);
        config.setDefaultServer(true);
        config.setRegister(true);
        config.addClass(Product.class);
        config.addPackage("com.sample.app.model");
        config.setDatabaseSequenceBatchSize(1);

        File ebeansResourceFile = new File("/tmp/ebeans");
        if (!ebeansResourceFile.exists()) {
            Boolean isDirCreated = ebeansResourceFile.mkdirs();
        }
        config.setResourceDirectory("/tmp/ebeans");
        ebeanServer = EbeanServerFactory.create(config);
    }

    private void disconnectDB() {
        EbeanShutdownHack.shutdownAllActiveEbeanServers(ebeanServer);
    }
}