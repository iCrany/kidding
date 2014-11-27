package org.kidding.orm.exception;

import java.sql.SQLException;

/**
 * Created by iCrany on 14/11/26.
 * 数据库初始化异常类，提示用户数据库没有初始化
 */
public class DataSourceInitException extends SQLException{

    private String message;

    public DataSourceInitException(){
        super();
    }

    public DataSourceInitException(String message){

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }
}
