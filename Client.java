import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;


public class Client extends JFrame implements ActionListener{

    private JTextArea dirOutTextArea;
    private JButton chooseFile;
    private JButton startBuckup;
    private JFileChooser fileChooser;
    private JLabel chosenNameLabel;
    private String chosenName;
    private FileInputStream fis;
    private BufferedInputStream inputLocal;
    private OutputStream outputGlobal;
    private Socket socket=null;

    public Client()
    {
        prepareWindow();
    }

    public static void main(String[] args)
    {
        //new Client();
        try {
            //int fileLength=Server.fileLength;
            Socket socket = new Socket("localhost", 12129);
            InputStream inputStream=socket.getInputStream();
            PrintWriter outNotify=new PrintWriter(socket.getOutputStream(),true);
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            System.out.println(bufferedReader.readLine());
            //OutputStream outputGlobal=new ObjectOutputStream(socket.getOutputStream());
            OutputStream out=socket.getOutputStream();
            File file=new File("E:\\xubuntu-14.04.4-desktop-i386.iso");
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
        }
    }




    private void prepareWindow()
    {
        dirOutTextArea=new JTextArea("C:\\Users\\Mateusz\\Desktop");
        chooseFile=new JButton("Wybierz plik");
        startBuckup=new JButton("Rozpocznij przesyłanie");
        fileChooser=new JFileChooser();
        chosenNameLabel =new JLabel("Wybrany plik");
        chooseFile.addActionListener(this);
        startBuckup.addActionListener(this);
        JPanel contentFrame=(JPanel)this.getContentPane();
        contentFrame.setLayout(new BorderLayout());
        contentFrame.add(chosenNameLabel,BorderLayout.SOUTH);
        contentFrame.add(dirOutTextArea,BorderLayout.NORTH);
        contentFrame.add(chooseFile,BorderLayout.WEST);
        contentFrame.add(startBuckup,BorderLayout.CENTER);
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
        }
        else if(clicked==startBuckup)
        {

        }
    }

    private boolean Buckup(String file_src)
    {
        char tmp;
        int i=0;
        String fileName_tmp="";
        int file_src_length=file_src.length();
        while((tmp=file_src.charAt(file_src_length-1-i))!='\\')
        {
            fileName_tmp+=tmp;
            i++;
        }
        StringBuilder builder=new StringBuilder(fileName_tmp);
        String fileName=builder.reverse().toString();
        File file= new File(file_src);
        if(file.exists())
        {
            try {
                fis = new FileInputStream(file);
                inputLocal=new BufferedInputStream(fis);
                int fileLength=(int)file.length();
                byte[] bytesInput=new byte[fileLength];
                inputLocal.read(bytesInput,0,fileLength);
                outputGlobal=new ObjectOutputStream(socket.getOutputStream());
                outputGlobal.write(bytesInput,0,fileLength);
            }catch (IOException e){
                System.err.println("Cannot open source file");
                return false;
            }
            return true;
        }
        else return false;
    }
}
