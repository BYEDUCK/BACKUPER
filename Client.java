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
    private JTextArea IPArea;
    private JTextArea IPArea_newUser;
    private JPasswordField passwordArea;
    private JTextArea login;
    private JPasswordField password;
    private JButton logIn;
    private JButton newUser;
    private JButton restoreButton;
    private JFileChooser fileChooser;
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
    protected static Socket socket = null;
    private static int port=-1;
    JFrame newUserFrame;
    private boolean loggedIn=false;
    private JLabel userLoginLabel;
    private JButton logout;
    private Vector<String> restoreFiles;
    private int filesNumber;
    private int ignored;
    private BufferedInputStream bis;
    private boolean connected = true;

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

    private void getAttainablePort(String IP) throws IOException {
        if(port==-1) {
            Socket socket = new Socket(IP, 12129);
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



    private void setConnection(String IP){
        try {
            getAttainablePort(IP);
            socket = new Socket(IP, port);
            InputStream inputStream = socket.getInputStream();
            outNotify = new PrintWriter(socket.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            outputStream=socket.getOutputStream();
            bis = new BufferedInputStream(inputStream);
        }
        catch (IOException e){
            System.err.println("Error establishing the connection with server!: "+e);
        }
    }


    public String getFileName(String filePath){
        char tmp;
        int i = 0;
        String fileName_tmp="";
        int file_src_length = filePath.length();
        while((tmp = filePath.charAt(file_src_length-1-i))!='\\') {
            fileName_tmp += tmp;
            i++;
        }
        StringBuilder builder=new StringBuilder(fileName_tmp);
        return builder.reverse().toString();
    }

    private void sendMetaData(FileMetaData fileData){
        String filePath=fileData.getFilePath();
        outNotify.println(fileData.getFileLength());
        outNotify.println(filePath);
        //restoreComboBox.addItem(getFileName(filePath));
    }

    public void sendFile(FileMetaData fileData, OutputStream out){
        try {

            File file = new File(fileData.getFilePath());

            int fileLength = fileData.getFileLength();
            FileInputStream inputLocal = new FileInputStream(file);
            byte[] bytesFile = new byte[fileLength];
            inputLocal.read(bytesFile, 0, fileLength);
            inputLocal.close();
            out.write(bytesFile, 0, fileLength);
        } catch (Exception e){
            JOptionPane.showMessageDialog(null,"Utracono połączenie, włącz aplikację ponownie.");
            System.err.println(e);
        }
    }


    private void fillVector() {
        String tmp = receive();
        restoreComboBox.removeAllItems();
        if(tmp.equals(MyProtocol.FILEEXIST)) {
            int amount = Integer.parseInt(receive());
            System.out.println(amount);
            System.out.println(tmp);
            outNotify.println(MyProtocol.READY);
            if (amount != 0) {
                for (int i = 0; i < amount; i++) {
                    restoreComboBox.addItem(receive());
                }
            }
        }else
            System.out.println(tmp);
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
        contentFrame.setLayout(new GridLayout(8,1));
        JLabel IPAreaText=new JLabel("Adres IP serwera");
        JLabel loginLabel=new JLabel("Login");
        JLabel passwordLabel=new JLabel("Hasło");
        IPArea=new JTextArea();
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
        contentFrame.add(loginLabel);
        contentFrame.add(login);
        contentFrame.add(passwordLabel);
        contentFrame.add(password);
        contentFrame.add(IPAreaText);
        contentFrame.add(IPArea);
        contentFrame.add(logIn);
        contentFrame.add(createUser);
        logInFrame.setSize(new Dimension(400,240));
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
        JLabel IPAreaText_newUser=new JLabel("Adres IP serwera");
        IPArea_newUser=new JTextArea();
        userNameLabel=new JLabel("Nazwa użytkownika");
        userNameArea=new JTextArea();
        passwordLabel=new JLabel("Hasło");
        passwordArea=new JPasswordField();
        newUser=new JButton("Załóż konto");
        newUser.addActionListener(this);
        contentFrame.setLayout(new GridLayout(7,1));
        newUserFrame.setSize(new Dimension(300,280));
        contentFrame.add(userNameLabel);
        contentFrame.add(userNameArea);
        contentFrame.add(passwordLabel);
        contentFrame.add(passwordArea);
        contentFrame.add(IPAreaText_newUser);
        contentFrame.add(IPArea_newUser);
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
        logout=new JButton("LOGOUT");
        logout.addActionListener(this);
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
        restoreButton.addActionListener(this);
        JPanel contentFrame=(JPanel)this.getContentPane();
        contentFrame.setLayout(new GridLayout(12,2));
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
        contentFrame.add(logout);
        setSize(new Dimension(650,650));
        setLocation(300,200);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                outNotify.println(MyProtocol.LOGOUT);
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
                outNotify.println(MyProtocol.SENDFILE);
                outNotify.println(que.size());
                for (FileMetaData data : que) {
                    sendMetaData(data);
                }

                for (FileMetaData data:que) {
                    while(!(receive().equals(MyProtocol.READY))){
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
                fillVector();
                if(listModelSent.size() != 0)
                    JOptionPane.showMessageDialog(null, "Poprawnie przesłano " +listModelSent.size()+ " plików.");
            }
            catch (IOException e1){
                System.err.println(e1);
            }
        }
        else if(clicked == clearButton) {
            listModelSent.clear();
            filesSent.clear();
        }
        else if (clicked == restoreButton) {
            String fileTMP = restoreComboBox.getSelectedItem().toString();
            outNotify.println(MyProtocol.RESTOREFILE);
            outNotify.println(fileTMP);
            int fileLength=0;
            String rec=receive();
            if(rec.equals(MyProtocol.READY))
                fileLength = Integer.parseInt(receive());
            else
                fileLength=Integer.parseInt(rec);
            String pathToSave=receive();
            outNotify.println(MyProtocol.READY);
            System.out.println(fileLength);
            byte[] fileBytes = new byte[fileLength];
            int offset = 0;
            int read;
            System.out.println("fine");
            try {
                while (offset < fileLength && (read = bis.read(fileBytes, offset, fileLength - offset)) != -1) {
                    offset += read;
                }
            }catch (IOException ioe) {
                JOptionPane.showMessageDialog(null, "Utracono połączenie, włącz ponownie aplikację.");
                System.err.println(ioe);
            } try {
                System.out.println("Wczytano bajtów: " + offset + "/" + fileLength);
                File fileOut = new File(pathToSave);
                FileOutputStream outputLocal = new FileOutputStream(fileOut);
                outputLocal.write(fileBytes, 0, fileLength);
                JOptionPane.showMessageDialog(null, "Poprawnie odtworzona plik: " +fileTMP);
                outputLocal.flush();
            } catch (Exception er) {
                System.err.println("Błąd przywracania pliku: " +er);
            }
        }
        else if(clicked == logIn)
        {
            try {
                String loginText = login.getText();
                String passwordText = password.getText();
                String IP=IPArea.getText();
                if(IP.isEmpty())
                    IP="localhost";
                if(socket==null) {
                    setConnection(IP);
                    System.out.println("Ustalono połączenie: " + port);
                }
                outNotify.println(MyProtocol.LOGIN);
                outNotify.println(loginText);
                outNotify.println(passwordText);
                String request = receive();
                switch (request){
                    case (MyProtocol.FAILED):
                        System.out.println("Nie udało się zalogować!");
                        JOptionPane.showMessageDialog(null, "Błędny login, lub hasło");
                        login.setBorder(BorderFactory.createLineBorder(new Color(0xff0000)));
                        password.setBorder(BorderFactory.createLineBorder(new Color(0xff0000)));
                        break;
                    case (MyProtocol.LOGGEDIN):
                        System.out.println("Udało się zalogować!");
                        userLoginLabel.setText("Zalogowano jako: "+loginText+".");
                        loggedIn=true;
                        logInFrame.setVisible(false);
                        setVisible(true);
                        fillVector();
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
            String IP_newUser=IPArea_newUser.getText();
            if(IP_newUser.isEmpty())
                IP_newUser="localhost";
            if(socket==null) {
                setConnection(IP_newUser);
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
                case (MyProtocol.READY):
                    newUserFrame.setVisible(false);
                    logInFrame.setVisible(true);
                    login.setText(name);
                    break;
                default:
                    break;
            }
        }
        else if(clicked==logout){
            outNotify.println(MyProtocol.LOGOUT);
            try{
                socket.close();
                socket=null;
            }
            catch (IOException e1){
                System.err.println("Error loggin out (client): "+e1);
                socket=null;
            }
            port=-1;
            logInFrame.setVisible(true);
            setVisible(false);
            login.setText("");
            password.setText("");
        }
    }
}