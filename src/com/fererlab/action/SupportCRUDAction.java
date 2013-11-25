package com.fererlab.action;

import com.fererlab.dto.*;

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
                    toContent(request, super.find(request.getParams().getValue("id"))),
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
                    toContent(request, super.findAll(clearKeyValuePairs(request.getParams()))),
                    Status.STATUS_OK
            );
        } catch (Exception e) {
            return Response.internalServerError(request, e);
        }
    }

    public Response create(Request request) {
        try {
            T t = super.create(clearKeyValuePairs(request.getParams()));
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
            ParamMap<String, Param<String, Object>> keyValuePairs = clearKeyValuePairs(request.getParams());
            return Response.create(
                    request,
                    toContent(request, super.update(keyValuePairs.remove("id").getValue(), keyValuePairs)),
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
                    toContent(request, super.delete(request.getParams().getValue("id"))),
                    Status.STATUS_OK
            );
        } catch (Exception e) {
            return Response.internalServerError(request, e);
        }
    }

    public Response deleteAll(Request request) {
        try {
            List<Object> ids = new ArrayList<Object>();
            String idsValue = (String) request.getParams().getValue("ids");
            if (idsValue.lastIndexOf("-") != -1) {
                String[] fromToIds = idsValue.split("-");// 1-4
                int from = Integer.valueOf(fromToIds[0]);
                int to = Integer.valueOf(fromToIds[1]);
                for (int i = from; i <= to; i++) {
                    ids.add(new Integer(i));
                }
            } else if (idsValue.lastIndexOf(",") != -1) {
                String[] stringIds = idsValue.split(",");// 1,2,3,4
                for (String id : stringIds) {
                    ids.add(Integer.valueOf(id));
                }
            }
            return Response.create(
                    request,
                    toContent(request, super.deleteAll(ids), ids),
                    Status.STATUS_OK
            );
        } catch (Exception e) {
            return Response.internalServerError(request, e);
        }
    }

    private ParamMap<String, Param<String, Object>> clearKeyValuePairs(ParamMap<String, Param<String, Object>> params) {
        ParamMap<String, Param<String, Object>> keyValuePairs = new ParamMap<String, Param<String, Object>>();
        keyValuePairs.putAll(params);
        for (RequestKeys requestKeys : RequestKeys.values()) {
            if (params.containsKey(requestKeys.getValue())) {
                keyValuePairs.remove(requestKeys.getValue());
            }
        }
        for (String key : keyValuePairs.keySet()) {
            Param<String, Object> param = keyValuePairs.get(key);
            try {
                if ("true".equalsIgnoreCase(param.getValue().toString()) || "false".equalsIgnoreCase(param.getValue().toString())) {
                    keyValuePairs.put(key, new Param<String, Object>(key, Boolean.valueOf(param.getValue().toString())));
                } else {
                    keyValuePairs.put(key, new Param<String, Object>(
                            key,
                            Integer.valueOf(param.getValue().toString()),
                            param.getRelation().equals(ParamRelation.BETWEEN) && param.getValueSecondary() != null ? Integer.valueOf(param.getValueSecondary().toString()) : param.getValueSecondary(),
                            param.getRelation()
                    ));
                }
            } catch (Exception e) {
                // current param and its key/value pair is ok as they are, do nothing
            }
        }
        return keyValuePairs;
    }

}