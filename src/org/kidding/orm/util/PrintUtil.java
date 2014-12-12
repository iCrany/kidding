package org.kidding.orm.util;

import org.apache.log4j.Logger;
import org.kidding.orm.entity.POJO;
import org.kidding.orm.parser.impl.EntityParser;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by iCrany on 14/12/11.
 * 输出 vo 属性工具类，方便测试
 */
public class PrintUtil<T extends POJO> {

    private static Logger logger = Logger.getLogger(PrintUtil.class);

    private Class<T> classType;

    private EntityParser<T> entityParser;

    public PrintUtil(Class<T> classType){
        this.classType = classType;
        try{
            this.entityParser = new EntityParser<T>(this.classType);
        }catch(Exception e){
            logger.error("fail to new PrintUtil class!!!",e);
        }

    }

    /**
     * 输出 vo 类的 属性名
     */
    public void printKey(){
        List<String> allKey = entityParser.getAllKey();

        for(String key : allKey){
            System.out.println(key);
        }
    }

    /**
     * 输出 vo 类的 键值对
     * @param entity
     * @param isForce null 值是否输出
     */
    public void printKeyValue(T entity , Boolean isForce){
        try {
            Map<String , Object> map = entityParser.getAttrValueNoPkMap(entity,isForce);
            Set<String> keySet = map.keySet();

            for(String key : keySet){
                System.out.println(key + " : " + map.get(key));
            }
        } catch (Exception e) {
            logger.error("fail to print vo keyValue");
        }
    }
}
