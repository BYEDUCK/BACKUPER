package Multithreded;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
//import java.io.EOFException;

public class Server extends JFrame implements ActionListener {


    private JButton choosePathButton;
    public static final String ready = "READY";
    public static String path = "D:\\Users\\mpars\\Desktop";
    private static OutputStream outputStream;
    private static PrintWriter printWriter;
    private static Random random;
    public static int transferPort;

    public Server()
    {
        setSize(500, 400);
        setTitle("Nowe połączenie");
        setLayout(null);
        setVisible(true);

        choosePathButton = new JButton("Wybierz");
        choosePathButton.setBounds(200,150,100,100);
        choosePathButton.addActionListener(this);
        add(choosePathButton);
    }

    public static void main(String[] args)
    {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(12129);
        } catch (Exception e){
            System.err.println("Create new meta socket error: " +e);
        }
        while(true) {
            try {
                new Server();
                Socket setterSocket = serverSocket.accept();
                outputStream = setterSocket.getOutputStream();
                printWriter = new PrintWriter(outputStream,true);
                random = new Random();
                transferPort = random.nextInt(15000);
                System.out.println(transferPort);
                printWriter.println(transferPort);
                Runnable runnable = new Thread1();
                Thread transferThread = new Thread(runnable);
                transferThread.start();
                setterSocket.close();
            } catch (Exception e) {
                System.out.println("Creating meta socket problem: " +e);
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
                System.out.println(file.getPath());
                JOptionPane.showMessageDialog(null, file);
            }
        }
    }
}