package org.kidding.orm.parser.impl;

import org.apache.log4j.Logger;
import org.kidding.orm.entity.POJO;
import org.kidding.orm.parser.SqlParser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;


/**
 * Created by iCrany on 14/11/28.
 */
public class MySqlParser<T extends POJO> implements SqlParser<T> {

    private static final Logger logger = Logger.getLogger(MySqlParser.class);

    /**
     * 实体类的解释器
     */
    private EntityParser entityParser;

    /**
     * 具体某个实体类中的 field --> value 的映射
     */
    private Map<String,Object> attrValueMap;

    /**
     * 具体某个实体类中的 field --> Class 的映射
     */
    private Map<String,Class> typeMap;

    /**
     * 具体某个实体类中的 fieldName set
     */
    private Set<String> keySet;

    public MySqlParser(EntityParser entityParser){
        this.entityParser = entityParser;
    }

    @Override
    public String query(T entity,Boolean isForce, String condition, Integer curPage, Integer pageSize, String orderBy, String groupBy,String... params) throws InvocationTargetException, IllegalAccessException {
        StringBuffer sql = new StringBuffer();
        String tableName = entity._tableName();

        if(null == attrValueMap)
            attrValueMap = entityParser.getAttrValueMap(entity, isForce);

        sql.append(getSelect(params));
        sql.append(getFrom(tableName));
        sql.append(getWhere(attrValueMap,condition));
        sql.append(getGroupBy(groupBy));
        sql.append(getOrderBy(orderBy));
        sql.append(getLimit(curPage,pageSize));

        logger.info(sql);
        return sql.toString();
    }

    /**
     * 删除
     * @param entity
     * @param isForce
     * @param condition
     * @param curPage
     * @param pageSize
     * @param orderBy
     * @param groupBy
     * @param params
     * @return
     */
    @Override
    public String delete(T entity, Boolean isForce, String condition, Integer curPage, Integer pageSize, String orderBy, String groupBy,String... params) {
        StringBuilder sql = new StringBuilder();
        String tableName = entity._tableName();

        sql.append(getDelete(tableName));
        sql.append(getWhere(attrValueMap,condition));
        sql.append(getGroupBy(groupBy));
        sql.append(getOrderBy(orderBy));
        sql.append(getLimit(curPage,pageSize));

        logger.info(sql);
        return sql.toString();
    }

    /**
     * 插入
     * @param entity 实体类
     * @param isForce
     * @param condition
     * @param curPage
     * @param pageSize
     * @param orderBy
     * @param groupBy
     * @param params
     * @return
     */
    @Override
    public String save(T entity, Boolean isForce, String condition, Integer curPage, Integer pageSize, String orderBy, String groupBy,String... params) {
        StringBuilder sql = new StringBuilder();
        try{
            attrValueMap = entityParser.getAttrValueNoPkMap(entity,isForce);
            keySet = attrValueMap.keySet();
            sql.append(getInsert(entity,isForce));
        }catch (Exception e){
            logger.error("fail to create insert sql template!!",e);
            return null;
        }

        logger.info(sql);
        return sql.toString();
    }

    /**
     * 更新
     * @param entity
     * @param isForce
     * @param condition
     * @param curPage
     * @param pageSize
     * @param orderBy
     * @param groupBy
     * @param params
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Override
    public String update(T entity, Boolean isForce, String condition, Integer curPage, Integer pageSize, String orderBy, String groupBy,String... params) throws InvocationTargetException, IllegalAccessException {
        StringBuilder sql = new StringBuilder();
        String tableName = entity._tableName();

        if(null == attrValueMap){
            attrValueMap = entityParser.getAttrValueMap(entity, isForce);
        }

        sql.append(getUpdate(tableName));
        sql.append(getSet(entity,isForce));
        sql.append(getWhereByPk(entity, condition));
        sql.append(getGroupBy(groupBy));
        sql.append(getOrderBy(orderBy));
        sql.append(getLimit(curPage,pageSize));

        logger.info(sql.toString());
        return sql.toString();
    }

    /**
     * 查询，根据不同条件生成相对应的 sql 语句
     * @param entity 实体类
     * @param isForce null 值是否需要
     * @param condition
     * @param curPage
     * @param pageSize
     * @param orderBy
     * @param groupBy
     * @param params
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Override
    public String list(T entity, Boolean isForce, String condition, Integer curPage, Integer pageSize, String orderBy, String groupBy, String... params) throws InvocationTargetException, IllegalAccessException {
        StringBuilder sql = new StringBuilder();
        String tableName = entity._tableName();

        if(null == attrValueMap){
            attrValueMap = entityParser.getAttrValueNoPkMap(entity,isForce);
        }

        sql.append(getSelect(params));
        sql.append(getFrom(tableName));
        sql.append(getWhere(attrValueMap,condition));
        sql.append(getGroupBy(groupBy));
        sql.append(getOrderBy(orderBy));
        sql.append(getLimit(curPage,pageSize));

        logger.info(sql.toString());
        return sql.toString();
    }

    /**
     * 向 pstmt 中填充参数，使用 preparedStatement 防止 sql 注入
     * @param entity 实体对象
     * @param isForce
     * @param pstmt pstmt对象
     * @return
     */
    public PreparedStatement setParameter(T entity,Boolean isForce,PreparedStatement pstmt) throws SQLException {
        Integer index = 1;
        for(String key : keySet){
            pstmt.setObject(index++,attrValueMap.get(key));
        }
        return pstmt;
    }

    /**
     * 根据返回参数构造 select 语句
     * @param params 需要查询的列
     * @return
     */
    private StringBuilder getSelect(String... params){
        StringBuilder select = new StringBuilder("SELECT ");
        if(null == params || 0 == params.length){
            select.append(" * ");
            return select;
        }

        for(String param : params){
            select.append(param+" , ");
        }
        select.setCharAt(select.lastIndexOf(","), ' ');
        return select;
    }

    /**
     * 根据键值对中的值构造 where 语句，对于不同的类型需要添加相应的 单引号
     * @param attrMap
     * @param condition
     * @return
     */
    private StringBuilder getWhere(Map<String,Object> attrMap,String condition){
        StringBuilder where = new StringBuilder("WHERE ");
        if(null == keySet)
            keySet = attrMap.keySet();

        for(String key : keySet){
            where.append(key + "= ? AND ");
        }

        if(null != condition && !condition.isEmpty()){
            where.append(" " + condition);
        }else {
            where.replace(where.lastIndexOf("AND "), where.length() - 1, "");
        }
        return where;
    }

    /**
     * 根据条件生成 where by id 与 相应的 condition 语句
     * @param entity 实体类
     * @param condition
     * @return
     */
    private StringBuilder getWhereByPk(T entity , String condition){
        StringBuilder where = new StringBuilder("WHERE ");
        String pk = entity._primaryKey();

        where.append( pk + " = " + attrValueMap.get(pk));

        if(null != condition && !condition.isEmpty()){
            where.append(" " + condition);
        }

        return where ;
    }


    /**
     * 根据分页的当前页 和 每页的大小 构造分页语句
     * @param curPage 当前页
     * @param pageSize 页大小
     * @return 两个参数有一个为 null 则返回空值
     */
    private StringBuilder getLimit(Integer curPage , Integer pageSize){
        StringBuilder limit = new StringBuilder("LIMIT ");

        if(null != curPage && null != pageSize){
            limit.append(curPage + " , " + pageSize );
            return limit;
        }else{
            return new StringBuilder("");
        }
    }

    /**
     * 根据条件生成 group by 语句
     * @param groupBy
     * @return
     */
    private StringBuilder getGroupBy(String groupBy){
        if(null != groupBy && !groupBy.isEmpty()){
            groupBy = groupBy.trim();
            if(groupBy.toLowerCase().startsWith("group by"))
                return new StringBuilder(groupBy);
            else
                return new StringBuilder("GROUP BY " + groupBy + " ");
        }else{
            return new StringBuilder("");
        }
    }

    /**
     * 根据条件生成 order by 语句
     * @param orderBy orderBy 语句
     * @return
     */
    private StringBuilder getOrderBy(String orderBy){
        if(null != orderBy && !orderBy.isEmpty()){
            orderBy = orderBy.trim();
            if(orderBy.toLowerCase().startsWith("order by")){
                return new StringBuilder(orderBy);
            }else {
                return new StringBuilder("ORDER BY " + orderBy + " ");
            }
        }else{
            return new StringBuilder("");
        }
    }

    /**
     * 根据条件生成 from 语句
     * @param tableName
     * @return
     */
    private StringBuilder getFrom(String tableName){
        if(null != tableName && !tableName.isEmpty()){
            return new StringBuilder("FROM " + tableName + " ");
        }else{
            return new StringBuilder("");
        }
    }

    /**
     * 根据条件生成 update 语句
     * @param tableName 表名
     * @return
     */
    private StringBuilder getUpdate(String tableName){
        if(null != tableName && !tableName.isEmpty()){
            return new StringBuilder("UPDATE " + tableName);
        }else{
            return new StringBuilder("");
        }
    }

    /**
     * 根据条件生成 set 语句
     * @param entity 实体对象
     * @param isForce
     * @return
     */
    private StringBuilder getSet(T entity, Boolean isForce){
        StringBuilder set = new StringBuilder(" SET ");
        if(null == keySet)
            keySet = attrValueMap.keySet();

        for(String key : keySet){
            set.append(key + " = ? , ");
        }
        set.setCharAt(set.lastIndexOf(","), ' ');
        return set;
    }

    /**
     * 根据条件生成 delete 语句
     * @param tableName 表名
     * @return
     */
    private StringBuilder getDelete(String tableName){
        if(null != tableName && !tableName.isEmpty()){
            return new StringBuilder("DELETE FROM " + tableName + " ");
        }else{
            return new StringBuilder("");
        }
    }

    /**
     * 根据条件生成 insert 语句
     * @param entity 实体类
     * @param isForce
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private StringBuilder getInsert(T entity,Boolean isForce) throws InvocationTargetException, IllegalAccessException {
        StringBuilder insert = new StringBuilder("INSERT INTO " + entity._tableName() + "(");
        Map<String,Object> map = entityParser.getAttrValueNoPkMap(entity,isForce);
        StringBuilder params = new StringBuilder();
        Set<String> keySet = map.keySet();

        for(String key : keySet){
            insert.append("" + key + ", ");
            params.append("?, ");
        }
        if(null != keySet && keySet.size() >0){
            insert.setCharAt(insert.lastIndexOf(","),' ');
            params.setCharAt(params.lastIndexOf(","), ' ');
            insert.append(") VALUES ( " + params + ") ");
        }

        return insert;
    }

}
