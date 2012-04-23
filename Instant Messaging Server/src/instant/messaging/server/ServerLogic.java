package instant.messaging.server;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerLogic {

    private ServerSocket server = null;
    private ServerThread connection;
    public HashMap<String, ServerThread> connectedClients = new HashMap<String, ServerThread>();
    private String userFilePath = "resources/UserList.txt";
    private File file = new File(userFilePath);
    //private String userFilePath = "C:/Users/Mel/Desktop/Development/NetBeansProjects/Instant Messaging Server/resources/UserList.txt";

    //returns the thread object from a specific user
    //public ServerThread getThreadFromUser(String user){
    //     ServerThread th = connectedClients.get(user);
    //   return th;
    //}
    public boolean listenSocket() {

        try {
            server = new ServerSocket(4444);
            System.out.println("server running on port 4444");
        } catch (IOException e) {
            System.out.println("Could not listen on port 4444");
            System.exit(-1);
        }
        //listen for new connections
        while (true) {
            try {
                System.out.println("in while serverlogic");
                //server.accept returns a client connection
                connection = new ServerThread(server.accept());
                System.out.println("new server object created");
                connection.getCredentials();

                //check username and password
                if (isGoodLogin(connection.recievedUsername, connection.recievedPassword, connectedClients) == true) {


                    //create and start new Thread
                    Thread t = new Thread(connection);
                    t.start();

                    //put the username + threadobject in hashmap
                    connectedClients.put(connection.recievedUsername, connection);
                    connection.setRecentHashMap(connectedClients);
                    System.out.println(connectedClients.get(connection.recievedUsername).toString());
                    System.out.println(connectedClients.size());

                    //method to set the recent hashmap of online users for the clients
                    connection.setRecentHashMapForUsers(connectedClients);

                } //new users handling
                else if (connection.recievedUsername.startsWith("new") == true) {
                        String message;
                        
                    if (creationAccount(connection.recievedUsername, connection.recievedPassword) == true)
                        message = "[Success]Account has been created!";
                    else
                        message = "[Error]Account could not be created!";
                    
                    
                        System.out.println(message);
                        connection.out.writeUTF(message);
                        connection.out.flush();
                        
                } else {
                    System.err.println("Bad username or Password...or user already logged in");
                }

            } catch (IOException e) {
                System.out.println("Accept failed: 4444");
                System.exit(-1);
            }
        }
    }
    //Check if user already exists in database and if the user put the right password

    private boolean isGoodLogin(String login, String password, HashMap currentlyConnectedUsers) {
        if (currentlyConnectedUsers.get(login) == null) {
            File file = new File(userFilePath);
            if (file.exists()) {
                try {

                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(
                            new FileInputStream(file)));
                    String ligne;
                    while ((ligne = br.readLine()) != null) {
                        String[] resultSplit = ligne.split(":");
                        if (login.equals(resultSplit[0]) && password.equals(resultSplit[1])) {
                            return true;
                        }
                    }
                    br.close();
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

            }
            return false;
        } else {
            System.out.println("User with same name already logged in");
            return false;
        }
    }
    
    private boolean isExistingLogin(String login){
        
        if (this.file.exists()) {
                try {

                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(
                            new FileInputStream(this.file)));
                    String ligne;
                    while ((ligne = br.readLine()) != null) {
                        String[] resultSplit = ligne.split(":");
                        if (login.equals(resultSplit[0])){
                            br.close();
                            return true;
                        }
                    }
                    br.close();
                    return false;
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

            }
        
        return false;
    }


    //creates an account for new users
    private boolean creationAccount(String login, String password) {
        //cut off the new prefix 'new'
        
        System.out.println("login : " +login);
        System.out.println("login.substring(3) : " +login.substring(3));
        login = login.substring(3);

        try {

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                    new FileInputStream(this.file)));
            String ligne;
            while ((ligne = br.readLine()) != null) {
                String[] resultSplit = ligne.split(":");
                if (login.equals(resultSplit[0])) {
                    br.close();
                    return false;
                }
            }
            br.close();

            String concatenateLigne = login + ":" + password;
            System.out.println(login + ":" + password);
            FileWriter bw = new FileWriter(file, true);
            bw.append(concatenateLigne + "\n").flush();
            bw.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return true;
    }
    //shows all offline users in an arraylist
    public ArrayList<String> getOfflineUsers(String userItself) {
        System.out.println("getOfflineUsers");
        ArrayList<String> offlineUsers = new ArrayList();
        File file = new File(userFilePath);
        if (file.exists()) {
            System.out.println("file exists");
            try {

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                        new FileInputStream(file)));
                String ligne;
                while ((ligne = br.readLine()) != null) {
                    String[] resultSplit = ligne.split(":");
                    System.out.println("current user: "+resultSplit[0]);
                    if (!resultSplit[0].equals(userItself)) {
                        offlineUsers.add(resultSplit[0]);
                    }
                    return offlineUsers;
                }
                br.close();
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        offlineUsers.add("There are currently no Users");
        return offlineUsers;
    }
}