import javax.naming.spi.DirectoryManager;
import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;

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
            } else
                loggedIn = true;
            System.out.println("Zalogowano!");
            //setVisible(true);
            out.println(MyProtocol.LOGGEDIN);
            mDatabase.closeConnection();
            connection = null;
            return true;
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
            ResultSet set=mDatabase.selectViaSQL("SELECT login FROM clients;");
            try {
                while (set.next())
                    users.add(set.getString(1));
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
                        Path="E:\\"+userNameActive;
                        if(Files.notExists(Paths.get(Path)))
                            Files.createDirectories(Paths.get(Path));
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
                        for (int i = 0; i < howmany; i++) {
                            int fileLngth = Integer.parseInt(bufferedReader.readLine());
                            String fileNm = bufferedReader.readLine();
                            filesData.add(new FileMetaData(Path + "\\" + fileNm, fileLngth));
                            System.out.println(filesData);
                        }
                        out.println(Server.ready);
                        System.out.println(Server.ready);
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
                            out.println(Server.ready);
                            System.out.println(Server.ready);
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
                        out.println(Server.ready);
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
