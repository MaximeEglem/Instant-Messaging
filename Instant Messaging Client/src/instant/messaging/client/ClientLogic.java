package instant.messaging.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientLogic {

    DataInputStream in;
    DataOutputStream out;
    Socket socket;

    //creates the socket connection and prepare out-/ and inputstream
    //returns false if connection can not be established
    public boolean listenSocket(String username, String password) {
        try {
            socket = new Socket("localhost", 4444);
            System.out.println("Connected to server localhost on port 4444");
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("DataOutputStream initialised");
            in = new DataInputStream(socket.getInputStream());
            System.out.println("BufferedReader initialised");
            out.writeUTF(username + ":" + password);
            System.out.println("login data sent "+username + ":" + password);
            out.flush();
            
            return true;
        } catch (UnknownHostException e) {
            System.out.println("Unknown host");
            System.exit(1);
            return false;
        } catch (IOException e) {
            System.out.println("No I/O");
            System.exit(1);
            return false;
        }
    }

    public static String SHA(String password) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        System.out.println(md.digest());
        md.update(password.getBytes());

        byte byteData[] = md.digest();

        System.out.println(md.digest());
        //convert the byte to hex format method 1
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();

    }
    
    public void refreshUserList() {
        try {
            System.out.println("userlist refresh request");
            out.writeUTF("%refreshUserList%");
            out.flush();
            System.out.println("command for refresh sent");
        } catch (IOException ex) {
            Logger.getLogger(ClientLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}