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
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatClient implements Runnable{
    
    private String clientID = null;
    private String thisIP = "";
    private boolean isLogin = false;
    public static int ts = 0;
    private static int svPort = 4445;
    
    private Socket clientSocket = null;
    private DataInputStream console = null;
    private DataOutputStream streamOut = null;

    private Thread thread = null;
    private ClientThread clientThread = null;
    
    private MulticastSocket s = null;
    private InetAddress group =  null;
    public static PriorityQueue<Pair> msg_buffer;
    
    private String lastCmd = "";
    
    public ChatClient(String serverName, int serverPort){
        System.out.println("Establishing connection. Please wait ...");
        try {
            clientSocket = new Socket(serverName,serverPort);
            System.out.println("Connected: " + clientSocket);
            start();
            Scanner sc = new Scanner(System.in);
        
            showCommand();
            while(true){
                System.out.print(">>");
                String inputCommand = sc.nextLine();
                lastCmd = inputCommand;
                runCommand(inputCommand);
            }
        } catch (UnknownHostException e) {
            System.out.println("Host unknown : "+e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF : "+e.getMessage());
            System.out.println("Server 1 is downed try to connecting server2");
            try {
                //change port to Server 2
                svPort = 4446;
                clientSocket = new Socket(serverName,svPort);
                System.out.println("Connected: " + clientSocket);
                start();
                
                runCommand(lastCmd);
                
                Scanner sc = new Scanner(System.in);
                showCommand();
                while(true){
                    System.out.print(">>");
                    String inputCommand = sc.nextLine();
                    lastCmd = inputCommand;
                    runCommand(inputCommand);
                }
            } catch (IOException ex) {
                System.out.println("S2 IO : "+ex.getMessage());
            }
        } catch (IOException e) {
            System.out.println("IO : "+e.getMessage());
            
        }
    }

    public void sendCommandToServer (String message){
        try {
            streamOut.writeUTF(message);
            streamOut.flush();
        } catch (IOException e) {
            System.out.println("Sending error : "+e.getMessage());
        }
    }
    
    public void showCommand() {
        System.out.println("======================================================");
        if(!isLogin) 
            System.out.println("Hello, Guest");
        else 
            System.out.println("Hello, " + clientID);
        System.out.println("Please input command.");
        System.out.println(" - createUser userID : to create a user with id is \"userID\"");
        System.out.println(" - login userID : to login with id is \"userID\"");
        System.out.println(" - createGroup groupID : to create a group with id is \"groupID\"");
        System.out.println(" - joinGroup groupID : to join the group with id is \"groupID\"");
        //System.out.println(" - post groupID : to send a message to the group with id is \"groupID\"");
        System.out.println(" - quit : to exit the chat program");
        System.out.println("======================================================");
    }
    
    public void runCommand(String inputCommand) throws IOException{
         //split the input text
        String[] command = inputCommand.split(" ");
        
        // run createUser command
        if(command[0].equalsIgnoreCase("createUser")){
            //check command syntax
            if(command.length!=2){
                System.out.println("Command format incorrect!");
                return;
            }
            //send command
            String message = "createUser " + command[1];
            sendCommandToServer(message);
            
            //read return message
            String line = console.readUTF();
            System.out.println("return : "+line);
//            if(line.equalsIgnoreCase("yes"))
//                System.out.println("OK");
//            else
//                System.out.println("Username already exists, please try again.");
        }
        
        // run login command
        if(command[0].equalsIgnoreCase("login")){
            //check command syntax
            if(command.length!=2){
                System.out.println("Command format incorrect!");
                return;
            }
            //send command
            String message = "login " + command[1];
            sendCommandToServer(message);
            
            //read return message
            String line = console.readUTF();
            System.out.println("return : "+line);
            
            if(line.equalsIgnoreCase("Login Success")){
                clientID = command[1];
                isLogin = true;
            }
        }
        
        //run createGroup command
        else if(command[0].equalsIgnoreCase("createGroup")){
            //check command syntax
            if(command.length!=2){
                System.out.println("Command format incorrect!");
                return;
            }
            String gid = command[1];
             //send command
            String message = "createGroup " + gid;
            sendCommandToServer(message);
            
            //read return message
            String line = console.readUTF();
            System.out.println("return : "+line);
        }
        
        //run joinGroup command
        else if(command[0].equalsIgnoreCase("joinGroup")){
            if(!isLogin){
                System.out.println("Please log in");
            } else {
                //check command syntax
                if(command.length!=2){
                    System.out.println("Command format incorrect!");
                    return;
                }
                String gid = command[1];
                String message = "joinGroup " + gid;
                sendCommandToServer(message);

                //read return message
                String line = console.readUTF();
                String[] ret = line.split(" ");
                if(ret[0].equalsIgnoreCase("OK")){
                    thisIP = ret[1];
                    try {
                        s = new MulticastSocket(4444);
                        group = InetAddress.getByName(thisIP);
                        System.out.println("Waiting for "+group.getHostName());
                        System.out.println("Multicast Receiver running at:"+ s.getLocalSocketAddress());
                        s.joinGroup(group);

                        System.out.println("Welcome to group : "+command[1]);
                        System.out.println("These are special command");
                        System.out.println(" - leave : to leave the group");
                        System.out.println(" - exit : to exit the group");
                        System.out.println("===================================");
                        start_thread();
                        Scanner sc = new Scanner(System.in);
                       
                        String ip = InetAddress.getLocalHost().getHostAddress();
                        
                        String text = "GETUNREAD " + clientID + " " + ip + " " +"END";
                        byte[] m = text.getBytes();
                        DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 4444);
                        s.send(messageOut);
                        Thread unread = new GetUnreadThread(5555);
                        unread.start();
                        
                        
                        ChatClient.msg_buffer = new PriorityQueue();
                        Thread displaythread = new DisplayThread(5,500);
                        displaythread.start();
                        
                        while(true){
                            text = sc.nextLine();
                            if(text.equalsIgnoreCase("leave")){
                                text = "LEAVE " + clientID + " " + "END";
                                m = text.getBytes();
                                messageOut = new DatagramPacket(m, m.length, group, 4444);
                                s.send(messageOut);
                                showCommand();
                                clientThread.close();
                                stop_thread();
                                break;
                            }
                            else if(text.equalsIgnoreCase("exit")){
                                text = "EXIT " + clientID + " " + "END";
                                m = text.getBytes();
                                messageOut = new DatagramPacket(m, m.length, group, 4444);
                                s.send(messageOut);
                                showCommand();
                                clientThread.close();
                                stop_thread();
                                break;
                            }
                            else { 
                                ts++;
                                Date javaDate = new java.util.Date();
                                long javaTime = javaDate.getTime();
                                Timestamp sqlTimestamp = new Timestamp(javaTime);
                                
                                text = clientID +" "+ts+" "+ sqlTimestamp.toString()+" "+ text;
                                m = text.getBytes();
                                
                                messageOut = new DatagramPacket(m, m.length, group, 4444);
                                s.send(messageOut);
                            }
                        }
                    } catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                } else {
                    System.out.println("return : "+line);
                }
            }
        }
        
        //run quit command
        else if(command[0].equalsIgnoreCase("quit")){
            sendCommandToServer("quit");
            stop();
            System.out.println("======================================================");
            System.out.println("Thank you for using chat program.");
            System.out.println("See you again!");
            System.exit(0);
        }
        else {
            System.out.println("Please enter correct command!");
        }
    }
    
    public void start_thread() {
        if(thread == null){
            thread = new Thread(this);
            thread.start();
        }
    }
    
    public void stop_thread() {
        if(thread != null){
            thread.stop();
            thread = null;
        }
    }
    
    public void start() throws IOException {
        //System.out.println("before get io");
        console = new DataInputStream(clientSocket.getInputStream());
        streamOut = new DataOutputStream(clientSocket.getOutputStream());
        //System.out.println("after get io");
    }
    
    public void stop() {
        try {
            if (console != null) console.close();
            if (streamOut != null) streamOut.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing : "+e.getMessage());
        }
    }
    
    public static void main(String [] args) throws IOException{
        //client = new ChatClient("localhost",4444);
        Scanner sc = new Scanner(System.in);
        System.out.print("Please enter Server Chat ip : ");
        String svIP = sc.nextLine();
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.out.println("Welcome to chat program, Please enter the commands ");
        ChatClient client  = new ChatClient(svIP,svPort);
    }

    @Override
    public void run() {
        clientThread = new ClientThread(s,group,clientID);
        clientThread.start();
    }
}


class Pair implements Comparable<Pair>{
    public int ts;
    public String msg;
    
    public Pair(int ts,String msg){
        this.ts = ts;
        this.msg = msg;
    }
    

    @Override
    public int compareTo(Pair o) {
        if (ts < o.ts || (ts == o.ts && msg.length()<o.msg.length()))  
            return -1;
        return 1;
    }
}


