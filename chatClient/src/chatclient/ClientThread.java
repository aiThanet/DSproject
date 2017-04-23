/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import chatclient.ChatClient;
import java.util.PriorityQueue;

/**
 *
 * @author aithanet
 */
public class ClientThread extends Thread{
    
    private String thisIP ="";
    private MulticastSocket multicastSocket = null;
    private InetAddress group = null;
    private String id = "";
    
    
    public ClientThread(MulticastSocket multicastSocket,InetAddress group,String id){
        this.multicastSocket = multicastSocket;
        this.group = group;
        this.id = id;
    }
    
    public void run(){
        System.out.println("Thread is running.");
        
        
        //System.setProperty("java.net.preferIPv4Stack", "true")
        try {
            while(true) {
                byte[] buffer = new byte[200]; 
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(messageIn);
                
                String text = new String(messageIn.getData());
                String[] ntext = text.split(" ");
                
                if((ntext[0].equalsIgnoreCase("EXIT") || ntext[0].equalsIgnoreCase("LEAVE")) && ntext[1].equalsIgnoreCase(id)){
                    break;
                } else if (ntext[0].equalsIgnoreCase("GETUNREAD") || ntext[0].equalsIgnoreCase("EXIT") || ntext[0].equalsIgnoreCase("LEAVE")) {
                    continue;
                } else {
                    String message ="";
                    int i =4;
                    while(i<ntext.length){
                        message = message + " " + ntext[i];
                        i++;
                    }
                    String line = ntext[0]+"("+ntext[2]+" "+ntext[3]+") :"+message;
                    Pair new_msg = new Pair(Integer.parseInt(ntext[1]),line);
                    ChatClient.msg_buffer.add(new_msg);
                    int tj = Integer.parseInt(ntext[1]);
                    if(tj>ChatClient.ts)
                    ChatClient.ts = tj + 1;
                }   
            }
        } catch (IOException ex) {
            System.out.print("");
        }
        
    }
    
    public void close() throws IOException {
        multicastSocket.leaveGroup(group);
        multicastSocket.close();
    }
}
