package com.sample.app.action;

import com.fererlab.action.BaseAction;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.dto.Status;

/**
 * acm 4/24/13
 */
public class FlowAction extends BaseAction {


    public Response execute(Request request) {

        return Response.create(request, "operation not supported", Status.STATUS_NOT_FOUND);
    }

}
