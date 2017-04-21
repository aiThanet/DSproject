/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.sql.ResultSet;
import java.net.*;
import java.io.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aithanet
 */
public class SendUnreadThread extends Thread{
    
    private String ip = "";
    private ResultSet rs = null;
    private int port = 0;
    
    private Socket socket              = null;
    private DataOutputStream streamOut = null;
    
    public SendUnreadThread(ResultSet rs,String ip,int port){
        this.rs = rs;
        this.ip = ip;
        this.port = port;
    }
    
   public void run(){
       System.out.println("Establishing connection. Please wait ... : " + ip);
        try {
            
            socket = new Socket(ip,port);
            System.out.println("Connected: " + socket);
            streamOut = new DataOutputStream(socket.getOutputStream());
            System.out.println("Start send..");
            String line = "";
            while(rs.next()){
                int mid = rs.getInt("mid");
                String uid = rs.getString("uid");
                String message = rs.getString("message");
                Timestamp ts = rs.getTimestamp("timestamp");
                line = uid + "("+ts.toString()+") : " + message;
                //System.out.println("send -> " + line);
                streamOut.writeUTF(line);
            }
            streamOut.writeUTF("DONE");
            if (streamOut != null)  streamOut.close();
            if (socket != null)  socket.close();
            //this.stop();
            //System.out.println("closed");
        } catch (IOException e) {
            System.out.println("Unexpected exception: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("SQL : " + e.getMessage());
        }
   }
  
    
}
