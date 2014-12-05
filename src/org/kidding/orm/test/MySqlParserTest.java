package org.kidding.orm.test;

import org.kidding.orm.db.DBManager;
import org.kidding.orm.entity.POJO;
import org.kidding.orm.parser.impl.EntityParser;
import org.kidding.orm.parser.impl.MySqlParser;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by iCrany on 14/11/28.
 */
public class MySqlParserTest {
    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        POJOTest entity = new POJOTest();
        entity.setName("li2");
        entity.setLastName("wu");
        entity.setAddress(new String("guang zhou"));
        entity.setCreateTime(new Timestamp(new Date().getTime()));
        entity.setSex(new String("man"));
        entity.setId(2);

        EntityParser<POJOTest> entityParser = new EntityParser<POJOTest>(POJOTest.class);
        MySqlParser<POJOTest> mySqlParser = new MySqlParser<POJOTest>(entityParser);

        String sqlTemplate = mySqlParser.query(entity,true,"name != 'li2'",2,20,"ORDER BY desc",null);
//        Connection con = DBManager.getConnection();
    }
}
