package com.fererlab.action;

import com.fererlab.dto.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * acm | 1/21/13
 */
public class SupportCRUDAction<T extends Model> extends BaseAction {

    private CRUDAction<T> crudAction;

    public SupportCRUDAction(Class<T> type) {
        crudAction = new BaseJpaCRUDAction<T>(type);
        // crudAction = new BaseEBeanCRUDAction<T>(type);
        // crudAction has below implementations currently available
        // crudAction = new BaseEBeanCRUDAction<T>(type);
    }

    public SupportCRUDAction(Class<T> type, Class<CRUDAction<T>> crudActionClass) {
        try {
            crudAction = crudActionClass.getConstructor(type).newInstance(type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Response find(Request request) {
        try {
            return Response.create(
                    request,
                    crudAction.toContent(request, crudAction.find(request.getParams().getValue("id"))),
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
                    crudAction.toContent(request, crudAction.findAll(clearKeyValuePairs(request.getParams()))),
                    Status.STATUS_OK
            );
        } catch (Exception e) {
            return Response.internalServerError(request, e);
        }
    }

    public Response create(Request request) {
        try {
            T t = crudAction.create(clearKeyValuePairs(request.getParams()));
            return Response.create(
                    request,
                    crudAction.toContent(request, t),
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
                    crudAction.toContent(request, crudAction.update(keyValuePairs.remove("id").getValue(), keyValuePairs)),
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
                    crudAction.toContent(request, crudAction.delete(request.getParams().getValue("id"))),
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
                    crudAction.toContent(request, crudAction.deleteAll(ids), ids),
                    Status.STATUS_OK
            );
        } catch (Exception e) {
            return Response.internalServerError(request, e);
        }
    }

    private ParamMap<String, Param<String, Object>> clearKeyValuePairs(ParamMap<String, Param<String, Object>> params) {

        // put all params to key value pairs, but not the request keys
        ParamMap<String, Param<String, Object>> keyValuePairs = new ParamMap<String, Param<String, Object>>();
        keyValuePairs.putAll(params);
        for (RequestKeys requestKeys : RequestKeys.values()) {
            if (params.containsKey(requestKeys.getValue())) {
                keyValuePairs.remove(requestKeys.getValue());
            }
        }

        // for each value in the keyValuePair, set the correct type for value
        for (String key : keyValuePairs.keySet()) {
            Param<String, Object> param = keyValuePairs.get(key);
            try {
                Field field = crudAction.getType().getDeclaredField(param.getKey());
                Class<?> fieldClass = field.getType();
                // then if the value's type is different from that, try to cast,
                if (!fieldClass.isInstance(param.getValue())) {
                    try {
                        param = new Param<String, Object>(
                                param.getKey(),
                                fieldClass.cast(param.getValue()),
                                param.getValueSecondary(),
                                param.getRelation()
                        );
                    } catch (Exception e) {
                        //              if cannot cast, try to create an instance of value's type using the
                        //                  value(probably the string value) as the constructor parameter of valueOf(...) parameter
                        try {
                            if (param.getRelation().equals(ParamRelation.BETWEEN)) {
                                param = new Param<String, Object>(
                                        param.getKey(),
                                        fieldClass.getDeclaredConstructor(String.class).newInstance(param.getValue().toString()),
                                        fieldClass.getDeclaredConstructor(String.class).newInstance(param.getValueSecondary().toString()),
                                        param.getRelation()
                                );
                            } else {
                                param = new Param<String, Object>(
                                        param.getKey(),
                                        fieldClass.getDeclaredConstructor(String.class).newInstance(param.getValue().toString()),
                                        param.getValueSecondary(),
                                        param.getRelation()
                                );
                            }
                        } catch (Exception e1) {
                            // this value's class and the declared field's class are not compatible, will skip this key/value pair!
                            e1.printStackTrace();
                            continue;
                        }
                    }
                }
                // put the param over the old key's value
                keyValuePairs.put(key, param);
            } catch (Exception e) {
                // current param and its key/value pair is ok as they are, do nothing
            }
        }
        return keyValuePairs;
    }

}