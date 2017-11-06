import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import static sun.nio.ch.IOStatus.EOF;
//import java.io.EOFException;

public class Server1 extends JFrame implements ActionListener {

    //private Properties properties;
    //private Vector<Client> clients=new Vector<>();
    //private static ServerSocket serverSocket;
    private JButton choosePathButton;
    private static  int fileLength;
    private static String filePath;
    private static JProgressBar progressBar;
    private static int howmany;
    private static ArrayList<FileMetaData> filesData;
    public static final String ready="READY";
    public static final String end="END";
    public static String path = "D:\\Users\\mpars\\Desktop\\BackuperKopie";

    public Server1()
    {
        setSize(500, 400);
        setTitle("Nowe połączenie");
        setLayout(null);

        choosePathButton = new JButton("Wybierz");
        choosePathButton.setBounds(200,150,100,100);
        choosePathButton.addActionListener(this);
        add(choosePathButton);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);;
        setSize(500,400);
        add(progressBar);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args)
    {
        ServerSocket serverSocket=null;
        try{
            serverSocket=new ServerSocket(12130);
        }
        catch (Exception e){
            System.err.println(e);
        }
        while(true)
        {
            try {
                new Server1();
                /*server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                server.setVisible(true);*/
                Socket socket = serverSocket.accept();
                filesData=new ArrayList<>();
                InputStream inputS=socket.getInputStream();
                BufferedInputStream input=new BufferedInputStream(inputS);
                PrintWriter out=new PrintWriter(socket.getOutputStream(),true);
                BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                howmany = Integer.parseInt(reader.readLine());
                //String path = new String(JOptionPane.showMessageDialog(null,file));
                //new Server1();
                for(int i=0; i<howmany; i++){
                    int fileLngth=Integer.parseInt(reader.readLine());
                    String fileNm = reader.readLine();
                    filesData.add(new FileMetaData(path+"\\"+fileNm,fileLngth));
                }
                out.println(ready);
                System.out.println(ready);
                //new Server1();
                for(int i=0;i<howmany;i++) {
                    //reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    fileLength = filesData.get(i).getFileLength();
                    filePath = filesData.get(i).getFilePath();
                    progressBar.setMinimum(0);
                    progressBar.setMaximum(fileLength);
                    progressBar.setValue(0);
                    byte[] fileBytes = new byte[fileLength];
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

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if(source == choosePathButton) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                path = file.getPath();
                JOptionPane.showMessageDialog(null, file);
            }
        }
    }
}