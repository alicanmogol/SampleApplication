package com.sample.app;

import com.fererlab.app.BaseApplication;
import com.fererlab.db.EM;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;

/**
 * acm | 1/10/13
 */
public class SampleApplication extends BaseApplication {


    @Override
    public void start() {
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
        EM.stop();
    }

}