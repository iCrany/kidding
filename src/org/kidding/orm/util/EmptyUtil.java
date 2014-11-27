package org.kidding.orm.util;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Created by iCrany on 14/11/26.
 * 一个用于检测是否为空的类
 */
public class EmptyUtil {

    public static Boolean isEmpty(Integer entity){
        return entity == null;
    }

    public static Boolean isEmpty(Long entity){
        return entity == null;
    }

    public static Boolean isEmpty(Double entity){
        return entity == null;
    }

    public static Boolean isEmpty(Float entity){
        return entity == null;
    }

    public static Boolean isEmpey(String entity){
        return entity == null;
    }

    public static Boolean isEmpty(Byte entity){
        return entity == null;
    }

    public static Boolean isEmpty(List<?> entity){
        return entity == null || entity.size() == 0;
    }

    public static Boolean isEmpty(Collection<?> entity){
        return entity == null || entity.size() == 0;
    }

    public static Boolean isEmpty(Properties entity){
        return entity == null;
    }
}
