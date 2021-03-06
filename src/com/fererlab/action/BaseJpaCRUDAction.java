package com.fererlab.action;

import com.fererlab.db.EM;
import com.fererlab.dto.Model;
import com.fererlab.dto.Param;
import com.fererlab.dto.ParamMap;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * acm 11/12/12
 */
public class BaseJpaCRUDAction<T extends Model> extends BaseAction implements CRUDAction<T> {

    private Class<T> type;

    public BaseJpaCRUDAction(Class<T> type) {
        super();
        this.type = type;

        getXStream().autodetectAnnotations(true);
        getXStream().alias(type.getSimpleName(), type);

        getXStreamJSON().autodetectAnnotations(true);
        getXStreamJSON().alias(type.getSimpleName(), type);
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public T find(Object id) {
        return EM.find(type, id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAll(ParamMap<String, Param<String, Object>> keyValuePairs) {

        // will use criteria builder
        CriteriaBuilder criteriaBuilder = EM.getEntityManager().getCriteriaBuilder();

        // select from entity
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(type);
        Root<T> from = criteriaQuery.from(type);
        criteriaQuery.select(from);

        // will store all predicates
        List<Predicate> predicates = new ArrayList<Predicate>();

        // parameter list
        List<ParameterExpression<Object>> parameterExpressionList = new ArrayList<ParameterExpression<Object>>();
        List<Object> parameterList = new ArrayList<Object>();

        // orderBy may be null
        String orderBy = null;
        Integer offset = null;
        Integer limit = null;
        if (keyValuePairs != null) {
            // set offset, limit and orderBy any if exists
            if (keyValuePairs.containsKey("_offset") && keyValuePairs.getValue("_offset") != null) {
                offset = Integer.valueOf(keyValuePairs.remove("_offset").getValue().toString());
            }
            if (keyValuePairs.containsKey("_limit") && keyValuePairs.getValue("_limit") != null) {
                limit = Integer.valueOf(keyValuePairs.remove("_limit").getValue().toString());
            }
            if (keyValuePairs.containsKey("_order") && keyValuePairs.getValue("_order") != null) {
                orderBy = keyValuePairs.remove("_order").getValue().toString().trim().replace("%20", " ");
            }
            // for rest of the param list
            for (final Param<String, Object> param : keyValuePairs.getParamList()) {
                // create a parameter expression with value's type
                ParameterExpression parameterExpression = null;
                switch (param.getRelation()) {
                    case EQ:
                        parameterExpression = criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(criteriaBuilder.equal(from.get(param.getKey()), parameterExpression));
                        break;
                    case LIKE:
                        parameterExpression = criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(criteriaBuilder.like(from.<String>get(param.getKey()), parameterExpression));
                        break;
                    case NE:
                        parameterExpression = (ParameterExpression<? extends Number>) criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(criteriaBuilder.notEqual(from.<Number>get(param.getKey()), parameterExpression));
                        break;
                    case BETWEEN:
                        parameterExpression = (ParameterExpression<Comparable>) criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(
                                criteriaBuilder.between(from.<Comparable>get(param.getKey()), (Comparable) param.getValue(), (Comparable) param.getValueSecondary())
                        );
                        continue;
                    case GT:
                        parameterExpression = (ParameterExpression<? extends Number>) criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(criteriaBuilder.gt(from.<Number>get(param.getKey()), parameterExpression));
                        break;
                    case GE:
                        parameterExpression = (ParameterExpression<? extends Number>) criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(criteriaBuilder.ge(from.<Number>get(param.getKey()), parameterExpression));
                        break;
                    case LT:
                        parameterExpression = (ParameterExpression<? extends Number>) criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(criteriaBuilder.lt(from.<Number>get(param.getKey()), parameterExpression));
                        break;
                    case LE:
                        parameterExpression = (ParameterExpression<? extends Number>) criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(criteriaBuilder.le(from.<Number>get(param.getKey()), parameterExpression));
                        break;
                }
                // then add the value(with its new type) to parameterList and the parameter expression to its list
                parameterExpressionList.add(parameterExpression);
                parameterList.add(param.getValue());
            }

            // set predicates if any
            if (predicates.size() > 0) {
                Predicate[] predicatesArray = new Predicate[predicates.size()];
                for (int i = 0; i < predicates.size(); i++) {
                    predicatesArray[i] = predicates.get(i);
                }
                Predicate and = criteriaBuilder.and(predicatesArray);
                criteriaQuery.where(and);
            }

        }

        // set if orderBy is not null
        if (orderBy != null) {
            String[] orderByAndOrderDirection = orderBy.split(" ");
            if (orderByAndOrderDirection.length == 2) { // "id asc" or "id desc"
                if (orderByAndOrderDirection[1].equalsIgnoreCase("asc")) {
                    criteriaQuery.orderBy(criteriaBuilder.asc(from.get(orderByAndOrderDirection[0])));
                } else {
                    criteriaQuery.orderBy(criteriaBuilder.desc(from.get(orderByAndOrderDirection[0])));
                }
            } else {
                criteriaQuery.orderBy(criteriaBuilder.desc(from.get(orderBy)));
            }
        }

        // create query
        TypedQuery<T> query = EM.getEntityManager().createQuery(criteriaQuery);

        // set offset if available
        if (offset != null) {
            query = query.setFirstResult(offset);
        }

        // set limit if available
        if (limit != null) {
            query = query.setMaxResults(limit);
        }

        // set parameter values
        if (parameterList.size() > 0) {
            for (int i = 0; i < parameterList.size(); i++) {
                query.setParameter(parameterExpressionList.get(i), parameterList.get(i));
            }
        }

        // return the result list
        return query.getResultList();

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
                                        // method.invoke(...) should work, this should not be called
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

            EM.persist(t);

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

            t = EM.find(type, id);

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
                                        // method.invoke(...) should work, this should not be called
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

            t = EM.merge(t);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return t;
    }

    @Override
    public int delete(Object id) {
        try {
            EM.remove(EM.find(type, id));
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean deleteAll(List<Object> ids) {
        try {
            for (Object id : ids) {
                EM.remove(EM.find(type, id));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
