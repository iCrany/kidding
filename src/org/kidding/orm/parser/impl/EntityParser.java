package org.kidding.orm.parser.impl;

import org.kidding.orm.entity.POJO;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by iCrany on 14/11/28.
 * 一个解析 pojo 类的键值对，注意的是要处理 pojo 继承的父类的那些 属性 问题
 */
public class EntityParser<T extends POJO> {

    /**
     * 该实体的方法数组
     */
    private Method[] methods;

    /**
     * 该实体的属性数组
     */
    private Field[] fields;

    /**
     * 类的类型
     */
    private Class<T> classType;

    /**
     * methodName --> method
     */
    private Map<String,Method> methodMap;

    public EntityParser(Class<T> classType) throws IllegalAccessException, InstantiationException {
        this.classType = classType;

        T entity = classType.newInstance();
        methods = entity.getClass().getDeclaredMethods();
        fields = entity.getClass().getDeclaredFields();
        methodMap = new HashMap<String, Method>();

        for(Method method : methods){
            methodMap.put(method.getName(),method);
        }
    }

    /**
     * 获取实体类中得所有属性
     * @param entity 实体类
     * @return
     */
    public static <T> List<String> getAllKey(T entity){
        List<String> result = new ArrayList<String>();
        Method[] methods = entity.getClass().getDeclaredMethods();
        Field[] fields = entity.getClass().getDeclaredFields();
        Map<String,Method> methodMap = new HashMap<String, Method>();

        for(Method method : methods){
            methodMap.put(method.getName(),method);
        }

        for(Field field : fields){
            String key = field.getName();
            result.add(key);
        }

        return result;
    }

    /**
     * 获取构造该类的 实体类的 所有属性
     * @return
     */
    public List<String> getAllKey(){
        List<String> result = new ArrayList<String>();

        for(Field field : fields){
            result.add(field.getName());
        }
        return result;
    }

    /**
     * 获取实体类中除了主键的所有属性
     * @param entity 实体类
     * @return
     */
    public List<String> getAllKeyNoPk(T entity){
        List<String> result = getAllKey(entity);
        String pk = entity._primaryKey();

        if(null != pk && result.contains(pk)){
            result.remove(pk);
        }
        return result;
    }



    /**
     * 获取实体类中所有的 属性 --> 属性值 的键值对
     * @param entity 实体类
     * @param isForce null 值是否也需要
     * @return
     */
    public Map getAttrValueMap(T entity, Boolean isForce) throws InvocationTargetException, IllegalAccessException {
        Map<String,Object> result = new HashMap<String,Object>();
        Method[] methods = entity.getClass().getDeclaredMethods();
        Field[] fields = entity.getClass().getDeclaredFields();
        Map<String,Method> methodMap = new HashMap<String, Method>();

        for(Method method : methods){
            methodMap.put(method.getName(),method);
        }

        for(Field field : fields){
            String key = field.getName();
            Object value = null;

            String methodName = hasGetMethod(methods,key);
            if(null != methodName){
                value = methodMap.get(methodName).invoke(entity);
            }

            if(isForce){
                result.put(key,value);
            }else{
                if(null != value){
                    result.put(key,value);
                }
            }
        }
        return result;
    }

    /**
     * 获取实体类中的除了 primary key 属性的所有的 属性 --> 属性值 的键值对
     * @param entity 实体类
     * @param isForce null 值是否也需要
     * @return
     */
    public Map getAttrValueNoPkMap(T entity,Boolean isForce) throws InvocationTargetException, IllegalAccessException {
        Map<String , Object> map = getAttrValueMap(entity,isForce);
        String pk = entity._primaryKey();
        if(map.containsKey(pk))
            map.remove(pk);
        return map;
    }

    /**
     * 获取实体类中属性对应的类型 attrName --> classType
     * @param entity 对应的尸实体类
     * @return
     */
    public Map<String,Class> getAttrTypeMap(T entity){
        Map<String,Class> result = new HashMap<String, Class>();
        Field[] fields = entity.getClass().getDeclaredFields();

        for(Field field : fields){
            result.put(field.getName(),field.getType());
        }

        return result;
    }

    /**
     * 检测是否含有 getter 方法，该 kidding 项目利用 get 方法来获取数据
     * @param methods 实体类中的所有方法
     * @param fieldName 属性名
     * @return 有该方法则返回该方法的 name , 否则返回 null
     */
    public String hasGetMethod(Method[] methods , String fieldName){
        String getName = "get"+fieldName;
        for(Method method : methods){
            if(method.getName().equalsIgnoreCase(getName)){
                return method.getName();
            }
        }
        return null;
    }

    /**
     * 检测是否含有 setter 方法，该 kidding 项目利用 set 方法来获取数据
     * @param methods 实体类中的所有方法
     * @param fieldName 属性名
     * @return 有该方法则返回该方法的 name , 否则返回 null
     */
    public String hasSetMethod(Method[] methods , String fieldName){
        String setName = "set"+fieldName;
        for(Method method : methods){
            if(method.getName().equalsIgnoreCase(setName)){
                return method.getName();
            }
        }
        return null;
    }

    /**
     * 调用方法，设置更新值,暂时 boolean 类型只支持 set 的设置
     * @param key 属性名
     * @param value 新的属性值
     * @return 是否成功
     */
    public Object setMethodValue(T entity,String key , Object value) throws InvocationTargetException, IllegalAccessException {
        String setMethodName;

        if(value instanceof Boolean){
            setMethodName = "is" +  key.substring(0,1).toUpperCase() + key.substring(1);

            if(!methodMap.containsKey(setMethodName)){
                setMethodName = "set" + key.substring(0,1).toUpperCase() + key.substring(1);
            }

        }else{
            setMethodName = "set" + key.substring(0,1).toUpperCase() + key.substring(1);
        }

        Method method = methodMap.get(setMethodName);
        Object obj = method.invoke(entity,value);
        return obj;
    }

    /**
     * 获取实体类中的 某个属性值
     * @param entity 实体类
     * @param key 属性名
     * @return 返回该属性的值
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object getMethodValue(T entity , String key) throws InvocationTargetException, IllegalAccessException {
        String getMethodName;

        getMethodName = "get" + key.substring(0,1).toUpperCase() + key.substring(1);

        Method method = methodMap.get(getMethodName);
        Object obj = method.invoke(entity);
        return obj;
    }


}
