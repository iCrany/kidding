package org.kidding.orm.parser.impl;

import org.kidding.orm.entity.POJO;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by iCrany on 14/12/3.
 * sql 查询结果解析器
 */
public class ResultSetParser<T extends POJO> {

    /**
     * 类类型
     */
    private Class<T> classType;

    /**
     * 实体类解析器
     */
    private EntityParser<T> entityParser;

    public ResultSetParser(Class<T> classType) throws InstantiationException, IllegalAccessException {
        this.classType = classType;
        this.entityParser = new EntityParser<T>(classType);
    }

    public ResultSetParser(EntityParser entityParser,Class<T> classType) throws InstantiationException, IllegalAccessException {
        this.classType = classType;
        this.entityParser = entityParser;
    }

    /**
     * 将 resultSet 对象中的数据组织成 List<T> 形式返回
     * @param rs 结果对象集
     * @return
     * @throws SQLException
     */
    public List<T> parseList(Class<T> classType , ResultSet rs) throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException {
        List<T> result = new ArrayList<T>();
        List<String> keyList = entityParser.getAllKey(classType.newInstance());
        System.out.println("正在 parseList ... ");
        while(rs.next()){
            T entity = classType.newInstance();

            for(String key : keyList){
                if(null != rs.getObject(key)){
                    Object value = rs.getObject(key);
                    entityParser.setMethodValue(entity,key,value);
                }
            }
            result.add(entity);
        }
        return result;
    }

    /**
     * 获取单个的实体类
     * @param classType 类的类型
     * @param rs 结果集合
     * @return
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public T parse(Class<T> classType, ResultSet rs) throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException {
        T entity = classType.newInstance();
        List<String> keyList = entityParser.getAllKey(entity);

        if(rs.next()){
            for(String key : keyList){
                if(null != rs.getObject(key)){
                    Object value = rs.getObject(key);
                    entityParser.setMethodValue(entity,key,value);
                }
            }
        }
        return entity;
    }

}
