package com.fererlab.action;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.FutureList;
import com.fererlab.dto.Model;
import com.fererlab.dto.Param;
import com.fererlab.dto.ParamMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

/**
 * acm 11/12/12
 */
public class BaseCRUDAction<T extends Model> extends BaseAction implements CRUDAction<T> {

    private Class<T> type;

    public BaseCRUDAction(Class<T> type) {
        super();
        this.type = type;

        getXStream().autodetectAnnotations(true);
        getXStream().alias(type.getSimpleName(), type);
        getXStream().alias(type.getSimpleName() + "s", com.avaje.ebean.common.BeanList.class);

        getXStreamJSON().autodetectAnnotations(true);
        getXStreamJSON().alias(type.getSimpleName(), type);
        getXStreamJSON().alias(type.getSimpleName() + "s", com.avaje.ebean.common.BeanList.class);
    }

    @Override
    public T find(Object id) {
        return Ebean.find(type, id);
    }

    @Override
    public List<T> findAll(ParamMap<String, Param<String, Object>> keyValuePairs) {
        FutureList<T> futureList = Ebean.createQuery(type).findFutureList();
        ExpressionList<T> expressionList = Ebean.find(type).where();
        String orderBy = null;

        if (keyValuePairs != null) {
            if (keyValuePairs.containsKey("orderBy") && keyValuePairs.getValue("orderBy") != null) {
                orderBy = keyValuePairs.remove("orderBy").getValue().toString();
            }
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
        if (orderBy != null) {
            return expressionList.order(orderBy).findList();
        } else {
            return expressionList.findList();
        }
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
                            method = t.getClass().getDeclaredMethod(method.getName(), parameterClasses);
                            Object value = keyValuePairs.get(fieldName).getValue();
                            try {
                                method.invoke(t, value);
                            } catch (IllegalArgumentException iae) {
                                if (parameterClasses.length > 0) {
                                    try {
                                        // TODO create object creators for each type available
                                        Constructor constructor = Class.forName(parameterClasses[0].getName()).getConstructor(String.class);
                                        value = constructor.newInstance(keyValuePairs.get(fieldName).getValue());
                                        method.invoke(t, value);
                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
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
                            method = t.getClass().getDeclaredMethod(method.getName(), parameterClasses);
                            Object value = keyValuePairs.get(fieldName).getValue();
                            try {
                                method.invoke(t, value);
                            } catch (IllegalArgumentException iae) {
                                if (parameterClasses.length > 0) {
                                    try {
                                        // TODO create object creators for each type available
                                        Constructor constructor = Class.forName(parameterClasses[0].getName()).getConstructor(String.class);
                                        value = constructor.newInstance(keyValuePairs.get(fieldName).getValue());
                                        method.invoke(t, value);
                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
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
}
