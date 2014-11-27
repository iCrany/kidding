package org.kidding.orm.entity;

import com.sun.tools.javac.util.Name;
import com.sun.xml.internal.bind.v2.model.core.ID;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by iCrany on 14/11/26.
 */
public class POJO implements Serializable{

    /**
     * 获取实体类对应数据库的名称，使用 @Table 来进行标示
     * @return
     */
    public String _tableName(){
        Table table = this.getClass().getAnnotation(Table.class);
        if(null == table || table.name().isEmpty()){
            throw new RuntimeException("undefine pojo tableName!");
        }
        return table.name();
    }

    /**
     * 获取实体类的对应数据库表的主键 @Id,这里暂时不支持复合主键，若标注了复合主键，以第一个找到的为基准
     * @return
     */
    public String _primaryKey () throws RuntimeException{
        String primaryKey = null;
        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field : fields){
            if(field.isAnnotationPresent(Id.class)){
                primaryKey = field.getName();
                break;
            }
        }

        if(null == primaryKey || primaryKey.isEmpty()){
            throw new RuntimeException("undefine pojo id!");
        }
        return primaryKey;
    }

}
