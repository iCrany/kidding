package org.kidding.orm.dao;

import org.kidding.orm.entity.POJO;

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
    public int update(T entity);

    /**
     * 更新，实体类中的所有值都更新，包括为 null 的字段
     * @param entity
     * @return
     */
    public int fourceUdate(T entity);

    public int svae(T entity);

    public int delete(T entity);

    public T get(T entity);
}
