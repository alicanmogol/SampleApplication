package com.sample.app.action;

import com.fererlab.action.Action;
import com.fererlab.action.SupportCRUDAction;
import com.fererlab.dto.*;

/**
 * acm | 1/22/13
 */
@SuppressWarnings("unchecked")
public class GenericAction implements Action {

    private String ENTITY_PACKAGE = "com.sample.app.model";

    public Response find(Request request) {
        Class<? extends Model> modelClass = getModelClass(request);
        if (modelClass != null) {
            return new SupportCRUDAction(modelClass).find(request);
        }
        return Response.create(request, Status.STATUS_NOT_FOUND.getMessage(), Status.STATUS_NOT_FOUND);
    }

    public Response findAll(Request request) {
        Class<? extends Model> modelClass = getModelClass(request);
        if (modelClass != null) {
            return new SupportCRUDAction(modelClass).findAll(request);
        }
        return Response.create(request, Status.STATUS_NOT_FOUND.getMessage(), Status.STATUS_NOT_FOUND);
    }


    public Response create(Request request) {
        Class<? extends Model> modelClass = getModelClass(request);
        if (modelClass != null) {
            return new SupportCRUDAction(modelClass).create(request);
        }
        return Response.create(request, Status.STATUS_NOT_FOUND.getMessage(), Status.STATUS_NOT_FOUND);
    }


    public Response update(Request request) {
        Class<? extends Model> modelClass = getModelClass(request);
        if (modelClass != null) {
            return new SupportCRUDAction(modelClass).update(request);
        }
        return Response.create(request, Status.STATUS_NOT_FOUND.getMessage(), Status.STATUS_NOT_FOUND);
    }


    public Response delete(Request request) {
        Class<? extends Model> modelClass = getModelClass(request);
        if (modelClass != null) {
            return new SupportCRUDAction(modelClass).delete(request);
        }
        return Response.create(request, Status.STATUS_NOT_FOUND.getMessage(), Status.STATUS_NOT_FOUND);
    }


    public Response deleteAll(Request request) {
        Class<? extends Model> modelClass = getModelClass(request);
        if (modelClass != null) {
            return new SupportCRUDAction(modelClass).deleteAll(request);
        }
        return Response.create(request, Status.STATUS_NOT_FOUND.getMessage(), Status.STATUS_NOT_FOUND);
    }


    private Class<? extends Model> getModelClass(Request request) {
        String[] requestURIParts = request.getParams().get(RequestKeys.URI.getValue()).getValue().toString().split("/");
        if (requestURIParts.length > 2) {
            String modelClassName = requestURIParts[3].trim();
            if (!modelClassName.isEmpty()) {
                modelClassName = modelClassName.substring(0, 1).toUpperCase() + modelClassName.substring(1);
                try {
                    return (Class<? extends Model>) Class.forName(ENTITY_PACKAGE + "." + modelClassName);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
