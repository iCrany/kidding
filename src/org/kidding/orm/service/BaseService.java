package org.kidding.orm.service;

import org.kidding.orm.dao.imp.BaseDAO;
import org.kidding.orm.entity.POJO;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by iCrany on 14/12/30.
 * 使用所有的数据库查询操作在 service 层也可以直接调用，而不用创建 dao 对象，将创建 dao 的任务封装在 BaseService 层
 * 只需要在创建 Service 指定相应的 vo 类即可，以及 dao 类
 */
public class BaseService<T extends POJO> {

    /**对应的 vo 的 class 类别*/
    private Class<T> classType;

    /*对应 vo 的实体类**/
    private T entity;

    /**该 vo 对应的 dao */
    private BaseDAO<T> dao;

    /**数据库名*/
    private String dbAlias;

    public BaseService(String dbAlias,Class classType) throws IllegalAccessException, InstantiationException {
        this.classType = classType;
        this.dbAlias = dbAlias;
        this.dao = new BaseDAO<T>(dbAlias,classType);
    }

    public Integer _update(T entity){
        return dao.update(entity);
    }

    public Integer _forceUpdate(T entity){
        return dao.forceUpdate(entity);
    }

    public Integer _batchUpdate(List<T> entityList,Boolean isForce) throws IllegalAccessException, SQLException, InvocationTargetException {
        return dao._batchUpdate(entityList, isForce);
    }

    public Integer _save(T entity){
        return dao.save(entity);
    }

    public Integer _forceSave(T entity){
        return dao.forceSave(entity);
    }

    public Integer _batchSave(List<T> entityList,Boolean isForce){
        return dao.batchSave(entityList,isForce);
    }

    public Integer _delete(T entity) throws SQLException {
        return dao.delete(entity);
    }

    public Integer _deleteAll(){
        return dao.deleteAll();
    }

    public T _get(T entity,String... params){
        return dao.get(entity,params);
    }

    public List<T> _listAll(){
        return dao.listAll();
    }

    public List<T> _list(T entity,String condition,String orderBy,String groupBy,String...params) throws InvocationTargetException, SQLException, InstantiationException, IllegalAccessException {
        return dao.list(entity,condition,orderBy,groupBy,params);
    }

    public List<T> _list(T entity,Integer curPage,Integer pageSize,String condition , String orderBy ,String groupBy,String...params) throws InvocationTargetException, SQLException, InstantiationException, IllegalAccessException {
        return dao.list(entity,curPage,pageSize,condition,orderBy,groupBy,params);
    }

}
