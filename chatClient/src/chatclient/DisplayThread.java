/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aithanet
 */




public class DisplayThread extends Thread{
    
    private int size;
    private int timeout;
    
    public DisplayThread(int size,int timeout){
        this.size = size;
        this.timeout = timeout;
    }
    
    
    
    public void run(){
        System.out.println("Start Display");
        int timer = 0;
        while(true){
            timer++;
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(DisplayThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(timer == timeout){
                displayMsg();
                timer = 0;
            } 
            if(ChatClient.msg_buffer.size() >= size){
                System.out.println("Print > size");
                displayMsg();
            }
            
                
        }
    }
    
    public void displayMsg(){
        PriorityQueue<Pair> msg_buffer = new PriorityQueue<>(ChatClient.msg_buffer);
        ChatClient.msg_buffer.clear();
        while(!msg_buffer.isEmpty()){
            Pair msg = msg_buffer.poll();
            String msgg = msg.msg;
            String [] line = msgg.split(" ");
            String message ="";
            int i =3;
            while(i<line.length){
                message = message + " " + line[i];
                i++;
            }
            int index = line[1].indexOf(".");
            String new_line = line[1].substring(0,index);
            new_line = line[0]+" "+new_line +"):"+message;
            System.out.println(new_line);
        }
    }
    
    
    
}
