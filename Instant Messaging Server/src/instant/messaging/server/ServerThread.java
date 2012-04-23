package instant.messaging.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;

import java.io.File;
import java.util.*;



class ServerThread implements Runnable {

    private Socket client;
    HashMap<String, ServerThread> connectedClients;
    public String recievedUsername;
    public String recievedPassword;
    
    private String action;
    private String[] users;
    private String stringUsers;
    private String message;
    
    DataInputStream in;
    DataOutputStream out;
    ObjectOutput oos;
    ServerLogic ser;
    String finishedlist = "";
    Boolean errorUserConnection = true;
    ArrayList<String> clients; //Contacts list

    //Constructor
    ServerThread(Socket client) {
        this.client = client;
    }
    
    public String getAction(){
        return this.action;
    }
    
    public String getUsers(int value){
        return this.users[value];
    }
    
    public String getMessage(){
        return this.message;
    }
    
    public String getStringUsers(){
        return this.stringUsers;
    }
    
    public void setAction(String value){
        this.action = value;
    }
    
    public void setUsers(String values){
        values = values.replaceAll(" ", "");
        setStringUsers(values);
        String[] usersName = values.split(",");
        this.users = usersName;
    }
    
    public void setMessage(String value){
        this.message = value;
    }
    
    public void setStringUsers(String value){
        this.stringUsers = value;
    }
    
    public void sendClients(){	//Sending contact list
		
        try {
                    Iterator iter = clients.iterator();
                        while (iter.hasNext()) {
                        System.out.println("Contact server:  " + iter.next());
                        }
                       
            oos = new ObjectOutputStream(client.getOutputStream());
            oos.writeObject(clients);
            oos.flush();
               
		} catch (IOException e) {
                    System.err.println("Error : Cannot send the contacts list :" +e.getMessage());
		}
	}
    
    public Boolean isErrorUserConnection(){
        return errorUserConnection;
    }
    
    public void setErrorUserConnection(Boolean value){
         this.errorUserConnection = value;
    }

    public void registre(){  //L'enregistement du client dans la liste des contacts
		//Registring client
		clients.add(client.getRemoteSocketAddress().toString());
		
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
       
        clients = new ArrayList<String>();
       System.out.println("setRecentHashMapForUsers");
       //this.clients.clear();
       Iterator iter = connectedClients.entrySet().iterator();
 
        while (iter.hasNext()) {
                Map.Entry mEntry = (Map.Entry) iter.next();
                this.clients.add(mEntry.getKey().toString());
                System.out.println(mEntry.getKey());
        }
        String onlineClients = clients.toString();
        //get all offline users from userlist file
        ArrayList offlineClients = new ArrayList<String>();
        //offlineClients =ser.getOfflineUsers(recievedUsername);
        //offlineClients.toString();
        
       
            //out.writeUTF("%newList%:"+onlineClients+""+offlineClients);
            //out.flush();
            System.out.println("%newList%:"+onlineClients+""+offlineClients);
        
        
        
        
        //sendClients();
       /*
       
       String completeList = connectedClients.toString();
       completeList.replace("{", "");
       completeList.replace("}", "");
       this.finishedlist = "";
       String[] singleUser = completeList.split(",");
       for (int i=0; i <= singleUser.length;i++) {
           String user = singleUser.toString();
            this.finishedlist = this.finishedlist + user.substring(0, user.indexOf("="));
            
       }

       System.out.println("this is the finished list: "+this.finishedlist);
       //sendClients();
        
        
        */
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

            //check if there is a offline message for the logged in user
            CheckChatHistory(recievedUsername);
            
            System.out.println("User: " + recievedUsername + " logged in with password: " + recievedPassword);

            while (true) {
                //prepare incoming message for sending to corresponding client
                String fullMessage = in.readUTF().toString();
                System.out.println("INCOMING: " + fullMessage);
                             
                dataProcessing(fullMessage);           
                
            }
        } catch (IOException e) {
            System.out.println("in or out failed");
            //System.exit(-1);
        }
        
    }
    
    
    
    private void dataProcessing(String fullMessage){
        
        splitMessageInformaton(fullMessage);
        findAction();
        
    }
    
    private void splitMessageInformaton(String actionMessage){
         
        String[] information;
        information = actionMessage.split("]");
        
        
        for (int i = 0; i < information.length; i++){
          information[i] = information[i].substring(1);
         
            switch(i){
                case 0 : this.setAction(information[0]);
                break;
                
                case 1 : this.setUsers(information[1]);
                break;
             
                case 2 : 
                    information = actionMessage.split(information[1]); // Even if a caractere [" appear the message will be corectly display
                    this.setMessage(information[1].substring(2,information[1].length()-1)); // delete ][ at the begining anf ] and the end one the string
                break;
            }
    }
      
    }
    
    private void findAction(){
     
        if (this.action.equals("LOG-USER-OFF")){
            connectedClients.remove(this.users);
            System.out.println("User " + this.getUsers(0) + " has been logged of and removed from the hashmap");
        }
        else if (this.action.equals("BROADCAST")){
            System.out.println("message for everyone");
            //iterate the whole hashmap to send a message to all online users
            for (String currentUser : connectedClients.keySet()) {
                if (!currentUser.equals(recievedUsername)) {
                    writeMessageToOnlineUser(currentUser, recievedUsername, this.getMessage() + "&&all");
                }
            }
        }
        else if (this.action.equals("USER")){ // message is for single conversation
            System.out.println("SINGLE user communication : " + this.getUsers(0));
            //check if client user is currently online
            if (connectedClients.containsKey(this.getUsers(0)) == true) {
                writeMessageToOnlineUser(this.getUsers(0), recievedUsername, this.getMessage());
            } else {
                writeMessageToOfflineUser(this.getUsers(0), recievedUsername, this.getMessage());
                System.out.println("User not online, offline message has been sent");
            }
        }
        else if (this.action.equals("USERS")){ // message is for multiple users
            System.out.println("MULTI user communication");
            //iterate the users of the multipleUsers string array
            System.out.println("lenght : " + this.users.length);
            for (int i = 0; i < this.users.length; i++) {
                System.out.println("users name  : (" + this.getUsers(i)+")");
                //check if client user is currently online
                if (connectedClients.containsKey(this.getUsers(i)) == true) {
                    writeMessageToOnlineUser(this.getUsers(i), recievedUsername, this.getMessage() + "&&USERS::" + this.getStringUsers());
                }
            }
        }
        else if (this.action.equals("REQUEST")){ // Asking permittion to add him into a group
            if (connectedClients.containsKey(this.getUsers(0)) == true) {
                writeMessageToOnlineUser(this.getUsers(0), recievedUsername, this.getMessage() + "&&REQUEST");
            }
        }
        else if(this.action.equals("REQUEST-ANSWERED")){
            for (int i = 0; i < this.users.length; i++) {
                if (connectedClients.containsKey(this.getUsers(i)) == true) {
                    writeMessageToOnlineUser(this.getUsers(i), recievedUsername, this.getStringUsers() + this.getMessage() + "&&ANSWERED");
                }
            }
        }
        
    }  
                
    
}
