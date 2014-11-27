package org.kidding.orm.test;

/**
 * Created by iCrany on 14/11/27.
 */
public class MainTest {
    public static void main(String[] args) {
        POJOTest test = new POJOTest();
        System.out.println(test._tableName());
        System.out.println("pk = " + test._primaryKey());
    }
}
