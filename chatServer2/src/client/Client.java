/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 *
 * @author aithanet
 */

import java.net.*;
import java.io.*;

public class Client {
    
    private static Socket clientSocket = null;
    private static DataInputStream console = null;
    private static DataOutputStream streamOut = null;
    
    public Client(String serverName, int serverPort){
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
        String line = "";
        while (!line.equals(".bye")){
            try {
                line = console.readLine();
                streamOut.writeUTF(line);
                streamOut.flush();
            } catch (IOException e) {
                System.out.println("Sending error : "+e.getMessage());
            }
        } 
    }
    
    public static void start() throws IOException {
        console = new DataInputStream(System.in);
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
    
    public static void main(String [] args){
        Client client = null;
        client = new Client("localhost",4444);
//        if (args.length != 1)
//            System.out.println("Please enter only ChatServer name and port.");
//        else
//            client = new Client(args[0],Integer.parseInt(args[1]));
    }
}
