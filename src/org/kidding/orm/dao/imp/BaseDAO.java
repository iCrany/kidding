package org.kidding.orm.dao.imp;

import org.apache.log4j.Logger;
import org.kidding.orm.dao.DAO;
import org.kidding.orm.db.DBManager;
import org.kidding.orm.entity.POJO;
import org.kidding.orm.parser.SqlParser;
import org.kidding.orm.parser.impl.EntityParser;
import org.kidding.orm.parser.impl.MySqlParser;
import org.kidding.orm.parser.impl.ResultSetParser;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iCrany on 14/11/27.
 */
public class BaseDAO<T extends POJO> implements DAO<T>{

    private Logger logger = Logger.getLogger(BaseDAO.class);

    /**
     * 类类型
     */
    private Class<T> classType;

    /**
     * 一次批量更新最多 5000 条
     */
    private static final Integer batch_limit = 5000;

    /**
     * 相应的 dao 有不同的 数据库连接池
     */
    private String dbAlias;

    /**
     * 相应的 dao 有不同的 sql 解析器
     */
    private SqlParser sqlParser;

    /**
     * 相应的 dao 有不同的 resultSet 解析器
     */
    private ResultSetParser resultSetParser;

    private BaseDAO(String dbAlias,Class<T> classType) throws InstantiationException, IllegalAccessException {
        this.classType = classType;
        this.dbAlias = dbAlias;
        this.sqlParser = new MySqlParser(new EntityParser(classType));
        this.resultSetParser = new ResultSetParser(classType);
    }

    public BaseDAO(SqlParser sqlParser,String dbAlias,Class<T> classType) throws IllegalAccessException, InstantiationException {
        this.classType = classType;
        this.dbAlias = dbAlias;
        this.sqlParser = sqlParser;
        this.resultSetParser = new ResultSetParser(classType);
    }

    public BaseDAO(ResultSetParser resultSetParser , String dbAlias,Class<T> classType) throws InstantiationException, IllegalAccessException {
        this.classType = classType;
        this.dbAlias = dbAlias;
        this.resultSetParser = resultSetParser;
        this.sqlParser = new MySqlParser(new EntityParser(classType));
    }

    public BaseDAO(ResultSetParser resultSetParser , SqlParser sqlParser , String dbAlias,Class<T> classType){
        this.classType = classType;
        this.resultSetParser = resultSetParser;
        this.sqlParser = sqlParser;
        this.dbAlias = dbAlias;
    }

    @Override
    public Integer update(T entity) {
        try {
            return _update(entity,false);
        }catch (Exception e){
            logger.error("fail to update!!",e);
        }
        return 0;
    }

    @Override
    public Integer forceUpdate(T entity) {
        try{
            return _update(entity,true);
        }catch(Exception e){
            logger.error("fail to forceUpdate!!",e);
        }
        return 0;
    }

    @Override
    public Integer batchUpdate(List<T> entityList) {
        return 0;
    }

    /**
     * 具体的实现 update 操作的方法
     * @param entity 实体类
     * @return 返回受影响的条目数
     */
    public Integer _update(T entity,Boolean isForce) throws SQLException, InvocationTargetException, IllegalAccessException {
        Integer affectedNum = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sqlTemplate = null;
        try {
            conn = DBManager.getConnection(dbAlias);

            //根据条件构造 sql 语句，需要一个根据 entity  condition orderBy groupBy 等信息来生产 sql 的一个类
            sqlTemplate = sqlParser.update(entity, isForce, null, null, null, null, null);

            pstmt = conn.prepareStatement(sqlTemplate);
            pstmt = sqlParser.setParameter(entity,isForce,pstmt);
            logger.debug(pstmt.toString());
            affectedNum = pstmt.executeUpdate();
        }finally {
            DBManager.close(pstmt);
            DBManager.close(conn);
        }
        return affectedNum;
    }

    /**
     * 批量更新，这里进行批量的更新操作
     * @param entityList
     * @param isForce
     * @return
     */
    public Boolean _batchUpdate(List<T> entityList,Boolean isForce) throws SQLException, InvocationTargetException, IllegalAccessException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        Boolean result = false;
        String sql = null;
        try {
            conn = DBManager.getConnection(dbAlias);
            conn.setAutoCommit(false);//设置事务回滚

            if(null != entityList && entityList.size()>0){
                sql = sqlParser.update(entityList.get(0),isForce,null,null,null,null,null);
                pstmt = conn.prepareStatement(sql);//这里返回更新 sql 语句模板
                logger.debug("batch update module : " + sql);
                for(T entity : entityList){
                    
                }
            }else{

            }
            conn.commit();//设置事物的提交
        }finally {
            DBManager.close(pstmt);
            DBManager.close(conn);
        }
        return result;
    }

    @Override
    public Long save(T entity) {
        try{
            return _save(entity,false);
        }catch(Exception e) {
            logger.error("fail to save!!!",e);
        }
        return 0L;
    }

    @Override
    public Long forceSave(T entity) {
        try{
            return _save(entity,true);
        }catch (Exception e){
            logger.error("fail to force save!!!",e);
        }
        return 0L;
    }

    @Override
    public Integer batchSave(List<T> entityList) {
        return 0;
    }

    /**
     * 保存
     * @param entity 实体类
     * @param isForce null 值是否需要
     * @return 成功返回成功插入的id,失败返回 null 值
     */
    public Long _save(T entity , Boolean isForce) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sqlTemplate = null;
        Long id = null;

        try{
            conn = DBManager.getConnection(dbAlias);
            sqlTemplate = sqlParser.save(entity,isForce,null,null,null,null,null);

            pstmt = conn.prepareStatement(sqlTemplate,PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt = sqlParser.setParameter(entity,isForce,pstmt);

            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if(rs.next()){
                id = rs.getLong(1);
            }

        }finally {
            DBManager.close(pstmt);
            DBManager.close(conn);
        }

        return id;
    }

    /**
     * 批量保存
     * @param entityList 实体类列表
     * @param isForce null 值是否需要
     * @return 返回受影响的条目数
     */
    public Integer _batchSave(List<T> entityList , Boolean isForce){
        Connection conn = null;
        PreparedStatement pstmt = null;

        try{

        }finally {
            DBManager.close(pstmt);
            DBManager.close(conn);
        }

        return 0;
    }

    @Override
    public Integer delete(T entity) throws SQLException {
        try {
            return _delete(entity,false);
        }catch(Exception e){
            logger.error("fail to delete!!!",e);
        }
        return 0;
    }


    @Override
    public Integer deleteAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        Integer affectedNum = 0;
        String sqlTemplate = null;
        String tableName = null;

        try{
            conn = DBManager.getConnection(dbAlias);
            tableName = classType.newInstance()._tableName();
            sqlTemplate = "DELETE FROM " + tableName;
            pstmt = conn.prepareStatement(sqlTemplate);
            affectedNum = pstmt.executeUpdate();

        }catch(Exception e){
            logger.error("fail to delete all data!!!",e);
        }finally{
            DBManager.close(pstmt);
            DBManager.close(conn);
        }
        return affectedNum;
    }

    @Override
    public Integer batchDelete(List<Integer> idList) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sqlTemplate = null;
        Integer affectedNum = 0;
        T entity = null;

        try{
            entity = classType.newInstance();
            conn = DBManager.getConnection(dbAlias);
            sqlTemplate = sqlParser.batchDelete(entity,idList);
            pstmt = conn.prepareStatement(sqlTemplate);

            affectedNum = pstmt.executeUpdate();
        }catch(Exception e){
            logger.error("fail to delete given list data!!!",e);
        }finally {
            DBManager.close(pstmt);
            DBManager.close(conn);
        }

        return affectedNum;
    }

    /**
     * 根据条件来进行删除操作
     * @param entity 实体类
     * @param isForce null 值是否需要
     * @return
     * @throws SQLException
     */
    public Integer _delete(T entity , Boolean isForce) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        Integer affectedNum = 0;
        String sqlTemplate = null;

        try{
            conn = DBManager.getConnection(dbAlias);
            sqlTemplate = sqlParser.delete(entity,isForce,null,null,null,null,null);

            pstmt = conn.prepareStatement(sqlTemplate);
            pstmt = sqlParser.setParameter(entity,isForce,pstmt);

            affectedNum = pstmt.executeUpdate();

        }finally{
            DBManager.close(pstmt);
            DBManager.close(conn);
        }

        return affectedNum;
    }




    @Override
    public T get(T entity,String... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sqlTemplate = null;
        T result = null;

        try{
            conn = DBManager.getConnection(dbAlias);
            sqlTemplate = sqlParser.list(entity,false,null,null,null,null,null,params);
            pstmt = conn.prepareStatement(sqlTemplate);

            rs = pstmt.executeQuery();

            result = (T) resultSetParser.parse(classType,rs);

        }catch(Exception e){
            logger.error("fail to get entity data!!!",e);
        }finally {
            DBManager.close(rs);
            DBManager.close(pstmt);
            DBManager.close(conn);
        }

        return result;
    }

    @Override
    public List<T> list(T entity, String condition, String orderBy, String groupBy,String... params) throws SQLException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return _list(entity,null,null,condition,orderBy,groupBy,params);
    }

    @Override
    public List<T> list(T entity, Integer curPage, Integer pageSize, String condition, String orderBy, String groupBy,String... params) throws SQLException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return _list(entity,curPage,pageSize,condition,orderBy,groupBy,params);
    }

    @Override
    public List<T> listAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String tableName = null;
        String sqlTemplate = null;
        List<T> result = null;

        try{
            tableName = classType.newInstance()._tableName();
            conn = DBManager.getConnection(dbAlias);
            sqlTemplate = "SELECT * FROM " + tableName;
            pstmt = conn.prepareStatement(sqlTemplate);

            rs = pstmt.executeQuery();
            result = resultSetParser.parseList(classType,rs);

        }catch(Exception e){
            logger.error("fail to list all data!!!",e);
        }finally {
            DBManager.close(rs);
            DBManager.close(pstmt);
            DBManager.close(conn);
        }

        return result;
    }

    /**
     * 根据条件查找符合条件的条目数
     * @param entity 实体类
     * @param curPage
     * @param pageSize
     * @param condition
     * @param orderBy
     * @param groupBy
     * @param params
     * @return
     */
    public List<T> _list(T entity, Integer curPage, Integer pageSize, String condition, String orderBy, String groupBy,String... params) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<T> result = new ArrayList<T>();

        String sqlTemplate = null;
        try{
            conn = DBManager.getConnection(dbAlias);
            sqlTemplate = sqlParser.list(entity,false,condition,curPage,pageSize,orderBy,groupBy,params);

            pstmt = conn.prepareStatement(sqlTemplate);
            pstmt = sqlParser.setParameter(entity,false,pstmt);
            logger.info(pstmt.toString());
            rs = pstmt.executeQuery();

            result = resultSetParser.parseList(entity.getClass(), rs);

        }finally {
            DBManager.close(rs);
            DBManager.close(pstmt);
            DBManager.close(conn);
        }
        return result;
    }
}
