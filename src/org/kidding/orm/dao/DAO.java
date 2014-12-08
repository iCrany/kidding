package org.kidding.orm.dao;

import org.kidding.orm.entity.POJO;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by iCrany on 14/11/27.
 * dao 层的接口
 */
public interface DAO<T extends POJO> {

    /**
     * 更新，实体类中不为 null 的值
     * @param entity
     * @return
     */
    public Integer update(T entity);

    /**
     * 更新，实体类中的所有值都更新，包括为 null 的字段
     * @param entity
     * @return
     */
    public Integer forceUpdate(T entity);

    /**
     * 批量更新，
     * @param entityList
     * @return
     */
    public Integer batchUpdate(List<T> entityList);

    /**
     * 保存，实体类中不为 null 的值
     * @param entity
     * @return
     */
    public Long save(T entity);

    /**
     * 保存，实体类中的所有值都进行保存，包括为 null 的字段
     * @param entity
     * @return
     */
    public Long forceSave(T entity);

    /**
     * 批量保存，
     * @param entityList
     * @return
     */
    public Integer batchSave(List<T> entityList);


    /**
     * 删除，实体类中不为 null 的值作为条件
     * @param entity
     * @return
     */
    public Integer delete(T entity) throws SQLException;

    /**
     * 删除，删除表中的所有列表
     * @return 返回删除成功的数量
     */
    public Integer deleteAll();

    /**
     * 批量删除，
     * @param idList 需要删除的数据的主键列表
     * @return 返回删除成功的数量
     */
    public Integer batchDelete(List<Integer> idList);

    /**
     * 查询，实体类中不为 null 的值作为查询条件，并且只返回一个条目
     * @param entity 实体类
     * @param params 需要返回的列，请尽量填充，避免不必要的数据传输
     * @return
     */
    public T get(T entity,String... params);

    /**
     * 查询，实体类红不为 null 的值作为查询条件，并且返回一些列的条目
     * @param entity
     * @param condition 一些 entity 中无法表示的条件，例如 field < 11
     * @param orderBy 排序条件
     * @param groupBy 分组条件
     * @param params 需要返回的列，有选择的返回一些字段值
     * @return
     */
    public List<T> list(T entity , String condition , String orderBy , String groupBy ,String... params) throws SQLException, InstantiationException, IllegalAccessException, InvocationTargetException;

    /**
     * 分页查询，实体类中不为 null 的值作为查询条件，并且返回一些类的类目，大小为 <= pageSize
     * @param entity
     * @param curPage
     * @param pageSize
     * @param condition
     * @param orderBy
     * @param groupBy
     * @return
     */
    public List<T> list(T entity, Integer curPage , Integer pageSize , String condition , String orderBy , String groupBy,String... params) throws SQLException, InstantiationException, IllegalAccessException, InvocationTargetException;

    /**
     * 查询，查询该表中的所有数据
     * @return
     */
    public List<T> listAll();
}
