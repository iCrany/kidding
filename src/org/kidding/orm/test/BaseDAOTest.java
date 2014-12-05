package org.kidding.orm.test;

import org.kidding.orm.dao.imp.BaseDAO;
import org.kidding.orm.db.DBManager;
import org.kidding.orm.entity.POJO;
import org.kidding.orm.parser.SqlParser;
import org.kidding.orm.parser.impl.EntityParser;
import org.kidding.orm.parser.impl.MySqlParser;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Created by iCrany on 14/11/29.
 */
public class BaseDAOTest {
    public static void main(String[] args) throws IllegalAccessException, SQLException, InvocationTargetException, InstantiationException {
        POJOTest entity = new POJOTest();
        entity.setName("li2");
//        entity.setLastName("wu");
//        entity.setAddress(new String("guang zhou"));
//        entity.setCreateTime(new Timestamp(new Date().getTime()));
//        entity.setSex(new String("man"));

        DBManager.init("mysql","druid.properties");
        EntityParser<POJOTest> entityParser = new EntityParser<POJOTest>(POJOTest.class);
        SqlParser<POJOTest> sqlParser = new MySqlParser<POJOTest>(entityParser);
        BaseDAO<POJOTest> dao = new BaseDAO<POJOTest>(sqlParser,"mysql",POJOTest.class);
//        Long id = dao.list(entity);
        List<POJOTest> list = dao.list(entity,0,2,null,"createTime desc",null);

        System.out.println("size = " + list.size());
        for(POJOTest pojo : list){
            System.out.println("pojo.name = " + pojo.getName() + " createTime = " + pojo.getCreateTime());
        }

    }
}
