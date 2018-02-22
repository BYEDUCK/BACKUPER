package BACKUPER;

import java.sql.*;

public class MyDatabase {
    private  Connection connection;
    private static String fileName="backuperDatabase.db";
    private final static String startClientsTable="CREATE TABLE IF NOT EXISTS clients(" +
            "id INTEGER PRIMARY KEY,"+
            "login TEXT,"+
            "password TEXT);";
    private final static String startFilesTable = "CREATE TABLE IF NOT EXISTS files (" +
            "id INTEGER PRIMARY KEY,"+
            "name TEXT,"+
            "length INTEGER,"+
            "path TEXT," +
            "version INTEGER);";

    public void connect(String path){
        try{
            String url=path+fileName;
            connection= DriverManager.getConnection(url);
            System.out.println("Connection established");
        }
        catch (SQLException e){
            System.err.println("Cannot connect to a database");
        }
    }

    public void startDatabase(int flag){
        if(flag==0) {
            if (connection != null) {
                try {
                    Statement statement = connection.createStatement();
                    statement.execute(startClientsTable);
                } catch (SQLException e) {
                    System.err.println("Cannot start database");
                }
            }
        }
        else if(flag==1){
            if (connection != null) {
                try {
                    Statement statement = connection.createStatement();
                    statement.execute(startFilesTable);
                } catch (SQLException e) {
                    System.err.println("Cannot start database");
                }
            }
        }
    }

    public ResultSet selectViaSQL(String sql){
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(sql);
        }
        catch (SQLException e){
            System.err.println("Cannot find data");
            return null;
        }
    }

    public void newFile(String name,long length, String path,int version){
        String sql="INSERT INTO files(name,length,path,version) VALUES(?,?,?,?)";
        try{
            PreparedStatement statement=connection.prepareStatement(sql);
            statement.setString(1,name);
            statement.setLong(2,length);
            statement.setString(3,path);
            statement.setInt(4,version);
            statement.executeUpdate();
        }
        catch (SQLException e){
            System.err.println(e);
        }
    }

    public void newUser(String userName,String userPassword){
        String sql="INSERT INTO clients(login,password) VALUES(?,?)";
        try{
            PreparedStatement statement=connection.prepareStatement(sql);
            statement.setString(1,userName);
            statement.setString(2,userPassword);
            statement.executeUpdate();
        }
        catch (SQLException e){
            System.out.println("New user added!: "+userName+".");
            System.err.println(e);
        }
    }


    public void closeConnection(){
        if(connection!=null){
            try{
                connection.close();
            }
            catch (SQLException e){
                System.err.println("Cannot close the connection");
            }
            finally {
                connection=null;
            }
        }
    }
}