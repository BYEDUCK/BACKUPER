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

    //private JTextArea dirOutTextArea;
    private JButton chooseFile;
    private JButton startBuckup;
    private JButton endButton;
    private JFileChooser fileChooser;
    private JLabel chosenNameLabel;
    private String chosenName;
    private JList fileQue;
    private DefaultListModel listModel;
    private ArrayList<String> que;
    private PrintWriter outNotify;
    //private OutputStream out;
    //private BufferedReader bufferedReader;
    //private FileInputStream fis;
    //private BufferedInputStream inputLocal;
    //private OutputStream outputGlobal;
    private  Socket socket=null;

    public Client()
    {
        prepareWindow();
        que=new ArrayList<>();
    }

    public static void main(String[] args)
    {
        new Client();
        /*try {
            socket = new Socket("localhost", 12129);
            InputStream inputStream=socket.getInputStream();
            PrintWriter outNotify=new PrintWriter(socket.getOutputStream(),true);
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            System.out.println(bufferedReader.readLine());
            OutputStream out=socket.getOutputStream();
            File file=new File("E:\\elektr\\Podręczniki.zip");
            int fileLength=(int)file.length();
            outNotify.println(fileLength);
            FileInputStream inputLocal=new FileInputStream(file);
            byte[] bytesFile=new byte[fileLength];
            inputLocal.read(bytesFile,0,fileLength);
            out.write(bytesFile,0,fileLength);
            out.flush();
            System.out.println(bufferedReader.readLine());
            socket.close();
        }
        catch (Exception e){
            System.err.println(e);
        }*/
    }

    private void setConnectionForSend(){
        try {
            socket = new Socket("localhost", 12129);
            //InputStream inputStream = socket.getInputStream();
            outNotify = new PrintWriter(socket.getOutputStream(), true);
            //bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            //System.out.println(bufferedReader.readLine());
        }
        catch (IOException e){
            System.err.println(e);
        }
    }

    private void sendFile(String filePath,OutputStream out){
        try {

            /*socket = new Socket("localhost", 12129);
            InputStream inputStream=socket.getInputStream();
            PrintWriter outNotify=new PrintWriter(socket.getOutputStream(),true);
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            System.out.println(bufferedReader.readLine());
            OutputStream out=socket.getOutputStream();*/

            char tmp;
            int i=0;
            String fileName_tmp="";
            int file_src_length=filePath.length();
            while((tmp=filePath.charAt(file_src_length-1-i))!='\\')
            {
                fileName_tmp+=tmp;
                i++;
            }
            StringBuilder builder=new StringBuilder(fileName_tmp);
            String fileName=builder.reverse().toString();


            File file=new File(filePath);
            int fileLength=(int)file.length();
            outNotify.println(fileLength);
            outNotify.println(fileName);
            FileInputStream inputLocal=new FileInputStream(file);
            byte[] bytesFile=new byte[fileLength];
            inputLocal.read(bytesFile,0,fileLength);
            out.write(bytesFile,0,fileLength);
            //System.out.println(bufferedReader.readLine());
        }
        catch (Exception e){
            System.err.println(e);
        }
    }



    private void prepareWindow()
    {
        //dirOutTextArea=new JTextArea("C:\\Users\\Mateusz\\Desktop");
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
        //contentFrame.add(dirOutTextArea,BorderLayout.NORTH);
        contentFrame.add(chooseFile,BorderLayout.NORTH);
        contentFrame.add(startBuckup,BorderLayout.WEST);
        setSize(new Dimension(500,200));
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
            que.add(chosenName);
        }
        else if(clicked==startBuckup)
        {
            try {
                setConnectionForSend();
                outNotify.println(que.size());
                for (String path : que
                        ) {
                    OutputStream out = socket.getOutputStream();
                    sendFile(path,out);
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
