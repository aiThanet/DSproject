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
import chatserver.ChatServer;
import java.util.ArrayList;
import java.util.List;

public class ChatServerThread extends Thread {
    private Socket clientSocket = null;
    private ChatServer server = null;
    private int ID = -1;
    private DataInputStream streamIn = null;
    private DataOutputStream streamOut = null;
    private static List<String> clientIDs = new ArrayList<String>();;
    
    public  ChatServerThread(ChatServer server, Socket socket){
        this.server = server;
        this.clientSocket = socket;
    }
    
    public void run(){
        System.out.println("Server Thread " + ID + " running.");
        boolean done = false;
        //while (!done){
            try {
                String line = streamIn.readUTF();
                System.out.println(line);
                readCommand(line);
            } catch (IOException e){
                System.out.println("Error run: " + e.getMessage());
                //done = true;
            }
        //}
    }
    
    public void readCommand(String inputCommand) throws IOException {
        String[] command = inputCommand.split(" ");
        
        // run createUser command
        if(command[0].equalsIgnoreCase("createUser")){
            //check command syntax
            if(command.length!=2){
                System.out.println("Command format incorrect!");
                return;
            }
            if(clientIDs.contains(command[1])){
                System.out.println("cant not create user " + command[1]);
                streamOut.writeUTF("NO");
            }
            else {
                clientIDs.add(command[1]);
                System.out.println("create user " + command[1] + "complete!");
                streamOut.writeUTF("YES");
            }
        }
    }
    
    public void open() throws IOException {
        streamIn = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
        streamOut = new DataOutputStream(clientSocket.getOutputStream());
    }
    
    public void close() throws IOException {
        if(clientSocket != null) clientSocket.close();
        if(streamIn != null) streamIn.close();
        if(streamOut != null) streamOut.close();
    }
}
