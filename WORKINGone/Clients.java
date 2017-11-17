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
    private JLabel userNameLabel;
    private JLabel passwordLabel;
    private JButton createUser;
    private JTextArea userNameArea;
    private JTextArea passwordArea;
    private JTextArea login;
    private JTextArea password;
    private JButton logIn;
    private JButton newUser;
    private JFileChooser fileChooser;
    private JLabel chosenNameLabel;
    private JList fileQue;
    private JList filesArchivied;
    private JFrame logInFrame;
    private DefaultListModel listModelQue;
    private DefaultListModel listModelSent;
    private ArrayList<FileMetaData> que;
    private PrintWriter outNotify;
    private BufferedReader bufferedReader;
    private static int curiosity = 0;
    private Socket socket = null;
    private static int port=-1;
    JFrame newUserFrame;
    private boolean loggedIn=false;

    public Clients() {
        prepareLogInWindow();
        prepareWindow();
        que = new ArrayList<>();
    }

    public static void main(String[] args) {
        new Clients();
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


    private void prepareLogInWindow(){
        logInFrame=new JFrame();
        JPanel contentFrame=(JPanel)logInFrame.getContentPane();
        contentFrame.setLayout(new GridLayout(4,1));
        login=new JTextArea();
        password=new JTextArea();
        logIn=new JButton("Zaloguj");
        logIn.addActionListener(this);
        createUser=new JButton("Załóż konto");
        createUser.addActionListener(this);
        login.setBounds(0,0,400,25);
        password.setBounds(0,0,400,25);
        logIn.setBounds(0,0,400,100);
        login.setBorder(BorderFactory.createLineBorder(new Color(000000)));
        password.setBorder(BorderFactory.createLineBorder(new Color(000000)));
        contentFrame.add(login);
        contentFrame.add(password);
        contentFrame.add(logIn);
        contentFrame.add(createUser);
        logInFrame.setSize(new Dimension(400,150));
        logInFrame.setLocation(100,100);
        logInFrame.setVisible(true);
    }

    private void prepareNewUserWindow(){
        newUserFrame=new JFrame();
        JPanel contentFrame=(JPanel)newUserFrame.getContentPane();
        userNameLabel=new JLabel("Nazwa użytkownika");
        userNameArea=new JTextArea();
        passwordLabel=new JLabel("Hasło");
        passwordArea=new JTextArea();
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
        setVisible(false);
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
                //getAttainablePort();
                setConnection();
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
        else if(clicked == logIn)
        {
            try {
                String loginText = login.getText();
                String passwordText = password.getText();
                //getAttainablePort();
                setConnection();
                System.out.println(port);
                outNotify.println(loginText);
                outNotify.println(passwordText);
                int status = Integer.parseInt(bufferedReader.readLine());
                switch (status){
                    case (0):
                        System.out.println("Nie udało się zalogować!");
                        login.setBorder(BorderFactory.createLineBorder(new Color(0xff0000)));
                        password.setBorder(BorderFactory.createLineBorder(new Color(0xff0000)));
                        socket.close();
                        break;
                    case (1):
                        System.out.println("Udało się zalogować!");
                        logInFrame.setVisible(false);
                        setVisible(true);
                        socket.close();
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
            prepareNewUserWindow();
        }
    }

}