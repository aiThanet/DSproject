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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CheckFailThread extends Thread{
    
    private String ip = "";
    private int port = 0;
    
    private Socket socket = null;
    private DataInputStream streamIn = null;
    private DataOutputStream streamOut = null;
    
    public CheckFailThread(String ip,int port){
        this.ip = ip;
        this.port = port;
    }
    
    public void run(){
        try {
            socket = new Socket(ip,port);
            System.out.println("Connected: " + socket);
            streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            streamOut = new DataOutputStream(socket.getOutputStream());
            System.out.println("Start checking status..");
            
            while(true){
                TimeUnit.MILLISECONDS.sleep(10);
                String line = "CHECK";
                streamOut.writeUTF(line);
                line = streamIn.readUTF();
                if(line.equalsIgnoreCase("OK")){
                    continue;
                }
                
            }
             
        }  catch (IOException e) {
            System.out.println("check status s1 error : "+e.getMessage());
            ChatServer.isS1Fail = true;
            System.out.println("is Fail : "+ChatServer.isS1Fail);
            
        } catch (InterruptedException ex) {
            Logger.getLogger(CheckFailThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
