package BACKUPER;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;


public class MyServer extends JFrame implements ActionListener{


    private static OutputStream outputStream;
    private static PrintWriter printWriter;
    private static Random random;
    public static int transferPort;
    private JLabel label;
    private JButton disconnectButton;
    private JButton changePathButton;
    protected static DefaultListModel clientsList;
    private JList usersList;

    public MyServer() {
        setSize(500,450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setTitle("Server");
        label = new JLabel("Lista klientów:");
        disconnectButton = new JButton("Rozłącz");
        changePathButton = new JButton("Zmień ścieżkę");
        clientsList = new DefaultListModel();
        usersList = new JList(clientsList);
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        label.setBounds(200,20,150,25);
        disconnectButton.setBounds(70,300,150,50);
        changePathButton.setBounds(250,300,150,50);
        usersList.setBounds(20,50,440,220);
        add(label);
        add(changePathButton);
        add(usersList);
        add(disconnectButton);
        changePathButton.addActionListener(this);
        disconnectButton.addActionListener(this);
        setVisible(true);
    }


    public static void main(String[] args) {
        new MyServer();
        while (true) {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(12129);
            } catch (Exception e){
                System.err.println("Create new meta socket error: " +e);
            }
            while(true) {
                try {
                    Socket setterSocket = serverSocket.accept();
                    outputStream = setterSocket.getOutputStream();
                    printWriter = new PrintWriter(outputStream, true);
                    random = new Random();
                    transferPort = random.nextInt(65535);
                    while(isAvailable(transferPort) == false) {
                        transferPort = random.nextInt(65535);
                    }
                    printWriter.println(transferPort);
                    setterSocket.close();
                    Runnable runnable = new MyThread(transferPort);
                    Thread transferThread = new Thread(runnable);
                    transferThread.start();
                } catch (Exception e) {
                    System.out.println("Creating meta socket problem: " + e);
                }
            }
        }
    }

    public static boolean isAvailable(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object click = e.getSource();
            if (click == changePathButton) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                MyThread.defaultPath = file.getPath();
                JOptionPane.showMessageDialog(null, file);
            }
        }
        else if (click == disconnectButton) {
                printWriter.println(MyProtocol.LOGOUT);
                String TMP = usersList.getSelectedValue().toString();
                MyServer.clientsList.removeElement(TMP);
                try{
                    MyThread.socket.close();
                    MyThread.transferSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
    }
}