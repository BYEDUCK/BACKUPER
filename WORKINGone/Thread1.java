import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

//import static sun.nio.ch.IOStatus.EOF;


public class Thread1 extends JFrame implements Runnable{

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


    public Thread1(int port) {
        transferPort=port;
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        setSize(new Dimension(500,100));
        add(progressBar);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });
        setVisible(true);
    }

    private void connectToDatabase(){
        mDatabase=new MyDatabase();
        connection=mDatabase.connect();
        mDatabase.startDatabase();
        ResultSet all=mDatabase.selectViaSQL("SELECT login, password FROM clients;");
        try{
            while(all.next())
                System.out.println(all.getString(1)+"; "+all.getString(2)+".\n");
        }
        catch (SQLException e){
            System.err.println(e);
        }
    }

    private boolean logIn(){
        try{
            String login;
            String password;
            String passwordCheck="";
            do{
                if(transferSocket==null)
                    transferSocket = new ServerSocket(transferPort);
                socket=transferSocket.accept();
                out=new PrintWriter(socket.getOutputStream(),true);
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                login=bufferedReader.readLine();
                password=bufferedReader.readLine();
                ResultSet check=mDatabase.selectViaSQL("SELECT password FROM clients WHERE login='"+login+"';");
                if(check.next())
                    passwordCheck=check.getString(1);
                if(!password.equals(passwordCheck)){
                    System.out.println("Nie udało się zalogować!");
                    out.println(0);
                    transferSocket.close();
                    transferSocket=null;
                }
            }while(!password.equals(passwordCheck));
            System.out.println("Zalogowano!");
            out.println(1);
            mDatabase.closeConnection();
            connection=null;
            return true;
        }
        catch (IOException e){
            System.err.println("Network error!: "+e);
            return false;
        }
        catch (SQLException e1){
            System.err.println("Database error!:"+e1);
            return false;
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
            connectToDatabase();
            if(logIn()) {
                transferSocket.close();
                transferSocket=null;
                transferSocket=new ServerSocket(transferPort);
                socket = transferSocket.accept();
                filesData = new ArrayList<>();
                inputStream = socket.getInputStream();
                bufferedInputStream = new BufferedInputStream(inputStream);
                out = new PrintWriter(socket.getOutputStream(), true);
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                howmany = Integer.parseInt(bufferedReader.readLine());
                System.out.println(howmany);
                for (int i = 0; i < howmany; i++) {
                    int fileLngth = Integer.parseInt(bufferedReader.readLine());
                    String fileNm = bufferedReader.readLine();
                    filesData.add(new FileMetaData(Server.path + fileNm, fileLngth));
                    System.out.println(filesData);
                }
                out.println(Server.ready);
                System.out.println(Server.ready);
                for (int i = 0; i < howmany; i++) {
                    fileLength = filesData.get(i).getFileLength();
                    filePath = filesData.get(i).getFilePath();
                    progressBar.setMinimum(0);
                    progressBar.setMaximum(fileLength);
                    progressBar.setValue(0);
                    byte[] fileBytes = new byte[fileLength];
                    int offset = 0;
                    int read;
                    while (offset < fileLength && (read = bufferedInputStream.read(fileBytes, offset, fileLength - offset)) != -1) {
                        offset += read;
                        progressBar.setValue(offset);
                    }
                    System.out.println("Wczytano bajtów: " + offset + "/" + fileLength);
                    File fileOut = new File(filePath);
                    FileOutputStream outputLocal = new FileOutputStream(fileOut);
                    outputLocal.write(fileBytes, 0, fileLength);
                    outputLocal.flush();
                    out.println(Server.ready);
                    System.out.println(Server.ready);
                }
                out.println(end);
                socket.close();
            }
            else{
                System.out.println("Failed to log in!");
            }
        }
        catch (IOException e){
            System.err.println(e);
        }
    }
}

