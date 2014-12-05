package org.kidding.orm.test;

import org.kidding.orm.parser.impl.EntityParser;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by iCrany on 14/11/28.
 */
public class EntityParserTest {
    public static void main(String[] args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        EntityParser<POJOTest> tt = new EntityParser<POJOTest>(POJOTest.class);
        POJOTest pojo = new POJOTest();
        pojo.setId(123);
        pojo.setLastName("li");
        pojo.setName("shan");

        Field[] fields = pojo.getClass().getDeclaredFields();
        Method[] methods = pojo.getClass().getDeclaredMethods();

        Map<String,Method> methodMap = new HashMap<String,Method>();
        for(Method method : methods){
            methodMap.put(method.getName(),method);
        }

        System.out.println("fields size = " + fields.length);
        for(Field field : fields){
            field.setAccessible(true);
            String methodName = tt.hasGetMethod(methods,field.getName());
            if(null != methodName){
                Method method = methodMap.get(methodName);
                System.out.println("FieldName : " + field.getName() + "  = " + method.invoke(pojo));
            }else {
                System.out.println("FieldName : " + field.getName() + "  ");
            }
        }

        for(Method method: methods){
            System.out.println("MethodName : " + method.getName());
        }

        tt.getAttrValueMap(pojo, true);
    }
}
