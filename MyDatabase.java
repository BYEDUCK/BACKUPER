package BACKUPER;
import java.sql.*;

public class MyDatabase {
    private  Connection connection;
    private static String fileName="backuperDatabase.db";
    private final static String startClientsTable="CREATE TABLE IF NOT EXISTS clients(" +
            "id INTEGER PRIMARY KEY,"+
            "login TEXT,"+
            "password TEXT);";

    public Connection connect(){
        try{
            String url="jdbc:sqlite:E:/"+fileName;
            connection= DriverManager.getConnection(url);
            System.out.println("Connection established");
            return connection;
        }
        catch (SQLException e){
            System.err.println("Cannot connect to a database");
            return null;
        }
    }

    public void startDatabase(){
        if(connection!=null){
            try{
                Statement statement=connection.createStatement();
                statement.execute(startClientsTable);
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
