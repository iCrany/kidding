package org.kidding.orm.parser.impl;

import org.apache.log4j.Logger;
import org.kidding.orm.entity.POJO;
import org.kidding.orm.parser.SqlParser;
import org.kidding.orm.util.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
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
     * 批量删除
     * @param entity 实体类，无具体意义
     * @param idList 实体类的主键列表
     * @return 返回相应的 sql 语句
     */
    @Override
    public String batchDelete(T entity , List<Integer> idList){
        StringBuilder sql = new StringBuilder();
        String tableName = entity._tableName();
        String pkName = entity._primaryKey();

        sql.append(getDelete(tableName));
        sql.append(getWhereIn(pkName, idList));

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
     * 批量插入，这里只需要生成一个 插入 sql 语句的模板即可，剩下的数据由 pstmt 的 addBatch() 方法来处理
     * @param entity 实体类，并没有实际的意义，只是用来获取数据库表明
     * @param isForce null 值是否需要插入
     * @return 根据条件生成的批量插入sql语句，否则返回 null
     */
    @Override
    public String batchSave(T entity , Boolean isForce ){
        StringBuilder sql = new StringBuilder();
        String tableName = null;

        try{
            tableName = entity._tableName();
            sql.append(getInsert(entity,isForce));

        }catch(Exception e){
            logger.error("fail to create batch save sql template!!!",e);
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
            attrValueMap = entityParser.getAttrValueNoPkMap(entity, isForce);
        }

        sql.append(getSelect(params));
        sql.append(getFrom(tableName));
        sql.append(getWhere(attrValueMap, condition));
        sql.append(getGroupBy(groupBy));
        sql.append(getOrderBy(orderBy));
        sql.append(getLimit(curPage,pageSize));

        logger.info(sql.toString());
        return sql.toString();
    }

    /**
     * 向 pstmt 中填充参数，使用 preparedStatement 防止 sql 注入
     * @param entity 实体对象
     * @param isForce null 值是否需要更新
     * @param pstmt pstmt对象
     * @return
     */
    public PreparedStatement setParameter(T entity,Boolean isForce ,PreparedStatement pstmt) throws SQLException, InvocationTargetException, IllegalAccessException {
        Integer index = 1;
        attrValueMap = entityParser.getAttrValueMap(entity, isForce);//需要每次都进行更新数据，该方法会被批量处理代码调用
        for(String key : keySet){
            pstmt.setObject(index++,attrValueMap.get(key));
        }
        return pstmt;
    }

    /**
     * 向 pstmt 中填充参数，使用 preparedStatement 防止 sql 注入，支持用户自定义参数值，使查询更加灵活
     * @param paramsValue 与 pstmt 中得参数的值相同的值顺序
     * @param pstmt pstmt对象
     * @return
     * @throws SQLException
     */
    public PreparedStatement setParameter(List<Object> paramsValue , PreparedStatement pstmt) throws SQLException {
        Integer index = 1;
        if(null == paramsValue ) throw new SQLException("paramsValue is null,fail to set sql parameter!!!");
        for(Object value : paramsValue) {
            pstmt.setObject(index++,value);
        }
        return pstmt;
    }

    /**
     * 批量插入数据
     * @param entityList
     * @param isForce null 是否需要
     * @param pstmt
     * @return
     */
    public PreparedStatement setBatchSaveParameter(List<T> entityList , Boolean isForce , PreparedStatement pstmt) throws IllegalAccessException, SQLException, InvocationTargetException {

        for(T entity : entityList){
            pstmt = setParameter(entity,isForce,pstmt);
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
     * 根据条件生成 in 语句 , 无需进行数值的填充 setParameter 操作
     * @param pkName 主键名称
     * @param idList 主键列表
     * @return
     */
    private StringBuilder getWhereIn(String pkName , List<Integer> idList){
        StringBuilder batchDelete = new StringBuilder("WHERE " + pkName + " IN (");

        batchDelete.append(StringUtil.join(idList,","));
        batchDelete.append(") ");
        return batchDelete;

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

//    /**
//     * 根据条件生成 batch insert 语句
//     * @param entityList
//     * @param isForce
//     * @param tableName
//     * @return
//     */
//    private StringBuilder getBatchInsert(List<T> entityList , Boolean isForce,String tableName) throws InvocationTargetException, IllegalAccessException {
//        StringBuilder batchInsert = new StringBuilder("INSERT INTO " + tableName + "(");
//        if(null == entityList || entityList.size() == 0) throw new NullPointerException("the batch save given list is null or size is zero!!!");
//
//        T example = entityList.get(0);
//        Map<String,Object> map = entityParser.getAttrValueNoPkMap(example, isForce);
//        StringBuilder params = new StringBuilder();
//        Set<String> keySet = map.keySet();
//
//        for(String key : keySet){
//            batchInsert.append("" + key + ", ");
//        }
//        batchInsert.setCharAt(batchInsert.lastIndexOf(","),')');
//        batchInsert.append(" VALUES ");
//
//        for(T entity : entityList){
//            map = entityParser.getAttrValueNoPkMap(entity,isForce);
//
//        }
//
//
//
//        return batchInsert;
//    }

}
