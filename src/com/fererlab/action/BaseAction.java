package com.fererlab.action;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.fererlab.dto.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

/**
 * acm 11/12/12
 */
public class BaseAction<T extends Model> implements CRUDAction<T> {

    private XStream xStreamJSON = new XStream(new JettisonMappedXmlDriver());
    private XStream xstream = new XStream(new StaxDriver());

    private Class<T> type;

    public BaseAction(Class<T> type) {
        this.type = type;
        xstream.autodetectAnnotations(true);
        xStreamJSON.autodetectAnnotations(true);
    }

    @Override
    public T find(Object id) {
        return Ebean.find(type, id);
    }

    @Override
    public List<T> findAll(ParamMap<String, Param<String, Object>> keyValuePairs) {

        ExpressionList<T> expressionList = Ebean.find(type).where();

        if (keyValuePairs != null) {
            for (Param<String, Object> param : keyValuePairs.getParamList()) {
                switch (param.getRelation()) {
                    case EQ:
                        expressionList = expressionList.eq(param.getKey(), param.getValue());
                        break;
                    case NE:
                        expressionList = expressionList.ne(param.getKey(), param.getValue());
                        break;
                    case BETWEEN:
                        expressionList = expressionList.between(param.getKey(), param.getValue(), param.getValueSecondary());
                        break;
                    case GT:
                        expressionList = expressionList.gt(param.getKey(), param.getValue());
                        break;
                    case GE:
                        expressionList = expressionList.ge(param.getKey(), param.getValue());
                        break;
                    case LT:
                        expressionList = expressionList.lt(param.getKey(), param.getValue());
                        break;
                    case LE:
                        expressionList = expressionList.le(param.getKey(), param.getValue());
                        break;
                }
            }
        }

        return expressionList.findList();
    }

    @Override
    public T create(ParamMap<String, Param<String, Object>> keyValuePairs) {
        T t = null;
        try {
            t = type.newInstance();
            for (Method method : type.getDeclaredMethods()) {
                if (method.getName().startsWith("set")) {
                    String fieldName = method.getName().substring(3, 4).toLowerCase(Locale.ENGLISH) + method.getName().substring(4);
                    if (keyValuePairs.containsKey(fieldName)) {
                        try {
                            Class<?>[] parameterClasses = method.getParameterTypes();
                            Object value = keyValuePairs.get(fieldName).getValue();
                            if (parameterClasses.length > 0) {
                                try {
                                    // TODO create object creators for each type available
                                    Constructor constructor = Class.forName(parameterClasses[0].getName()).getConstructor(String.class);
                                    value = constructor.newInstance(keyValuePairs.get(fieldName).getValue());
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                            method.invoke(t, value);
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            Ebean.save(t);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return t;
    }

    @Override
    public T update(Object id, ParamMap<String, Param<String, Object>> keyValuePairs) {

        T t = null;

        try {

            t = Ebean.find(type, id);

            if (t == null) {
                throw new Exception("There is no object found with id: " + id + " of type: " + type);
            }

            for (Method method : type.getDeclaredMethods()) {
                if (method.getName().startsWith("set")) {
                    String fieldName = method.getName().substring(3, 4).toLowerCase(Locale.ENGLISH) + method.getName().substring(4);
                    if (keyValuePairs.containsKey(fieldName)) {
                        try {
                            Class<?>[] parameterClasses = method.getParameterTypes();
                            Object value = keyValuePairs.get(fieldName).getValue();
                            if (parameterClasses.length > 0) {
                                try {
                                    // TODO create object creators for each type available
                                    Constructor constructor = Class.forName(parameterClasses[0].getName()).getConstructor(String.class);
                                    value = constructor.newInstance(keyValuePairs.get(fieldName).getValue());
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                            method.invoke(t, value);
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            Ebean.update(t);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return t;
    }

    @Override
    public int delete(Object id) {
        try {
            return Ebean.delete(type, id);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean deleteAll(List<Object> ids) {
        try {
            Ebean.delete(type, ids);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String toContent(Request request, Object o) {
        if (request.getHeaders().containsKey(RequestKeys.RESPONSE_TYPE.getValue())
                && ((String) request.getHeaders().get(RequestKeys.RESPONSE_TYPE.getValue()).getValue()).equalsIgnoreCase("JSON")) {
            return toJSON(o);
        } else {
            return toXML(o);
        }
    }

    private String toJSON(Object o) {
        return xStreamJSON.toXML(o);
    }

    private String toXML(Object o) {
        return xstream.toXML(o);
    }
}
