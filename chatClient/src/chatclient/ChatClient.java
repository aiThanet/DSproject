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
import java.util.Scanner;

public class ChatClient {
    
    private static Socket clientSocket = null;
    private static DataInputStream console = null;
    private static DataOutputStream streamOut = null;
    private static String clientID = null;
    //private static boolean isLogin = false;
    
    public ChatClient(String serverName, int serverPort,String message){
        System.out.println("Establishing connection. Please wait ...");
        try {
            clientSocket = new Socket(serverName,serverPort);
            System.out.println("Connected: " + clientSocket);
            start();
        } catch (UnknownHostException e) {
            System.out.println("Host unknown : "+e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF : "+e.getMessage());
        } catch (IOException e) {
            System.out.println("IO : "+e.getMessage());
        }
        try {
            streamOut.writeUTF(message);
            streamOut.flush();
        } catch (IOException e) {
            System.out.println("Sending error : "+e.getMessage());
        }
        
    }
    
    public static void showCommand() {
        System.out.println("======================================================");
        System.out.println("Please input command.");
        System.out.println(" - createUser userID : to create a user with id is \"userID\"");
        System.out.println(" - createGroup groupID : to create a group with id is \"groupID\"");
        System.out.println(" - joinGroup groupID : to join the group with id is \"groupID\"");
        System.out.println(" - leaveGroup groupID : to leave the group with id is \"groupID\"");
        System.out.println(" - exitGroup groupID : to exit the group with id is \"groupID\"");
        System.out.println(" - getUnread groupID : to get messages that are sended when you exit the group with id is \"groupID\"");
        System.out.println(" - post groupID : to send a message to the group with id is \"groupID\"");
        System.out.println(" - quit : to exit the chat program");
        System.out.println("======================================================");
    }
    
    public static void runCommand(String inputCommand) throws IOException{
         //split the input text
        String[] command = inputCommand.split(" ");
        
        // run createUser command
        if(command[0].equalsIgnoreCase("createUser")){
            //check command syntax
            if(command.length!=2){
                System.out.println("Command format incorrect!");
                return;
            }
            clientID = command[1];
            //create a user
            String message = "createUser " + clientID;
            ChatClient client  = new ChatClient("localhost",4447,message);
            String line = console.readUTF();
            if(line.equalsIgnoreCase("yes"))
                System.out.println("OK");
            else
                System.out.println("Username already exists, please try again.");
        }
        
        //run createGroup command
        else if(command[0].equalsIgnoreCase("createGroup")){
            //check command syntax
            if(command.length!=2){
                System.out.println("Command format incorrect!");
                return;
            }
            String gid = command[1];
           //run createGroup
        }
        
        //run joinGroup command
        else if(command[0].equalsIgnoreCase("joinGroup")){
            //check command syntax
            if(command.length!=2){
                System.out.println("Command format incorrect!");
                return;
            }
            String gid = command[1];
           //run joinGroup
        }
        
        //run leaveGroup command
        else if(command[0].equalsIgnoreCase("leaveGroup")){
            //check command syntax
            if(command.length!=2){
                System.out.println("Command format incorrect!");
                return;
            }
            String gid = command[1];
           //run leaveGroup
        }
        
        //run getUnread command
        else if(command[0].equalsIgnoreCase("getUnread")){
            //check command syntax
            if(command.length!=2){
                System.out.println("Command format incorrect!");
                return;
            }
            String gid = command[1];
           //run getUnread
        }
        
        //run getUnread command
        else if(command[0].equalsIgnoreCase("getUnread")){
            //check command syntax
            if(command.length!=2){
                System.out.println("Command format incorrect!");
                return;
            }
            String gid = command[1];
           //run getUnread
        }
        
        //run exitGroup command
        else if(command[0].equalsIgnoreCase("post")){
            //check command syntax
            if(command.length!=2){
                System.out.println("Command format incorrect!");
                return;
            }
            String gid = command[1];
           //run post
        }
        
        //run quit command
        else if(command[0].equalsIgnoreCase("quit")){
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
    
    public void start() throws IOException {
        console = new DataInputStream(clientSocket.getInputStream());
        streamOut = new DataOutputStream(clientSocket.getOutputStream());
    }
    
    public static void stop() {
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
        System.out.println("Welcome to chat program, Please enter the commands ");
        Scanner sc = new Scanner(System.in);
        showCommand();
        while(true){
            System.out.print(">>");
            String inputCommand = sc.nextLine();
            runCommand(inputCommand);
        }
    }
}
