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
            "file TEXT,"+
            "owner TEXT);";

    public void connect(){
        try{
            String url="jdbc:sqlite:D:/"+fileName;
            connection= DriverManager.getConnection(url);
            System.out.println("Connection established");
        }
        catch (SQLException e){
            System.err.println("Cannot connect to a database");
        }
    }

    public void startDatabase(){
        if(connection!=null){
            try{
                Statement statement=connection.createStatement();
                statement.execute(startClientsTable);
                statement.execute(startFilesTable);
            }
            catch (SQLException e){
                System.err.println("Cannot start database");
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