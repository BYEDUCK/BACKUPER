package Working;

import javax.naming.spi.DirectoryManager;
import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MyThread implements Runnable {

    private static Socket socket = null;
    private InputStream inputStream;
    private BufferedInputStream bufferedInputStream;
    private PrintWriter out;
    private BufferedReader bufferedReader;
    private static int fileLength;
    private static String filePath;
    private static int howmany;
    private static ArrayList<FileMetaData> filesData;
    private static JProgressBar progressBar;
    public static final String end = "END";
    private Connection connection;
    private int transferPort;
    ServerSocket transferSocket = null;
    private MyDatabase mDatabase;
    private String userNameActive;
    private String Path;
    private boolean loggedIn=false;
    private boolean alreadyExists=false;
    private ArrayList<String> users;
    private Vector <String> filesNames = new Vector<>();
    private List<String> filesTitles = new ArrayList<>();
    private PrintWriter save;

    private void connectToDatabase(){
        mDatabase=new MyDatabase();
        connection=mDatabase.connect();
        mDatabase.startDatabase();
        /*mDatabase.newUser("Andrzej","password");
        mDatabase.newUser("Grażyna","password123");
        mDatabase.newUser("Mateusz","12345");*/
    }

    public MyThread(int port){
        this.transferPort=port;
    }

    private boolean logIn(){
        try{
            String password;
            String passwordCheck = "";
            userNameActive = bufferedReader.readLine();
            password = bufferedReader.readLine();
            ResultSet check = mDatabase.selectViaSQL("SELECT password FROM clients WHERE login='" + userNameActive + "';");
            if (check.next())
                passwordCheck = check.getString(1);
            if (!password.equals(passwordCheck)) {
                System.out.println("Nie udało się zalogować!");
                out.println(MyProtocol.FAILED);
                return false;
            } else {
                loggedIn = true;
                System.out.println("Zalogowano!");
                //setVisible(true);
                out.println(MyProtocol.LOGGEDIN);
                mDatabase.closeConnection();
                connection = null;
                return true;
            }
        }
        catch (IOException e){
            System.err.println("Network error!: "+e);
            out.println(MyProtocol.FAILED);
            return false;
        }
        catch (SQLException e1){
            System.err.println("Database error!:"+e1);
            out.println(MyProtocol.FAILED);
            return false;
        }
    }


    private void initilize(){
        try {
            inputStream = socket.getInputStream();
            bufferedInputStream=new BufferedInputStream(inputStream);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out=new PrintWriter(socket.getOutputStream(),true);
            connectToDatabase();
            users=new ArrayList<>();
            ResultSet set=mDatabase.selectViaSQL("SELECT login,password FROM clients;");
            try {
                while (set.next()) {
                    users.add(set.getString(1));
                    System.out.println(set.getString(1)+":"+set.getString(2)+".");
                }
            }
            catch (SQLException e){
                System.err.println("Cannot read from database: "+e);
            }
            System.out.println("Liczba użytkowników: "+users.size());
        }
        catch (IOException e){
            System.err.println(e);
        }
    }

    private String receive(){
        try {
            return bufferedReader.readLine();
        }
        catch (IOException e){
            return null;
        }
    }
    public void sendTitles() {
        Path path = Paths.get(Path + "\\" + "container" + userNameActive + ".txt");
        try {
            filesTitles = Files.readAllLines(path);
            out.println(filesTitles.size());
            System.out.println(filesTitles.size());
            for (int i = 0; i < filesTitles.size(); i++) {
                out.println(filesTitles.get(i));
                System.out.println(filesTitles.get(i));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {
            transferSocket = new ServerSocket(transferPort);
        } catch (Exception e) {
            System.err.println("Server socket for client creating problem: " + e);
        }
        try {
            socket = transferSocket.accept();
            initilize();
            while(true) {
                alreadyExists=false;
                String request = receive();
                /*StringTokenizer st = new StringTokenizer(request);
                String command = st.nextToken();*/
                if(request.equals(MyProtocol.LOGIN)){
                    if(logIn()){
                        Path="C:\\Users\\mpars\\Desktop\\BackuperKopie\\"+userNameActive;
                        if(Files.notExists(Paths.get(Path)))
                            Files.createDirectories(Paths.get(Path));
                        if(Files.exists(Paths.get(Path + "\\" + "container" + userNameActive + ".txt"))) {
                            out.println(MyProtocol.FILEEXIST);
                            sendTitles();
                        }
                        else
                            out.println(MyProtocol.NOSUCHFILE);
                    }
                }
                else if(request.equals(MyProtocol.SENDFILE)){
                    try {
                        filesData = new ArrayList<>();
                        //inputStream = socket.getInputStream();
                        //bufferedInputStream = new BufferedInputStream(inputStream);
                        //out = new PrintWriter(socket.getOutputStream(), true);
                        //bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        howmany = Integer.parseInt(bufferedReader.readLine());
                        System.out.println("Ilość plików do pobrania: "+howmany);
                        save = new PrintWriter(Path+"\\"+"container"+userNameActive+".txt");
                        for (int i = 0; i < howmany; i++) {
                            int fileLngth = Integer.parseInt(bufferedReader.readLine());
                            String fileNm = bufferedReader.readLine();
                            filesNames.add(fileNm);
                            save.println(fileNm);
                            filesData.add(new FileMetaData(Path + "\\" + fileNm, fileLngth));
                            System.out.println(filesData);
                        }
                        /*PrintWriter save = new PrintWriter(Path+"\\"+"container"+userNameActive+".txt");
                        save.println(filesNames);*/
                        for(int i = 0;i<filesTitles.size();i++) {
                            save.println(filesTitles.get(i));
                        }
                        save.close();
                        out.println(MyServer.ready);
                        System.out.println(MyServer.ready);
                        for (int i = 0; i < howmany; i++) {
                            fileLength = filesData.get(i).getFileLength();
                            filePath = filesData.get(i).getFilePath();
                            //progressBar.setMinimum(0);
                            //progressBar.setMaximum(fileLength);
                            //progressBar.setValue(0);
                            byte[] fileBytes = new byte[fileLength];
                            int offset = 0;
                            int read;
                            while (offset < fileLength && (read = bufferedInputStream.read(fileBytes, offset, fileLength - offset)) != -1) {
                                offset += read;
                                //progressBar.setValue(offset);
                            }
                            System.out.println("Wczytano bajtów: " + offset + "/" + fileLength);
                            File fileOut = new File(filePath);
                            FileOutputStream outputLocal = new FileOutputStream(fileOut);
                            outputLocal.write(fileBytes, 0, fileLength);
                            outputLocal.flush();
                            out.println(MyServer.ready);
                            System.out.println(MyServer.ready);
                        }
                    }
                    catch (IOException e){
                        System.err.println(e);
                        out.println(MyProtocol.FAILED);
                    }
                }
                else if(request.equals(MyProtocol.NEWUSER)){
                    //out.println(MyProtocol.OK);
                    String newName=bufferedReader.readLine();
                    String newPassword=bufferedReader.readLine();
                    for (String name:users
                            ) {
                        if(newName.equals(name)) {
                            alreadyExists=true;
                            out.println(MyProtocol.FAILED);
                        }
                    }
                    if(!alreadyExists) {
                        mDatabase.newUser(newName, newPassword);
                        out.println(MyServer.ready);
                    }
                }
                else if(request.equals(MyProtocol.LOGOUT)){

                }
            }
        }

        catch (Exception e){
            System.err.println(e);
        }
    }

}