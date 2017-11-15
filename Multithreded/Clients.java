package Multithreded;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class Clients extends JFrame implements ActionListener {

    private JButton chooseFileButton;
    private JButton startBuckupButton;
    private JButton clearButton;
    private JFileChooser fileChooser;
    private JLabel chosenNameLabel;
    private JList fileQue;
    private JList filesArchivied;
    private DefaultListModel listModelQue;
    private DefaultListModel listModelSent;
    private ArrayList<FileMetaData> que;
    private PrintWriter outNotify;
    private BufferedReader bufferedReader;
    private static int curiosity = 0;
    private Socket socket = null;
    private static int port;

    public Clients() {
        prepareWindow();
        que = new ArrayList<>();
    }

    public static void main(String[] args) {
        new Clients();
    }

    private int getAttainablePort() throws IOException {
        Socket socket = new Socket("localhost", 12129);
        InputStream inputStream = socket.getInputStream();
        BufferedReader portReader = new BufferedReader(new InputStreamReader(inputStream));
        port = Integer.parseInt(portReader.readLine());
        System.out.println(port);
        socket.close();
        return port;
    }

    private void setConnectionForSend(){
        try {
            socket = new Socket("localhost", getAttainablePort());
            InputStream inputStream = socket.getInputStream();
            outNotify = new PrintWriter(socket.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        }
        catch (IOException e){
            System.err.println("Setting connection problem: " +e);
        }
    }

    private String readFromServer(){
        try {
            return bufferedReader.readLine();
        }
        catch (IOException e){
            System.err.println(e);
            return null;
        }
    }

    private void sendMetaData(FileMetaData fileData){
        char tmp;
        int i = 0;
        String fileName_tmp="";
        String filePth = fileData.getFilePath();
        int file_src_length = filePth.length();
        while((tmp = filePth.charAt(file_src_length-1-i))!='\\') {
            fileName_tmp += tmp;
            i++;
        }
        StringBuilder builder=new StringBuilder(fileName_tmp);
        String fileName=builder.reverse().toString();
        outNotify.println(fileData.getFileLength());
        outNotify.println(fileName);
    }

    private void sendFile(FileMetaData fileData, OutputStream out){
        try {

            File file = new File(fileData.getFilePath());
            int fileLength = fileData.getFileLength();
            FileInputStream inputLocal = new FileInputStream(file);
            byte[] bytesFile = new byte[fileLength];
            inputLocal.read(bytesFile, 0, fileLength);
            out.write(bytesFile, 0, fileLength);
        } catch (Exception e){
            System.err.println(e);
        }
    }



    private void prepareWindow() {
        chooseFileButton = new JButton("Wybierz plik");
        startBuckupButton = new JButton("Rozpocznij przesyłanie");
        clearButton = new JButton("Clear");
        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        chosenNameLabel = new JLabel("Wybrany plik");
        listModelQue = new DefaultListModel();
        fileQue = new JList(listModelQue);
        listModelSent = new DefaultListModel();
        filesArchivied = new JList(listModelSent);
        fileQue.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileQue.setVisibleRowCount(5);
        chooseFileButton.addActionListener(this);
        startBuckupButton.addActionListener(this);
        clearButton.addActionListener(this);
        JPanel contentFrame=(JPanel)this.getContentPane();
        contentFrame.setLayout(new BorderLayout());
        contentFrame.add(chosenNameLabel,BorderLayout.SOUTH);
        contentFrame.add(fileQue,BorderLayout.CENTER);
        contentFrame.add(clearButton,BorderLayout.EAST);
        contentFrame.add(chooseFileButton,BorderLayout.NORTH);
        contentFrame.add(startBuckupButton,BorderLayout.WEST);
        setSize(new Dimension(800,200));
        setLocation(300,200);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object clicked = e.getSource();
        if(clicked == chooseFileButton) {
            if(fileChooser.showOpenDialog(chooseFileButton) == JFileChooser.APPROVE_OPTION){
                File[] chosenFiles = fileChooser.getSelectedFiles();
                for (File file:chosenFiles) {
                    String name=file.getPath();
                    int length=(int)file.length();
                    listModelQue.addElement(name);
                    que.add(new FileMetaData(name,length));
                    System.out.println("Your choice: "+name);
                }
            }
        }
        else if(clicked == startBuckupButton) {
            try {
                getAttainablePort();
                setConnectionForSend();
                System.out.println(port);
                OutputStream out = socket.getOutputStream();
                outNotify.println(que.size());
                for (FileMetaData data : que) {
                    sendMetaData(data);
                }

                for (FileMetaData data:que) {
                    while(!(readFromServer().equals(Server.ready))){
                        curiosity++;
                        System.out.println("WAITING..");//ani razu nie wypisuje WAITING a dziala :D
                    }
                    sendFile(data,out);
                    out.flush();
                }
                //System.out.println(curiosity);
                socket.close();
            }
            catch (IOException e1){
                System.err.println(e1);
            }
        }
        else if(clicked == clearButton)
        {

        }
    }

}