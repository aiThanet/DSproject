/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

/**
 *
 * @author aithanet
 */

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

     
public class GetUnreadThread extends Thread{
    
    private ServerSocket serverSocket = null;
    private Socket clientSocket = null;
    private DataInputStream streamIn = null;
    private int port = 0;
    
    public GetUnreadThread(int port){
        this.port = port;
    }
    
    public void run(){
        try {
            System.out.println("Binding to port " + port + ", please wait  ...");
            serverSocket = new ServerSocket(port);
            //System.out.println("Server started: " + serverSocket);
            //System.out.println("Waiting for Chat server ...");
            clientSocket = serverSocket.accept();
            System.out.println("Client accepted: " + clientSocket);
            streamIn = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            
            while (true){
                String line = streamIn.readUTF();
                if(line.equalsIgnoreCase("DONE"))
                        break;
                String [] lines = line.split(" ");
                int index = lines[1].indexOf(".");
                String new_line = lines[1].substring(0,index);
                new_line = lines[0]+" "+new_line +"): "+lines[3];
                System.out.println(new_line);
            }
            if(streamIn != null) streamIn.close();
            if(clientSocket != null) clientSocket.close();
            if(serverSocket != null) serverSocket.close();
            //this.stop();
            //System.out.println("closed");
            //client = new ChatServerThread(this,acceptClients());
        } catch (IOException e) {
           System.out.println("Getunread error : "+e.getMessage());
        } 
    }
    
}
