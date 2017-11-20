package BACKUPER;

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

public class MyThread /*extends JFrame*/ implements Runnable {

    private static Socket socket = null;
    private InputStream inputStream;
    private BufferedInputStream bufferedInputStream;
    private PrintWriter out;
    private BufferedReader bufferedReader;
    private static int fileLength;
    private static String filePath;
    private static int howmany;
    private static ArrayList<FileMetaData> filesData;
    //private static JProgressBar progressBar;
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
        mDatabase.connect();
        mDatabase.startDatabase();
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
                out.println(MyProtocol.LOGGEDIN);
                mDatabase.closeConnection();
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
            //progressBar=new JProgressBar();
            //add(progressBar);
            //setVisible(false);
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
                if(request.equals(MyProtocol.LOGIN)){
                    if(logIn()){
                        if(Files.notExists(Paths.get("D:\\BackuperKopie")))
                            Files.createDirectory(Paths.get("D:\\BackuperKopie"));
                        Path="D:\\BackuperKopie\\"+userNameActive;
                        if(Files.notExists(Paths.get(Path)))
                            Files.createDirectory(Paths.get(Path));
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
                        //setVisible(true);
                        filesData = new ArrayList<>();
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
                        for(int i = 0;i<filesTitles.size();i++) {
                            save.println(filesTitles.get(i));
                        }
                        save.close();
                        out.println(MyProtocol.READY);
                        System.out.println(MyProtocol.READY);
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
                            out.println(MyProtocol.READY);
                            System.out.println(MyProtocol.READY);
                        }
                    }
                    catch (IOException e){
                        System.err.println(e);
                        out.println(MyProtocol.FAILED);
                    }
                }
                else if(request.equals(MyProtocol.NEWUSER)){
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
                        out.println(MyProtocol.READY);
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