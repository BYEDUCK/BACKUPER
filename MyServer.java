package BACKUPER;

import javax.swing.*;
import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;


public class MyServer extends JFrame{


    public static final String ready = "READY";
    //public static String path = "E:\\BackuperKopie\\";
    private static OutputStream outputStream;
    private static PrintWriter printWriter;
    private static Random random;
    public static int transferPort;


    public static void main(String[] args) {
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

    public static boolean isAvailable(int port) { //coś takiego radzili zrobić na Stack Overflow
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
}