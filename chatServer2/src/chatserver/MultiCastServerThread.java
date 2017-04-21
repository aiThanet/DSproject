/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.IOException;
import java.net.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aithanet
 */
public class MultiCastServerThread extends Thread{
    private String gid = "";
    private String ip = "";
    private MulticastSocket multicastSocket = null;
    private InetAddress group = null;
    private Statement stmt = null;
    private Connection conn = null;
    private int ts = 0;
    private int mid = 0;
    
    
    public MultiCastServerThread(String gid,String ip,Connection conn,Statement stmt){
        this.gid = gid;
        this.ip = ip;
        this.conn = conn;
        this.stmt = stmt;
        
    }
    
    public void run(){
        System.out.println("MultiCast Thread is running");
        try {
            
            //System.setProperty("java.net.preferIPv4Stack", "true");
            multicastSocket = new MulticastSocket(4444);
            
            group = InetAddress.getByName(ip);
            System.out.println("MultiCast Thread is created : "+ip);
            multicastSocket.joinGroup(group);
            stmt = null;
            stmt = conn.createStatement();
            String sql = "SELECT COUNT(*) AS rowcount FROM chat WHERE gid ='"+gid+"'";
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            mid = rs.getInt("rowcount");
            rs.close();
            while(true) {
                
                byte[] buffer = new byte[50]; 
//                String text = "clientID";
//                byte [] m = text.getBytes();
//                DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 4444);
//                multicastSocket.send(messageOut);
//                
                
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(messageIn);
                
                String text = new String(messageIn.getData());
                String[] ntext = text.split(" ");
                String uid = ntext[1];
                if(ntext[0].equalsIgnoreCase("EXIT")){
                    sql = "DELETE FROM exitUser WHERE uid ='"+uid+"' and gid ='"+gid+"'";
                    //sql = "DELETE FROM exit WHERE uid ='u2' AND gid ='a'";
                    stmt.executeUpdate(sql);
                    sql = "INSERT INTO exitUser VALUES ('"+uid+"', '"+gid+"', "+mid+")";
                    stmt.executeUpdate(sql);
                } else if(ntext[0].equalsIgnoreCase("GETUNREAD")){
                    //find last message
                    sql = "SELECT exittime FROM exitUser WHERE uid ='"+uid+"' and gid ='"+gid+"'";
                    ResultSet rss = stmt.executeQuery(sql);
                    int lastMessageID = Integer.MAX_VALUE;
                    if(rss.next()){
                        lastMessageID = rss.getInt("exittime");
                    }
                    //update lastMessage
                    sql = "UPDATE exitUser SET exittime='"+mid+"' WHERE uid ='"+uid+"' and gid ='"+gid+"'";
                    stmt.executeUpdate(sql);
                    rss.close();
                    sql = "SELECT mid,uid,message,timestamp FROM chat WHERE gid ='"+gid+"' and mid <= "+mid+" and mid > "+lastMessageID;
                    rss = stmt.executeQuery(sql);
                    Thread sendUnread = new SendUnreadThread(rss,ntext[2],5555);
                    sendUnread.start();
                } else if(ntext[0].equalsIgnoreCase("LEAVE")){
                    int lastMessageID = Integer.MAX_VALUE;
                    //update lastMessage
                    sql = "UPDATE exitUser SET exittime='"+lastMessageID+"' WHERE uid ='"+uid+"' and gid ='"+gid+"'";
                    stmt.executeUpdate(sql);
                    //System.out.println("c3");
                } else {
                    ts = Integer.parseInt(ntext[1]);
                    System.out.println(ntext[0]+"("+ntext[2]+" "+ntext[3]+") : " + ntext[1] + " : " + ntext[4]);
                    mid++;
                    DateFormat df = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss.SSS"); 
                    Date date = df.parse(ntext[2]+" "+ntext[3]);
                    Timestamp sqlDate = new Timestamp(date.getTime());
                   
                    sql = "INSERT INTO chat VALUES (?,?,?,?,?,?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1,mid);
                    pstmt.setString(2, gid);
                    pstmt.setString(3, ntext[0]);
                    pstmt.setString(4, ntext[4]);
                    pstmt.setInt(5,Integer.parseInt(ntext[1]));
                    pstmt.setTimestamp(6,sqlDate);
                    pstmt.executeUpdate();
                    
                }
            }
          
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Group "+ gid +" is closed");
    }
}
