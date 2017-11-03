import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;


public class Client extends JFrame implements ActionListener{

    private JButton chooseFile;
    private JButton startBuckup;
    private JButton endButton;
    private JFileChooser fileChooser;
    private JLabel chosenNameLabel;
    private String chosenName;
    private JList fileQue;
    private DefaultListModel listModel;
    private ArrayList<FileMetaData> que;
    private PrintWriter outNotify;
    private BufferedReader bufferedReader;
    private  Socket socket=null;

    public Client()
    {
        prepareWindow();
        que=new ArrayList<>();
    }

    public static void main(String[] args)
    {
        new Client();
    }

    private void setConnectionForSend(){
        try {
            socket = new Socket("localhost", 12129);
            InputStream inputStream = socket.getInputStream();
            outNotify = new PrintWriter(socket.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        }
        catch (IOException e){
            System.err.println(e);
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
        int i=0;
        String fileName_tmp="";
        String filePth=fileData.getFilePath();
        int file_src_length=filePth.length();
        while((tmp=filePth.charAt(file_src_length-1-i))!='\\')
        {
            fileName_tmp+=tmp;
            i++;
        }
        StringBuilder builder=new StringBuilder(fileName_tmp);
        String fileName=builder.reverse().toString();
        outNotify.println(fileData.getFileLength());
        outNotify.println(fileName);
    }

    private void sendFile(FileMetaData fileData,OutputStream out){
        try {
            File file = new File(fileData.getFilePath());
            int fileLength = fileData.getFileLength();    
            FileInputStream inputLocal = new FileInputStream(file);
            byte[] bytesFile = new byte[fileLength];
            inputLocal.read(bytesFile, 0, fileLength);
            out.write(bytesFile, 0, fileLength);
        }
        catch (Exception e){
            System.err.println(e);
        }
    }



    private void prepareWindow()
    {
        chooseFile=new JButton("Wybierz plik");
        startBuckup=new JButton("Rozpocznij przesyłanie");
        endButton=new JButton("END");
        fileChooser=new JFileChooser();
        chosenNameLabel =new JLabel("Wybrany plik");
        listModel=new DefaultListModel();
        fileQue=new JList(listModel);
        fileQue.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileQue.setVisibleRowCount(5);
        chooseFile.addActionListener(this);
        startBuckup.addActionListener(this);
        endButton.addActionListener(this);
        JPanel contentFrame=(JPanel)this.getContentPane();
        contentFrame.setLayout(new BorderLayout());
        contentFrame.add(chosenNameLabel,BorderLayout.SOUTH);
        contentFrame.add(fileQue,BorderLayout.CENTER);
        contentFrame.add(endButton,BorderLayout.EAST);
        contentFrame.add(chooseFile,BorderLayout.NORTH);
        contentFrame.add(startBuckup,BorderLayout.WEST);
        setSize(new Dimension(800,200));
        setLocation(100,100);
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
        Object clicked=e.getSource();
        if(clicked==chooseFile)
        {
            if(fileChooser.showOpenDialog(chooseFile)==JFileChooser.APPROVE_OPTION){}
            chosenName=fileChooser.getSelectedFile().getAbsolutePath();
            System.out.println("Your choice: "+chosenName);
            chosenNameLabel.setText(chosenName);
            listModel.addElement(chosenName);
            File file=new File(chosenName);
            int fileLength=(int)file.length();
            que.add(new FileMetaData(chosenName,fileLength));
        }
        else if(clicked==startBuckup)
        {
            try {
                setConnectionForSend();
                OutputStream out=socket.getOutputStream();
                outNotify.println(que.size());
                for (FileMetaData data : que
                        ) {
                    sendMetaData(data);
                }

                for (FileMetaData data:que
                     ) {
                    while(!readFromServer().equals(Server.ready)){
                        System.out.println("WAITING..");//ani razu nie wypisuje WAITING a dziala :D
                    }
                    sendFile(data,out);
                    out.flush();
                }
                socket.close();
            }
            catch (IOException e1){
                System.err.println(e1);
            }
        }
        else if(clicked==endButton)
        {
        }
    }

}
