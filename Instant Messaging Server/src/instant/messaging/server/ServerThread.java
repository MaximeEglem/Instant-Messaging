package instant.messaging.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;

import java.io.File;
import java.io.FilenameFilter;



class ServerThread implements Runnable {

    private Socket client;
    HashMap<String, ServerThread> connectedClients;
    public String recievedUsername;
    public String recievedPassword;
    DataInputStream in;
    DataOutputStream out;
    ServerLogic ser;

    //Constructor
    ServerThread(Socket client) {
        this.client = client;
    }

    //get the username and password of the first packet
    public void getCredentials() {
        try {
            System.out.println("in getcredentials");
            in = new DataInputStream(client.getInputStream());
            out = new DataOutputStream(client.getOutputStream());
            //extract username and password out of the initial connection
            System.out.println("buffers initialized");
            String initalData = in.readUTF();
            System.out.println("nach readline");
            String[] contactInformation = initalData.split(":");
            recievedUsername = contactInformation[0];
            recievedPassword = contactInformation[1];
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setRecentHashMap(HashMap<String, ServerThread> connectedClients) {
        this.connectedClients = connectedClients;
      
    }
    //CURRENTLY UNDER CONSTRUCTION
    public void setRecentHashMapForUsers(HashMap<String, ServerThread> connectedClients) {
        
       String completeList = connectedClients.toString();
       completeList.replace("{", "");
       completeList.replace("}", "");
       String finishedlist = "";
       String[] singleUser = completeList.split(",");
       for (int i=0; i <= singleUser.length;i++) {
           String user = singleUser.toString();
            finishedlist = finishedlist + user.substring(0, user.indexOf("="));
            
       }

       System.out.println("this is the finished list: "+finishedlist);
        
    }

    
    public void writeMessage(String message) {
        try {
            out = new DataOutputStream(client.getOutputStream());
            out.writeUTF(message);
            out.flush();
            System.out.println("OUTGOING: " + message);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Check and display the chat history
    public void CheckChatHistory(String currentUser) {

        String path = "resources/"; 

        String files;
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles(); 

        //list all the files
        for (int i = 0; i < listOfFiles.length; i++) 
        {
            //check if it is not a folder
            if (listOfFiles[i].isFile()) 
            {
                files = listOfFiles[i].getName();
                //check the extention of the file
                if (files.endsWith(".txt"))
                {
                    //look if the user have old chat
                    if(files.matches(currentUser+"-.*")){
                        //Reconized the name of the sender user
                        String nameOfUser[] = files.split("-");
                        nameOfUser = nameOfUser[1].split(".txt");
                        System.out.println(nameOfUser[0]);
                        
                        //***********read the line **********//
                        try{

                            BufferedReader br = new BufferedReader(
                                                    new InputStreamReader(
                                                        new FileInputStream(path+files)));
                            String ligne;
                            while ((ligne = br.readLine())!=null){
                                writeMessageToOnlineUser(currentUser, nameOfUser[0], ligne);
                            }
                            
                           File f = new File(path+files);
                           boolean success = f.delete();

                            if (!success)
                            throw new IllegalArgumentException("Delete: deletion failed");
  
                    }
                    catch (Exception e){
                            System.out.println(e.toString());
                    }
                        
                        
                        
                        
                        //*********************//
                    }
                }
            }
        }
  
    }

    
    //writes a message to online user
    public void writeMessageToOnlineUser(String forUser, String fromUser, String message) {
        try {
            forUser.replaceAll(" ", "");
            ServerThread correspondingClient = connectedClients.get(forUser);
            correspondingClient.out.writeUTF(fromUser + ": " + message);
            correspondingClient.out.flush();
            System.out.println("for user: " + forUser + " OUTGOING: " + message);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    //writes an message to offline users, the message is stored in the userList file with a "::" separator

    private void writeMessageToOfflineUser(String forUser, String fromUser, String message) {
            ServerLogic sl = new ServerLogic();
            File file = new File("resources/"+forUser+"-"+fromUser+".txt"); 
            String ligne;
            
            Date MessageTime = new Date();
            ArrayList<String> filecontent = new ArrayList();
            
            //wrinting messaging
            addLine(file.getPath(),MessageTime + " : " + message);
            
    }
    
        public void addLine (String filename , String newLigne){
        
        BufferedWriter bufWriter = null;
        FileWriter fileWriter = null; 
  
        try {
            File file = new File(filename);
            fileWriter = new FileWriter(filename, true);
            bufWriter = new BufferedWriter(fileWriter);
             
            if (file.length() > 0)
            bufWriter.newLine();
            
            bufWriter.write(newLigne);
            bufWriter.close();
            
        } catch (IOException e) {
            System.out.println(e.toString());
            
        } finally {
            try {
                bufWriter.close();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
    }

    @Override
    public void run() {

        try {


            //welcome message print
            writeMessage("Welcome to chat server 0.1 beta \n");
            
            CheckChatHistory(recievedUsername);
            
            System.out.println("User: " + recievedUsername + " logged in with password: " + recievedPassword);

            while (true) {
                //prepare incoming message for sending to corresponding client
                String fullMessage = in.readUTF().toString();
                System.out.println("INCOMING: " + fullMessage);
                String[] cutMessage = fullMessage.split("]");
                String forUser = cutMessage[0].replace("[", "");

                //check if message is for multiple users
                // if "," is contained within the forUser string it is a message for multiple users
                if (forUser.toLowerCase().contains("chat") == true) {
                    System.out.println("message is for broadcast");
                    //iterate the whole hashmap to send a message to all online users
                    for (String currentUser : connectedClients.keySet()) {
                        if (!currentUser.equals(recievedUsername)) {
                            writeMessageToOnlineUser(currentUser, recievedUsername, cutMessage[1] + "&&all");
                        }
                    }
                } // message is for single conversation
                else if (forUser.contains(",") == false && forUser.contains("chat") == false) {
                    System.out.println("SINGLE user communication");
                    //check if client user is currently online
                    if (connectedClients.containsKey(forUser) == true) {
                        writeMessageToOnlineUser(forUser, recievedUsername, cutMessage[1]);
                    } else {
                        writeMessageToOfflineUser(forUser, recievedUsername, cutMessage[1]);
                        System.out.println("User not online, offline message has been sent");
                    }
                } // message is for multiple users
                else if (forUser.contains(",") == true) {
                    System.out.println("MULTI user communication");
                    // prepare the string
                    String[] multipleUsers = forUser.split(",");
                    //iterate the users of the multipleUsers string array
                    for (int i = 0; i <= multipleUsers.length; i++) {
                        //check if client user is currently online
                        if (connectedClients.containsKey(forUser) == true) {
                            writeMessageToOnlineUser(multipleUsers[i], recievedUsername, cutMessage[1]);
                        } // user is not online
                        else {
                            writeMessageToOfflineUser(multipleUsers[i], recievedUsername, cutMessage[1]);
                            System.out.println("User not online, offline message has been sent");
                        }
                    }
                } 
                else if (cutMessage[1].contains("/logoffuser")) {
                    connectedClients.remove(cutMessage[0]);
                    System.err.println("user hase been logged of and removed from the hashmap");
                            
                }
                
            }
        } catch (IOException e) {
            System.out.println("in or out failed");
            System.exit(-1);
        }
    }
}
