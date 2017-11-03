import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import static sun.nio.ch.IOStatus.EOF;

public class Server extends JFrame  {

    private static  int fileLength;
    private static String filePath;
    private static JProgressBar progressBar;
    private static int howmany;
    private static ArrayList<FileMetaData> filesData;
    public static final String ready="READY";
    public static final String end="END";

    public Server(int min,int max)
    {
        progressBar=new JProgressBar(min,max);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        setSize(new Dimension(500,100));
        add(progressBar);
        setVisible(true);
    }

    public static void main(String[] args)
    {
        ServerSocket serverSocket=null;
        try{
            serverSocket=new ServerSocket(12129);
        }
        catch (Exception e){
            System.err.println(e);
        }
        while(true)
        {
            try {
                Socket socket = serverSocket.accept();
                filesData=new ArrayList<>();
                InputStream inputS=socket.getInputStream();
                BufferedInputStream input=new BufferedInputStream(inputS);
                PrintWriter out=new PrintWriter(socket.getOutputStream(),true);              
                BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                howmany=Integer.parseInt(reader.readLine());
                for(int i=0;i<howmany;i++){
                    int fileLngth=Integer.parseInt(reader.readLine());
                    String fileNm=reader.readLine();
                    filesData.add(new FileMetaData("E:\\copied\\"+fileNm,fileLngth));
                }
                out.println(ready);
                System.out.println(ready);
                for(int i=0;i<howmany;i++) {                  
                    fileLength = filesData.get(i).getFileLength();
                    filePath = filesData.get(i).getFilePath();
                    byte[] fileBytes = new byte[fileLength];
                    Server server = new Server(0, fileLength);
                    int offset = 0;
                    int read;
                    while (offset < fileLength && (read = input.read(fileBytes, offset, fileLength - offset)) != EOF) {
                        offset += read;
                        progressBar.setValue(offset);
                    }
                    System.out.println("Wczytano bajtów: " + offset + "/" + fileLength);
                    File fileOut = new File(filePath);
                    FileOutputStream outputLocal = new FileOutputStream(fileOut);
                    outputLocal.write(fileBytes, 0, fileLength);
                    outputLocal.flush();
                    out.println(ready);
                    System.out.println(ready);
                    
                }
                out.println(end);
                socket.close();
            }
            catch (IOException e){
                System.err.println(e);
            }

        }
    }
}
