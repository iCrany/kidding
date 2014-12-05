package org.kidding.orm.db;


import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.apache.log4j.Logger;
import org.kidding.orm.exception.DataSourceInitException;
import org.kidding.orm.util.EmptyUtil;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by iCrany on 14/11/26.
 * 管理数据库连接池
 */
public class DBManager {
    private static Logger logger = Logger.getLogger(DBManager.class);

    private static Map<String,DataSource> dataSourceMap ;// alias --> DataSource

    public DBManager() {
    }

    /**
     * 初始化数据库连接池
     * @param alias 别名
     * @param configName 数据库配置文件名
     * @return
     */
    public static Boolean init(String alias , String configName){
        if(null == alias || null == configName ){
            throw new NullPointerException("not define alias or configName!");
        }

        try{
            InputStream in = DBManager.class.getResourceAsStream("/"+configName);
            Properties pro = new Properties();
            pro.load(in);
            injectDataSource(alias, DruidDataSourceFactory.createDataSource(pro));
        }catch(IOException e){
            logger.error("fail to load the config file!",e);
        }catch(Exception e){
            logger.error("fail to init druidDataSource!",e);
        }

        return true;
    }

    /**
     * 注入数据库
     * @param alias
     * @param dataSource
     */
    public static void injectDataSource(String alias,DataSource dataSource){
        if(null == dataSourceMap){
            dataSourceMap = new ConcurrentHashMap<String, DataSource>();
        }

        dataSourceMap.put(alias,dataSource);
    }

    /**
     * 从连接池中获取数据库连接
     * @param alias
     * @return
     * @throws SQLException
     */
    public static Connection getConnection(String alias) throws SQLException{
        DataSource dataSource = dataSourceMap.get(alias);
        if(null == dataSource){
            throw new DataSourceInitException("fail init this dataSource : " + alias);
        }
        return dataSource.getConnection();
    }

    public static void close(Connection conn){
        try {
            if (null != conn && !conn.isClosed()) {
                conn.close();
            }
        }catch (SQLException e){
            logger.error("fail to close this connection!",e);
        }
    }

    public static void close(PreparedStatement pstmt){
        try{
            if(null != pstmt && !pstmt.isClosed()){
                pstmt.close();
            }
        }catch(SQLException e){
            logger.error("fail to close this preparedStatment!",e);
        }
    }

    public static void close(Statement stmt){
        try{
            if(null != stmt && !stmt.isClosed()){
                stmt.close();
            }
        }catch(SQLException e){
            logger.error("fail to close this statement!",e);
        }
    }

    public static void close(ResultSet rs){
        try{
            if(null != rs && !rs.isClosed()){
                rs.close();
            }
        }catch(SQLException e){
            logger.error("fail to close this resultSet!",e);
        }
    }
}
