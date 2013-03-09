package com.fererlab.action;

import com.fererlab.dto.Model;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.dto.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * acm | 1/21/13
 */
public class SupportCRUDAction<T extends Model> extends BaseCRUDAction<T> {

    public SupportCRUDAction(Class<T> type) {
        super(type);
    }

    public Response find(Request request) {
        try {
            return Response.create(
                    request,
                    toContent(request, super.find(Long.valueOf(String.valueOf(request.getParams().getValue("id"))))),
                    Status.STATUS_OK
            );
        } catch (Exception e) {
            return Response.internalServerError(request, e);
        }
    }

    public Response findAll(Request request) {
        try {
            return Response.create(
                    request,
                    toContent(request, super.findAll(null)),
                    Status.STATUS_OK
            );
        } catch (Exception e) {
            return Response.internalServerError(request, e);
        }
    }

    public Response create(Request request) {
        try {
            System.out.println("request: " + request);
            T t = super.create(request.getParams());
            System.out.println(">--- t: " + t);
            return Response.create(
                    request,
                    toContent(request, t),
                    Status.STATUS_CREATED
            );
        } catch (Exception e) {
            return Response.internalServerError(request, e);
        }
    }

    public Response update(Request request) {
        try {
            return Response.create(
                    request,
                    toContent(
                            request,
                            super.update(
                                    Long.valueOf(String.valueOf(request.getParams().getValue("id"))),
                                    request.getParams()
                            )
                    ),
                    Status.STATUS_OK
            );
        } catch (Exception e) {
            return Response.internalServerError(request, e);
        }
    }

    public Response delete(Request request) {
        try {
            return Response.create(
                    request,
                    toContent(request, super.delete(Long.valueOf(String.valueOf(request.getParams().getValue("id"))))),
                    Status.STATUS_OK
            );
        } catch (Exception e) {
            return Response.internalServerError(request, e);
        }
    }

    public Response deleteAll(Request request) {
        try {
            List<Object> ids = new ArrayList<Object>();
            String[] strings = ((String) request.getParams().getValue("ids")).split(",");
            for (String id : strings) {
                ids.add(Long.valueOf(id));
            }
            return Response.create(
                    request,
                    toContent(request, super.deleteAll(ids)),
                    Status.STATUS_OK
            );
        } catch (Exception e) {
            return Response.internalServerError(request, e);
        }
    }


}
