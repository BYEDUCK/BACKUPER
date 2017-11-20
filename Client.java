package BACKUPER;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;


public class Client extends JFrame implements ActionListener {

    private JButton chooseFileButton;
    private JButton startBuckupButton;
    private JButton clearButton;
    private JLabel userNameLabel;
    private JLabel passwordLabel;
    private JLabel restoreLabel;
    private JComboBox restoreComboBox;
    private JButton createUser;
    private JTextArea userNameArea;
    private JPasswordField passwordArea;
    private JTextArea login;
    private JPasswordField password;
    private JButton logIn;
    private JButton newUser;
    private JButton restoreButton;
    private JFileChooser fileChooser;
    //private JLabel chosenNameLabel;
    private JList fileQue;
    private JList filesArchivied;
    private JFrame logInFrame;
    private DefaultListModel listModelQue;
    private DefaultListModel listModelSent;
    private ArrayList<FileMetaData> que;
    private ArrayList<FileMetaData> filesSent;
    private PrintWriter outNotify;
    private BufferedReader bufferedReader;
    private OutputStream outputStream;
    private static int curiosity = 0;
    private Socket socket = null;
    private static int port=-1;
    JFrame newUserFrame;
    private boolean loggedIn=false;
    private JLabel userLoginLabel;
    private Vector<String> restoreFiles;
    private int filesNumber;
    private String ignored;

    public Client() {
        prepareLogInWindow();
        prepareWindow();
        que = new ArrayList<>();
        filesSent=new ArrayList<>();
    }

    public static void main(String[] args) {
        new Client();
        try {
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        }
        catch (Exception e){
            System.err.println(e);
        }
    }

    private void getAttainablePort() throws IOException {
        if(port==-1) {
            Socket socket = new Socket("localhost", 12129);
            InputStream inputStream = socket.getInputStream();
            BufferedReader portReader = new BufferedReader(new InputStreamReader(inputStream));
            port = Integer.parseInt(portReader.readLine());
            System.out.println("Nowoustawiony port: "+port);
            socket.close();
        }
        else{
            System.out.println("Poprzednio ustawiony port: "+port);
        }
    }

    /*private void setConnectionForLogin(){
        try {
            socket = new Socket("localhost", 12129);
            outNotify=new PrintWriter(socket.getOutputStream(),true);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (IOException e){
            System.err.println(e);
        }
    }*/

    private void setConnection(){
        try {
            getAttainablePort();
            socket = new Socket("localhost", port);
            InputStream inputStream = socket.getInputStream();
            outNotify = new PrintWriter(socket.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            outputStream=socket.getOutputStream();
        }
        catch (IOException e){
            System.err.println("Error establishing the connection with server!: "+e);
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

    public void fillVector() {
        int amount = Integer.parseInt(receive());
        System.out.println(amount);
        if (amount != 0) {
            for (int i = 0; i < amount; i++) {
                restoreComboBox.addItem(receive());
            }
        }
        else
            ignored = receive();
    }


    private void prepareLogInWindow(){
        logInFrame=new JFrame();
        logInFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
            }
        });
        JPanel contentFrame=(JPanel)logInFrame.getContentPane();
        contentFrame.setLayout(new GridLayout(4,1));
        login=new JTextArea();
        password=new JPasswordField();
        logIn=new JButton("Zaloguj");
        logIn.addActionListener(this);
        createUser=new JButton("Załóż konto");
        createUser.addActionListener(this);
        login.setBounds(0,0,400,25);
        password.setBounds(0,0,400,25);
        logIn.setBounds(0,0,400,100);
        login.setBorder(BorderFactory.createLineBorder(new Color(000000)));
        password.setBorder(BorderFactory.createLineBorder(new Color(0x000000)));
        contentFrame.add(login);
        contentFrame.add(password);
        contentFrame.add(logIn);
        contentFrame.add(createUser);
        logInFrame.setSize(new Dimension(400,150));
        logInFrame.setLocation(100,100);
        logInFrame.setVisible(true);
        logInFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void prepareNewUserWindow(){
        newUserFrame=new JFrame();
        newUserFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logInFrame.setVisible(true);
                super.windowClosing(e);
            }
        });
        JPanel contentFrame=(JPanel)newUserFrame.getContentPane();
        userNameLabel=new JLabel("Nazwa użytkownika");
        userNameArea=new JTextArea();
        passwordLabel=new JLabel("Hasło");
        passwordArea=new JPasswordField();
        newUser=new JButton("Załóż konto");
        newUser.addActionListener(this);
        contentFrame.setLayout(new GridLayout(5,1));
        newUserFrame.setSize(new Dimension(300,250));
        contentFrame.add(userNameLabel);
        contentFrame.add(userNameArea);
        contentFrame.add(passwordLabel);
        contentFrame.add(passwordArea);
        contentFrame.add(newUser);
        newUserFrame.setVisible(true);
    }

    private void prepareWindow() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
            }
        });
        JLabel toSendLabel=new JLabel("Files to be send:");
        JLabel sentLabel=new JLabel("Files sent:");
        userLoginLabel=new JLabel();
        chooseFileButton = new JButton("Wybierz pliki do przesłania");
        startBuckupButton = new JButton("Rozpocznij przesyłanie");
        clearButton = new JButton("Clear history");
        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        restoreLabel = new JLabel("Przywróć pliki:");
        restoreComboBox = new JComboBox();
        restoreButton = new JButton("Odtwórz plik");
        //chosenNameLabel = new JLabel("Wybrany plik");
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
        contentFrame.setLayout(new GridLayout(11,2));
        contentFrame.add(chooseFileButton);
        contentFrame.add(toSendLabel);
        contentFrame.add(new JScrollPane(fileQue));
        contentFrame.add(sentLabel);
        contentFrame.add(new JScrollPane(filesArchivied));
        contentFrame.add(clearButton);
        contentFrame.add(startBuckupButton);
        contentFrame.add(restoreLabel);
        contentFrame.add(restoreComboBox);
        contentFrame.add(restoreButton);
        contentFrame.add(userLoginLabel);
        setSize(new Dimension(650,650));
        setLocation(300,200);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });
        setVisible(false);
    }

    private String receive(){
        try{
            return bufferedReader.readLine();
        }
        catch (IOException e){
            System.err.println(e);
            return null;
        }
    }

    @SuppressWarnings("deprecated")
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
                //getAttainablePort();
                //setConnection();
                //System.out.println(port);
                //OutputStream out = socket.getOutputStream();
                outNotify.println(MyProtocol.SENDFILE);
                outNotify.println(que.size());
                for (FileMetaData data : que) {
                    sendMetaData(data);
                }

                for (FileMetaData data:que) {
                    while(!(readFromServer().equals(MyServer.ready))){
                        curiosity++;
                        System.out.println("WAITING..");//ani razu nie wypisuje WAITING a dziala :D
                    }
                    sendFile(data,outputStream);
                    outputStream.flush();
                    filesSent.add(data);
                    listModelSent.addElement(data.getFilePath());
                }
                que.clear();
                listModelQue.clear();
                //System.out.println(curiosity);
                //socket.close();
            }
            catch (IOException e1){
                System.err.println(e1);
            }
        }
        else if(clicked == clearButton) {
            listModelSent.clear();
            filesSent.clear();
        }
        else if(clicked == logIn)
        {
            try {
                String loginText = login.getText();
                String passwordText = password.getText();
                //getAttainablePort();
                if(socket==null) {
                    setConnection();
                    System.out.println("Ustalono połączenie: " + port);
                    //outNotify.println(MyProtocol.LOGIN);
                }
                outNotify.println(MyProtocol.LOGIN);
                outNotify.println(loginText);
                outNotify.println(passwordText);
                //int status = Integer.parseInt(bufferedReader.readLine());
                String request = receive();
                /*StringTokenizer st = new StringTokenizer(request);
                String command = st.nextToken();*/
                switch (request){
                    case (MyProtocol.FAILED):
                        System.out.println("Nie udało się zalogować!");
                        JOptionPane.showMessageDialog(null, "Błędny login, lub hasło");
                        login.setBorder(BorderFactory.createLineBorder(new Color(0xff0000)));
                        password.setBorder(BorderFactory.createLineBorder(new Color(0xff0000)));
                        //socket.close();
                        break;
                    case (MyProtocol.LOGGEDIN):
                        System.out.println("Udało się zalogować!");
                        userLoginLabel.setText("Zalogowano jako: "+loginText+".");
                        loggedIn=true;
                        logInFrame.setVisible(false);
                        setVisible(true);
                        fillVector();
                        //socket.close();
                        break;
                    default:
                        System.out.println("PROBLEM!");
                        socket.close();
                        break;
                }
            }
            catch (IOException e1){
                System.err.println(e1);
            }
        }
        else if(clicked==createUser){
            logInFrame.setVisible(false);
            prepareNewUserWindow();
        }
        else if(clicked==newUser){
            String name=userNameArea.getText();
            String password=passwordArea.getText();
            if(socket==null) {
                setConnection();
                System.out.println("Ustalono połączenie: " + port);
            }
            outNotify.println(MyProtocol.NEWUSER);
            outNotify.println(name);
            outNotify.println(password);
            String response=receive();
            switch (response){
                case(MyProtocol.FAILED):
                    userNameArea.setBorder(BorderFactory.createLineBorder(new Color(0xff0000)));
                    passwordArea.setBorder(BorderFactory.createLineBorder(new Color(0xff0000)));
                    break;
                case (MyServer.ready):
                    newUserFrame.setVisible(false);
                    logInFrame.setVisible(true);
                    login.setText(name);
                    break;
                default:
                    break;
            }
        }
    }

}
