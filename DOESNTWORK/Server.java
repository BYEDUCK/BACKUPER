import javax.swing.*;
import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;


public class Server extends JFrame{


    public static final String ready = "READY";
    public static String path = "E:\\BackuperKopie\\";
    private static BufferedReader reader;
    private static OutputStream outputStream;
    private static PrintWriter printWriter;
    private static Random random;
    public static int transferPort;
    private static Connection connection;


    public static void main(String[] args) {
        MyDatabase mDatabase=new MyDatabase();
        connection=mDatabase.connect();
        mDatabase.startDatabase();
        mDatabase.newUser("Mateusz","123");
        mDatabase.newUser("Miłosz","456");
        ResultSet all=mDatabase.selectViaSQL("SELECT login, password FROM clients;");
        try {
            while (all.next()) {
                System.out.println(all.getString(1)+": "+all.getString(2));
            }
        }
        catch (SQLException e){
            System.err.println(e);
        }
        while (true) {
            ServerSocket serverSocket = null;
            Socket setterSocket=null;
            try {
                serverSocket = new ServerSocket(12129);
            } catch (Exception e){
                System.err.println("Create new meta socket error: " +e);
            }
            while(true) {
                try {
                    String login;
                    String password;
                    String passwordCheck;
                    do {
                        setterSocket = serverSocket.accept();
                        outputStream = setterSocket.getOutputStream();
                        reader=new BufferedReader(new InputStreamReader(setterSocket.getInputStream()));
                        printWriter = new PrintWriter(outputStream, true);
                        login = reader.readLine();
                        password = reader.readLine();
                        passwordCheck = "";
                        ResultSet check = mDatabase.selectViaSQL("SELECT login,password FROM clients WHERE login='" + login + "';");
                        if (check.next())
                            passwordCheck = check.getString(2);

                        if(!password.equals(passwordCheck)) {
                            System.out.println("Nie udało się zalogować!");
                            printWriter.println(0);
                            setterSocket.close();
                        }
                    }while(!password.equals(passwordCheck));
                    System.out.println("Zalogowano!");
                    printWriter.println(1);
                    mDatabase.closeConnection();
                    connection = null;
                    setterSocket.close();
                    setterSocket=serverSocket.accept();
                    printWriter=new PrintWriter(setterSocket.getOutputStream());
                    random = new Random();
                    transferPort = random.nextInt(65535);
                    while (isAvailable(transferPort) == false) {
                        transferPort = random.nextInt(65535);
                    }
                    printWriter.println(transferPort);
                    Runnable runnable = new Thread1();
                    Thread transferThread = new Thread(runnable);
                    transferThread.start();
                    setterSocket.close();
                } catch (Exception e) {
                    System.out.println("Creating meta socket problem: " + e);
                    //printWriter.print(0);
                }
            }
        }
    }

    public static boolean isAvailable(int port) { //coś takiego radzili zrobić na Stack Overflow
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }
}