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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServerThread extends Thread {
    private Socket clientSocket = null;
    private ChatServer server = null;
    private int ID = ChatServer.threadID+1;
    private DataInputStream streamIn = null;
    private DataOutputStream streamOut = null;
    private String clientID = "Guest";
    private MulticastSocket multicastSocket = null;
    
    
    public  ChatServerThread(ChatServer server, Socket socket){
        ChatServer.threadID++;
        this.server = server;
        this.clientSocket = socket;
    }
    
    public void run(){
        System.out.println("Server Thread " + ID + " running.");
        boolean done = false;
        while (!done){
            try {
                String line = streamIn.readUTF();
                if(!line.equalsIgnoreCase("CHECK"))
                    System.out.println(clientID + " -> "+line);
                readCommand(line);
                done = line.equalsIgnoreCase("quit");
            } catch (IOException e){
                System.out.println("Error run: " + e.getMessage());
                done = true;
            } catch (SQLException e) {
                System.out.println("Error sql run: " + e.getMessage());
            }
        }
    }
    
    public void readCommand(String inputCommand) throws IOException, SQLException {
        String[] command = inputCommand.split(" ");
        try {            
            ChatServer.stmt = null;
            ChatServer.stmt = ChatServer.conn.createStatement();
        } catch (SQLException e) {
            System.out.println("Conn error : " + e.getMessage());
        }
        // run createUser command
        String messageBack = "";
        if(command[0].equalsIgnoreCase("createUser")){
            String sql = "INSERT INTO user VALUES ('"+command[1]+"')";
            messageBack = "OK user " + command[1];
            try {
                ChatServer.stmt.executeUpdate(sql);
                ChatServer.stmt.close();
            } catch (SQLException e) {
                System.out.println("insert user error : " + e.getMessage());
                messageBack = "cant not create user " + command[1];
            }
            streamOut.writeUTF(messageBack);
        }
        
        if(command[0].equalsIgnoreCase("login")){
            String sql = "SELECT uid FROM user WHERE uid ='"+command[1]+"'";
            ResultSet rs = ChatServer.stmt.executeQuery(sql);
            if(rs.next()){
                messageBack = "Login Success";
                clientID = command[1];
            } else {
                messageBack = "Your account is wrong!";
            }
            rs.close();
            ChatServer.stmt.close();
            streamOut.writeUTF(messageBack);
        }
        
        // run createGroup command
        else if(command[0].equalsIgnoreCase("createGroup")){
            try {
                String sql = "SELECT COUNT(*) AS rowcount FROM groupchat";
                ResultSet rs = ChatServer.stmt.executeQuery(sql);
                rs.next();
                int count = rs.getInt("rowcount");
                rs.close();
                String ip = "230.0."+Integer.toString(count/255)+"."+Integer.toString(count%255);
                sql = "INSERT INTO groupchat VALUES ('"+command[1]+"', '"+ip+"')";
                ChatServer.stmt.executeUpdate(sql);
                
                System.out.println("Create multicastServer thread");
                Thread  multi = new MultiCastServerThread(command[1],ip,ChatServer.conn,ChatServer.stmt);
                multi.start();
                
                ChatServer.stmt.close();
                
                //multicastSocket = new MulticastSocket(4444);
                //InetAddress group = InetAddress.getByName(ip);
                //System.out.println("Waiting for "+group.getHostName());
                //System.out.println("Multicast Receiver running at:"+ multicastSocket.getLocalSocketAddress());
               
                
                messageBack = "OK "+ip;
                //multicastSocket.joinGroup(group);
            } catch (SQLException e) {
                System.out.println("insert group error : " + e.getMessage());
                messageBack = "Can not create group " + command[1];
            }
            streamOut.writeUTF(messageBack);
            
        }
        
        // run joinGroup command
        else if(command[0].equalsIgnoreCase("joinGroup")){
            String sql = "SELECT gid, ip FROM groupchat WHERE gid ='"+command[1]+"'";
            ResultSet rs = ChatServer.stmt.executeQuery(sql);
            if(rs.next()){
                String ip = rs.getString("ip");
                messageBack = "OK "+ ip;
            } else {
                messageBack = "Your groupID doesn't exist.";
            }
            rs.close();
            ChatServer.stmt.close();
            streamOut.writeUTF(messageBack);
        }
        else if(command[0].equalsIgnoreCase("CHECK")){
            messageBack = "OK";
            streamOut.writeUTF(messageBack);
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
