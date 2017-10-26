import javax.swing.*;
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
    public static  int fileLength;



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
                //InputStream input=new ObjectInputStream(socket.getInputStream());
                InputStream inputS=socket.getInputStream();
                BufferedInputStream input=new BufferedInputStream(inputS);
                //BufferedInputStream input=new BufferedInputStream(socket.getInputStream());
                PrintWriter out=new PrintWriter(socket.getOutputStream(),true);
                out.println("Nawiązano połączenie!");
                BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                fileLength=Integer.parseInt(reader.readLine());
                byte[] fileBytes=new byte[fileLength];
                int offset=0;
                int read=0;
                while(offset<fileLength && (read=input.read(fileBytes,offset,fileLength-offset))!=EOF)
                {
                    offset+=read;
                }
                //int bytesRead=input.read(fileBytes,0,fileLength);
                System.out.println("Wczytano bajtów: "+read+" a miało być: "+fileLength);
                File fileOut=new File("C:\\Users\\Mateusz\\Desktop\\kopia.iso");
                FileOutputStream outputLocal=new FileOutputStream(fileOut);
                outputLocal.write(fileBytes,0,fileLength);
                outputLocal.flush();
                out.println("Pobrano plik i utworzono kopie!");
                socket.close();
            }
            catch (IOException e){
                System.err.println(e);
            }

        }
    }
}
