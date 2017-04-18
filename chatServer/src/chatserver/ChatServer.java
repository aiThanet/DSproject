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
import chatserver.ChatServerThread;


public class ChatServer implements Runnable{

    /**
     * @param args the command line arguments
     */
    private ServerSocket serverSocket = null;
    //private static Socket clientSocket = null;
    //private static DataInputStream streamIn = null;
    private ChatServerThread client = null;
    private Thread thread = null;
    
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
    }
    
    public static void main(String[] args) {
        ChatServer chatServer = null;
        chatServer = new ChatServer(4447);
    }

    
    
}
