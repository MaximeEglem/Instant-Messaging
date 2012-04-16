
package instant.messaging.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

class ClientWorker implements Runnable {
  
  private Socket client;
  public String recievedUsername;
  public String recievedPassword;
  BufferedReader in;
  PrintWriter out;

  //Constructor
  ClientWorker(Socket client) {
    this.client = client;
  }

  public void writeMessage (String message){
        try {
            out = new PrintWriter(client.getOutputStream(), true);
            out.println(message);
            out.flush();
            System.out.println("OUT: "+message);
        } catch (IOException ex) {
            Logger.getLogger(ClientWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
  }
  //get the username and password of the first packet
  public void getCredentials(){
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
            
            //extract username and password out of the initial connection
            String initalData = in.readLine();
            String[] contactInformation = initalData.split(":");
            recievedUsername = contactInformation[0];
            recievedPassword = contactInformation[1];
            
        } catch (IOException ex) {
            Logger.getLogger(ClientWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
  }
  
    @Override
  public void run(){
    ServerLogic sl = new ServerLogic();
    
    try{
      in = new BufferedReader(new InputStreamReader(client.getInputStream()));
      out = new PrintWriter(client.getOutputStream(), true);
    
      //welcome message print
      writeMessage("Welcome to chat server 0.1 beta \n");
      
      System.out.println("User: "+recievedUsername+" logged in with password: "+recievedPassword);
      //KNOWN BUG SEND BUTTON HAS TO BE HIT 2 TIMES
      while (true){
         
        String fullMessage = in.readLine().toString();
        System.out.println("INCOMING: "+fullMessage);
        String[] cutMessage = fullMessage.split("]");
        String forUser = cutMessage[0].replace("[", "");
        /////////////////////////////////////////////////////////////////////////////////////
        //ClientWorker userThread = sl.getThreadFromUser(forUser);
        //ClientWorker userThread = sl.getThreadFromUser("stefan");
        //userThread.writeMessage("testmessage");
       
        System.out.println("Message for user: "+cutMessage[0].replace("[", ""));
        System.out.println("Message: "+cutMessage[1]);
        ////////////////////////////////////////////////////////////////////////////////////
        
        
        /*
        if (in.readLine() == "/getofflineUser") {
            System.out.println("offline user angefordert");
            out.print(sl.getOfflineUsers());
        } 
        if (in.readLine() == "/getonlineUser") {
        
        }
        
        */
              
        writeMessage(recievedUsername+": "+cutMessage[1]);

      }
    } catch (IOException e) {
      System.out.println("in or out failed");
      System.exit(-1);
    }
  }
}
