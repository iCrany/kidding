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

    public BaseDAO(String dbAlias,Class<T> classType) throws InstantiationException, IllegalAccessException {
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
            pstmt = sqlParser.setParameter(entity,isForce,pstmt,false);
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
     * @param entityList 实体类列表
     * @param isForce null 值是否需要更新
     * @return 返回是否成功更新
     */
    public Boolean _batchUpdate(List<T> entityList,Boolean isForce) throws SQLException, InvocationTargetException, IllegalAccessException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sqlTemplate = null;
        int[] affectedNum = null;
        try {
            conn = DBManager.getConnection(dbAlias);

            sqlTemplate = sqlParser.update(entityList.get(0), isForce, null, null, null, null, null);
            pstmt = conn.prepareStatement(sqlTemplate);//这里返回更新 sql 语句模板
            pstmt = sqlParser.setBatchSaveParameter(entityList,isForce,pstmt);

            conn.setAutoCommit(false);
            affectedNum = pstmt.executeBatch();
            conn.commit();//设置事物的提交
        }finally {
            DBManager.close(pstmt);
            DBManager.close(conn);
        }

        for(Integer index = 0 ; index < entityList.size() ; index++){
            if(affectedNum[index] == PreparedStatement.EXECUTE_FAILED) return false;
        }
        return true;
    }

    @Override
    public Integer save(T entity) {
        try{
            return _save(entity,false);
        }catch(Exception e) {
            logger.error("fail to save!!!",e);
        }
        return 0;
    }

    @Override
    public Integer forceSave(T entity) {
        try{
            return _save(entity,true);
        }catch (Exception e){
            logger.error("fail to force save!!!",e);
        }
        return 0;
    }

    @Override
    public Integer batchSave(List<T> entityList , Boolean isForce) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sqlTemplate = null;
        int[] affectedNum = null;

        try{
            conn = DBManager.getConnection(dbAlias);
            sqlTemplate = sqlParser.batchSave(entityList.get(0), isForce);
            pstmt = conn.prepareStatement(sqlTemplate);
            pstmt = sqlParser.setBatchSaveParameter(entityList,isForce,pstmt);

            conn.setAutoCommit(false);
            affectedNum = pstmt.executeBatch();
            conn.commit();

        }catch(Exception e){
            try {
                conn.rollback();
            } catch (SQLException e1) {
                logger.error("batch save statement fail to rollback!!!",e1);
            }
            logger.error("fail to batch save!!!",e);
        }finally {
            DBManager.close(pstmt);
            DBManager.close(conn);
        }

        for(Integer index = 0 ; index < entityList.size() ; index++){
            if(affectedNum[index] == PreparedStatement.EXECUTE_FAILED) return null;
        }
        return 1;
    }

    /**
     * 保存
     * @param entity 实体类
     * @param isForce null 值是否需要
     * @return 成功返回成功插入的id,失败返回 null 值
     */
    public Integer _save(T entity , Boolean isForce) throws SQLException, InvocationTargetException, IllegalAccessException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sqlTemplate = null;
        Integer id = null;

        try{
            conn = DBManager.getConnection(dbAlias);
            sqlTemplate = sqlParser.save(entity,isForce,null,null,null,null,null);

            pstmt = conn.prepareStatement(sqlTemplate,PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt = sqlParser.setParameter(entity,isForce,pstmt,false);

            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();
            if(rs.next()){
                id = rs.getInt(1);
            }

        }finally {
            DBManager.close(pstmt);
            DBManager.close(conn);
        }

        return id;
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
     * @return 返回受影响的条目数
     * @throws SQLException
     */
    public Integer _delete(T entity , Boolean isForce) throws SQLException, InvocationTargetException, IllegalAccessException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        Integer affectedNum = 0;
        String sqlTemplate = null;

        try{
            conn = DBManager.getConnection(dbAlias);
            sqlTemplate = sqlParser.delete(entity,isForce,null,null,null,null,null);

            pstmt = conn.prepareStatement(sqlTemplate);
            pstmt = sqlParser.setParameter(entity,isForce,pstmt,true);

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
            sqlTemplate = sqlParser.get(entity,params);
            pstmt = conn.prepareStatement(sqlTemplate);
            pstmt = sqlParser.setParameter(entity,false,pstmt,true);

            rs = pstmt.executeQuery();

            result = (T) resultSetParser.parse(classType,rs,params);

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
     * @param curPage 当前是第几页
     * @param pageSize 每一页显示的条目数
     * @param condition 一些 entity 中无法表示的条件，例如 field < 11
     * @param orderBy 排序条件
     * @param groupBy 分组条件
     * @param params 需要返回的字段，尽量填写，避免不必要的数据传输
     * @return 返回条件查询的实体类列表
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
            pstmt = sqlParser.setParameter(entity,false,pstmt,false);
            rs = pstmt.executeQuery();

            result = resultSetParser.parseList(entity.getClass(), rs,params);

        }finally {
            DBManager.close(rs);
            DBManager.close(pstmt);
            DBManager.close(conn);
        }
        return result;
    }
}
