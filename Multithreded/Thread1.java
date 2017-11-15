package Multithreded;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import static Multithreded.Server.transferPort;
import static Multithreded.Server.path;
import static Multithreded.Server.ready;
import static sun.nio.ch.IOStatus.EOF;


public class Thread1 extends JFrame implements Runnable {

    private Socket socket = null;
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

    public Thread1() {
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);;
        setSize(500,400);
        add(progressBar);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void run() {
        ServerSocket transferSocket = null;
        try {
            transferSocket = new ServerSocket(transferPort);
        } catch (Exception e) {
            System.err.println("Server socket for client creating problem: " + e);
        }
        try {
            socket = transferSocket.accept();
            filesData = new ArrayList<>();
            inputStream = socket.getInputStream();
            bufferedInputStream = new BufferedInputStream(inputStream);
            out = new PrintWriter(socket.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            howmany = Integer.parseInt(bufferedReader.readLine());
            for(int i = 0; i<howmany; i++){
                int fileLngth = Integer.parseInt(bufferedReader.readLine());
                String fileNm = bufferedReader.readLine();
                filesData.add(new FileMetaData(path+"\\"+fileNm,fileLngth));
                System.out.println(filesData);
            }
            out.println(ready);
            System.out.println(ready);
            for(int i = 0; i<howmany; i++) {
                fileLength = filesData.get(i).getFileLength();
                filePath = filesData.get(i).getFilePath();
                progressBar.setMinimum(0);
                progressBar.setMaximum(fileLength);
                progressBar.setValue(0);
                byte[] fileBytes = new byte[fileLength];
                int offset = 0;
                int read;
                while (offset < fileLength && (read = bufferedInputStream.read(fileBytes, offset, fileLength - offset)) != EOF) {
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
