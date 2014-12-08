package org.kidding.orm.util;

import java.util.List;

/**
 * Created by iCrany on 14/12/8.
 * 字符串工具类
 */
public class StringUtil {

    /**
     * 以 delimiter 为分隔符将 idList 中得数据连接起来
     * @param idList 需要进行连接的数据
     * @param delimiter 分隔符
     * @return
     */
    public static String join(List<Integer> idList , String delimiter){
        StringBuilder result = new StringBuilder();
        for(Integer id : idList){
            result.append(id + delimiter + " ");
        }

        result.setCharAt(result.lastIndexOf(delimiter),' ');

        return result.toString();
    }
}
