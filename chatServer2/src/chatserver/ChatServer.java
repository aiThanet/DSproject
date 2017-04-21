/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

/**
 *
 * @author aithanet
 */

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mysql.jdbc.Driver;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ChatServer implements Runnable{

    /**
     */
    static final String myDriver = "com.mysql.jdbc.Driver";
    static final String myUrl = "jdbc:mysql://localhost/dsProject";
    static final String USER = "root";
    static final String PASS = "12345678";
    public static Statement stmt = null;
    public static Connection conn = null;
    
    public static int threadID = 0;
    private ServerSocket serverSocket = null;
    private ChatServerThread client = null;
    private Thread thread = null;
    
    public static boolean isS1Fail = false;
    
    public ChatServer() {
        
    }
    
    public ChatServer (int port) {
        try {
            System.out.println("Binding to port " + port + ", please wait  ...");
            serverSocket = new ServerSocket(port);
            System.out.println("Server started: " + serverSocket);
            start();
        } catch(IOException e){
            System.out.println("Listen : "+e.getMessage());
        }
    }

    public void run() {
        while (thread != null){
            System.out.println("Waiting for a client ...");
            addThread();
        }
    }
    
    public void addThread(){
        client = new ChatServerThread(this,acceptClients());
        try {
            client.open();
            client.start();
        } catch(IOException e){
            System.out.println("Error add Thread: "+e.getMessage());
        }
    }
    
    public Socket acceptClients() {
        try {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client accepted: " + clientSocket);
            return clientSocket;
        } catch(IOException e){
            System.out.println("Accept failed : "+e.getMessage());
            return null;
        }
    }
    
    
    public void start(){
        if(thread == null){
            thread = new Thread(this);
            thread.start();
        }
    }
    
    public void stop() {
        if(thread != null){
            thread.stop();
            thread = null;
        }
//        try {
//            serverSocket.close();
//        } catch (IOException ex) {
//            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        
        ChatServer chatServer = null;
        chatServer = new ChatServer(4446);
        
        //System.out.println("start check status s1");
        Thread thread = new CheckFailThread("localhost",4445);
        thread.start();
        
        System.out.println("Wait Fail..");
        while(true){
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException ex) {
                Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("status : "+isS1Fail);
            if(isS1Fail){
                System.out.println("start database...");
                try {
                    Class.forName(myDriver);

                    System.out.println("Connecting to database...");
                    conn = DriverManager.getConnection(myUrl,USER,PASS);
                    System.out.println("Connection Ok");


                    System.out.println("starting multicast group server...");
                    String sql = "SELECT gid, ip FROM groupchat";
                    stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);

                    while(rs.next()){
                        String gid = rs.getString("gid");
                        String ip = rs.getString("ip");
                        Thread  multi = new MultiCastServerThread(gid,ip,conn,stmt);
                        multi.start();
                    }
                    rs.close();

                    //stmt.close();
                } catch (ClassNotFoundException e) {
                    System.out.println("class error : " + e.getMessage());
                } catch (SQLException e) {
                    System.out.println("sql error : " + e.getMessage());
                }
    
                break;
            }
        }
        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
        if(line.equalsIgnoreCase("bye")){
            chatServer.stop();
            System.exit(0);
        }
    }

    
    
}
