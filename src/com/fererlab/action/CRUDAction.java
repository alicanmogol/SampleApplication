package com.fererlab.action;

import com.fererlab.dto.Model;
import com.fererlab.dto.Param;
import com.fererlab.dto.ParamMap;

import java.util.List;

/**
 * acm 11/12/12
 */
public interface CRUDAction<T extends Model> extends Action {

    public T find(Object id);

    public List<T> findAll(ParamMap<String, Param<String, Object>> keyValuePairs);

    public T create(ParamMap<String, Param<String, Object>> keyValuePairs);

    public T update(Object id, ParamMap<String, Param<String, Object>> keyValuePairs);

    public int delete(Object id);

    public boolean deleteAll(List<Object> ids);

}