package org.kidding.orm.parser;

import org.kidding.orm.entity.POJO;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by iCrany on 14/11/28.
 * 根据 entity , condition , curPage , pageSize , orderBy , groupBy 等条件生产 sql 语句 以及处理 pstmt
 */
public interface SqlParser<T extends POJO> {

    public String query(T entity, Boolean isForce,String condition,Integer curPage,Integer pageSize,String orderBy,String groupBy,String... params) throws InvocationTargetException, IllegalAccessException;

    public String save(T entity, Boolean isForce,String condition,Integer curPage,Integer pageSize,String orderBy,String groupBy,String... params);

    public String delete(T entity, Boolean isForce,String condition,Integer curPage,Integer pageSize,String orderBy,String groupBy,String... params) throws InvocationTargetException, IllegalAccessException;

    public String update(T entity, Boolean isForce,String condition,Integer curPage,Integer pageSize,String orderBy,String groupBy,String... params) throws InvocationTargetException, IllegalAccessException;

    public String list(T entity, Boolean isForce,String condition,Integer curPage,Integer pageSize,String orderBy,String groupBy,String... params) throws InvocationTargetException, IllegalAccessException;

    public String get(T entity, String... params) throws InvocationTargetException, IllegalAccessException;

    public PreparedStatement setParameter(T entity,Boolean isForce,PreparedStatement pstmt,Boolean isPk) throws SQLException, InvocationTargetException, IllegalAccessException;

    public PreparedStatement setBatchSaveParameter(List<T> entityList , Boolean isForce , PreparedStatement pstmt) throws IllegalAccessException, SQLException, InvocationTargetException;
    //一些多表查询的一些支持方法，还未想到好的解决方案，暂时留空

    public String batchDelete(T entity , List<Integer> idList);

    public String batchSave(T entity , Boolean isForce );
}
