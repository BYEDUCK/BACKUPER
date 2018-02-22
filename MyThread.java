package BACKUPER;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MyThread implements Runnable {

    protected static Socket socket = null;
    private InputStream inputStream;
    private BufferedInputStream bufferedInputStream;
    private PrintWriter out;
    private BufferedReader bufferedReader;
    private static long fileLength;
    private static String filePath;
    private static int howmany;//Amount of files to be sent
    private static ArrayList<FileMetaData> filesData;
    private int transferPort;
    protected static ServerSocket transferSocket = null;
    private MyDatabase mDatabase;
    private MyDatabase mLocalDatabase;
    private String userNameActive;
    private String Path;
    private int fileVersion;
    private String fileName;
    private boolean loggedIn=false;
    private boolean alreadyExists=false;
    private ArrayList<String> users;
    private Vector <String> filesNames = new Vector<>();
    private List<String> filesTitles = new ArrayList<>();
    private PrintWriter save;
    private OutputStream outputStream;
    private final static int bufferSize=16384;//Default buffer-size 16kB


    private void connectToDatabase(){
        try {
            if (Files.notExists(Paths.get("E:\\BackuperData")))
                Files.createDirectory(Paths.get("E:\\BackuperData"));
        }
        catch (IOException e){
            System.err.println(e);
        }
        mDatabase=new MyDatabase();
        mDatabase.connect("jdbc:sqlite:E:/BackuperData/");
        mDatabase.startDatabase(0);
    }

    private void connectToLocalDatabase(String url){
        mLocalDatabase=new MyDatabase();
        mLocalDatabase.connect(url);
        mLocalDatabase.startDatabase(1);
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
            if (check.next())//If user was found download correct password from database
                passwordCheck = check.getString(1);
            if (!password.equals(passwordCheck)) {
                System.out.println("Nie udało się zalogować!");
                out.println(MyProtocol.FAILED);
                return false;//If given password doesn't match, login unsuccessful
            } else {
                loggedIn = true;
                System.out.println("Zalogowano!: "+userNameActive);
                out.println(MyProtocol.LOGGEDIN);
                MyServer.clientsList.addElement(userNameActive);
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
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
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
            System.err.println("Cannot initilize app:"+e);
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
    public void sendTitles() {//Send client files that are on the server (written in the container)
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

    private String separateFromVersion(String filePath){//Deletes version from file-name in order to be comparable
        StringBuilder builder=new StringBuilder(filePath);
        int i=filePath.length()-1;
        while(filePath.charAt(i)!='_'){
            builder.deleteCharAt(i);
            i--;
        }
        builder.deleteCharAt(i);
        return builder.toString();
    }

    private String[] separateNameFromExtension(String fileName){//Deletes extension from file-name in order to be comparable
        String[] wynik=new String[2];
        StringBuilder builder=new StringBuilder(fileName);
        StringBuilder ext=new StringBuilder();
        int i=fileName.length()-1;
        while(fileName.charAt(i)!='.'){
            ext.append(fileName.charAt(i));
            builder.deleteCharAt(i);
            i--;
        }
        builder.deleteCharAt(i);
        wynik[0]=builder.toString();
        wynik[1]=ext.reverse().toString();
        return wynik;
    }

    private String getFileName(String filePath){//Recieves file-name from full path
        char tmp;
        int i = 0;
        String fileName_tmp="";
        int file_src_length = filePath.length();
        while((tmp = filePath.charAt(file_src_length-1-i))!='\\' || (tmp = filePath.charAt(file_src_length-1-i))!='\\') {
            fileName_tmp += tmp;
            i++;
        }
        StringBuilder builder=new StringBuilder(fileName_tmp);
        return builder.reverse().toString();
    }

    private void restoreFiles (OutputStream outputStream) {
        try {
            String fileName = receive();
            String pathToSave="";
            System.out.println(fileName);
            ResultSet set=mLocalDatabase.selectViaSQL("SELECT path FROM files WHERE name='"+fileName+"';");
            if(set.next())
                pathToSave=set.getString(1);
            File fileToRestore = new File(Path + "\\" + fileName);
            FileInputStream fis = new FileInputStream(fileToRestore);
            System.out.println(fileToRestore.getPath());
            System.out.println(MyProtocol.READY);
            long fileLength = fileToRestore.length();
            out.println(fileLength);
            out.println(pathToSave);
            int read=0;
            int offset=0;
            int off=0;
            while(!receive().equals(MyProtocol.READY));
            while (offset < fileLength) {
                byte[] buffer = new byte[bufferSize];
                while(off<bufferSize && ((read =fis.read(buffer, off, bufferSize - off)) != -1)) {
                    off+=read;
                }
                outputStream.write(buffer, 0, off);
                outputStream.flush();
                offset+=off;
                System.out.println("Wczytano "+off+" bajtów do bufora");
                off=0;
            }
            out.flush();
            System.out.println("Wysłano "+offset+" bajtów");

            System.out.println(fileLength);
        } catch (Exception e) {
            System.out.println("Plik nie został zlokalizowany na serwerze" +e);
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
                if(request==null) {
                    System.out.println("WYLOGOWANO: "+userNameActive);
                    break;
                }
                if(request.equals(MyProtocol.LOGIN)){
                    if(logIn()){
                        if(Files.notExists(Paths.get(MyServer.PATH)))
                            Files.createDirectory(Paths.get(MyServer.PATH));
                        Path=MyServer.PATH+"\\"+userNameActive;
                        if(Files.notExists(Paths.get(Path)))
                            Files.createDirectory(Paths.get(Path));
                        if(Files.exists(Paths.get(Path + "\\" + "container" + userNameActive + ".txt"))) {
                            out.println(MyProtocol.FILEEXIST);
                            sendTitles();
                        }
                        else
                            out.println(MyProtocol.NOSUCHFILE);
                        connectToLocalDatabase("jdbc:sqlite:E:/BackuperData/"+userNameActive+"_");
                    }
                }
                else if(request.equals(MyProtocol.SENDFILE)){
                    try {
                        filesData = new ArrayList<>();
                        howmany = Integer.parseInt(bufferedReader.readLine());
                        System.out.println("Ilość plików do pobrania: "+howmany);
                        save = new PrintWriter(Path+"\\"+"container"+userNameActive+".txt");
                        for (int i = 0; i < howmany; i++) {
                            long fileLngth = Long.parseLong(bufferedReader.readLine());
                            String filePth = bufferedReader.readLine();
                            String fileNm=getFileName(filePth);
                            ResultSet set=mLocalDatabase.selectViaSQL("SELECT name, length, version FROM files");
                            int version=1;
                            while(set.next()){
                                String[] separated=separateNameFromExtension(set.getString(1));
                                String name_cmp=separateFromVersion(separated[0])+"."+separated[1];
                                long length_cmp=set.getLong(2);
                                if(fileNm.equals(name_cmp)){
                                    if(fileLngth!=length_cmp)
                                        version=set.getInt(3)+1;
                                }
                            }
                            filesNames.add(fileNm);
                            String [] separated=separateNameFromExtension(fileNm);
                            String fPath=separated[0]+"_v"+version+"."+separated[1];
                            save.println(fPath);
                            filesData.add(new FileMetaData(version,filePth,Path + "\\" + fileNm, fileLngth));
                        }
                        for (String fileTitle:filesTitles
                                ) {
                            save.println(fileTitle);
                        }
                        save.close();
                        out.println(MyProtocol.READY);
                        System.out.println(MyProtocol.READY);
                        for (int i = 0; i < howmany; i++) {
                            fileLength = filesData.get(i).getFileLength();
                            filePath = filesData.get(i).getFilePath();
                            fileVersion=filesData.get(i).getVersion();
                            String [] separated=separateNameFromExtension(filePath);
                            filePath=separated[0]+"_v"+fileVersion+"."+separated[1];
                            String clientPath=filesData.get(i).getClientPath();
                            File fileOut = new File(filePath);
                            FileOutputStream outputLocal = new FileOutputStream(fileOut);
                            int offset = 0;
                            int off=0;
                            int read;
                            long numbOfPacks=fileLength/((long)bufferSize);
                            long rest=fileLength%((long)bufferSize);
                            if(rest!=0)
                                numbOfPacks++;
                            out.println(MyProtocol.READY);
                            for(int j=0;j<numbOfPacks-1;j++){
                                byte[] buffer = new byte[bufferSize];
                                while(off<bufferSize && ((read =bufferedInputStream.read(buffer, off, bufferSize - off)) != -1)) {
                                    off+=read;
                                }
                                outputLocal.write(buffer, 0, off);
                                offset+=off;
                                off=0;
                            }
                            byte[] buffer = new byte[(int)rest];
                            while(off<rest && ((read =bufferedInputStream.read(buffer, off, (int)(rest - off))) != -1)) {
                                off+=read;
                            }
                            outputLocal.write(buffer, 0, off);
                            mLocalDatabase.newFile(getFileName(filePath),fileLength,clientPath,fileVersion);
                            System.out.println("Wczytano bajtów: " + offset + "/" + fileLength);
                            out.println(MyProtocol.FILEEXIST);
                            sendTitles();
                            out.println(MyProtocol.READY);
                            out.flush();
                            System.out.println(MyProtocol.READY);
                        }
                        ResultSet resultSet=mLocalDatabase.selectViaSQL("SELECT name,length,path FROM files;");
                        while(resultSet.next()){
                            System.out.println(resultSet.getString(1)+": "+resultSet.getInt(2)+": "+resultSet.getString(3));
                        }
                    }
                    catch (IOException e){
                        System.err.println(e);
                        out.println(MyProtocol.FAILED);
                    }
                }
                else if (request.equals(MyProtocol.RESTOREFILE)) {
                    restoreFiles(outputStream);

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
                    System.out.println("WYLOGOWANO: "+userNameActive);
                    MyServer.clientsList.removeElement(userNameActive);
                    try {
                        socket.close();
                        transferSocket.close();
                    }
                    catch (IOException e){
                        System.err.println(e);
                    }
                    break;
                }
            }
        }

        catch (Exception e){
            System.err.println(e);
        }
    }

}