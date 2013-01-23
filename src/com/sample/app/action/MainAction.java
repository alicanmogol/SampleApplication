package com.sample.app.action;

import com.fererlab.action.Action;
import com.fererlab.dto.*;

/**
 * acm | 1/21/13
 */
public class MainAction implements Action {

    public Response welcome(Request request) {
        return Response.create(request, "welcome ACM", Status.STATUS_OK);
    }

    public Response main(Request request) {
        return Response.create(request, "main page", Status.STATUS_OK);
    }


}
