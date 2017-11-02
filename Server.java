import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Vector;

import static sun.nio.ch.IOStatus.EOF;
//import java.io.EOFException;

public class Server extends JFrame  {

    //private Properties properties;
    //private Vector<Client> clients=new Vector<>();
    //private static ServerSocket serverSocket;
    private static  int fileLength;
    private static String fileName;
    private static JProgressBar progressBar;
    private static int howmany;

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
                InputStream inputS=socket.getInputStream();
                BufferedInputStream input=new BufferedInputStream(inputS);
                //PrintWriter out=new PrintWriter(socket.getOutputStream(),true);
                //out.println("Nawiązano połączenie!");
                //ProgressMonitorInputStream input=new ProgressMonitorInputStream(null,"Reading...",Binput);
                BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                howmany=Integer.parseInt(reader.readLine());
                for(int i=0;i<howmany;i++) {
                    reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    fileLength = Integer.parseInt(reader.readLine());
                    fileName = reader.readLine();
                    //Server server = new Server(0, fileLength);
                    byte[] fileBytes = new byte[fileLength];
                    Server server = new Server(0, fileLength);
                    int offset = 0;
                    int read;
                    while (offset < fileLength && (read = input.read(fileBytes, offset, fileLength - offset)) != EOF) {
                        offset += read;
                        progressBar.setValue(offset);
                    }
                    System.out.println("Wczytano bajtów: " + offset + "/" + fileLength);
                    File fileOut = new File("E:\\copied\\kopia-" + fileName);
                    FileOutputStream outputLocal = new FileOutputStream(fileOut);
                    outputLocal.write(fileBytes, 0, fileLength);
                    outputLocal.flush();
                    //out.println("Pobrano plik i utworzono kopie!");
                }
                socket.close();
            }
            catch (IOException e){
                System.err.println(e);
            }

        }
    }
}
