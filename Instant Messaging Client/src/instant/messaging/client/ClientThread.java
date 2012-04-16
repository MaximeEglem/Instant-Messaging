/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package instant.messaging.client;

import java.awt.Component;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
 *
 * @author Ste
 */
public class ClientThread extends Thread {

    DataInputStream is = null;
    DataOutputStream os = null;
    Socket clientSocket = null;
    ObjectInputStream ois = null;
    ArrayList<String> contacts = new ArrayList<String>(); //Contacts list
    private final JTextArea history;
    private final JTabbedPane panel;
    private final String username;

    public ClientThread(Socket clientSocket, JTextArea history, JTabbedPane panel, String username) {
        this.clientSocket = clientSocket;
        this.history = history;
        this.panel = panel;
        this.username = username;
    }
    //this method handles everything regarding sending and recieving of messages via the streams
    @Override
    public void run() {
        System.out.println("Client thread start...");
        String line;

        try {
            is = new DataInputStream(clientSocket.getInputStream());
            os = new DataOutputStream(clientSocket.getOutputStream());
            String recievedmessage;

            while (true) {

                //*******************List Contacts***********************//
                
           /*  
                
                try {
			contacts = (ArrayList<String>)ois.readObject();	//Ecoute de la liste des contacts
                        Iterator iter = contacts.iterator();
                        while (iter.hasNext()) {
                        System.out.println("Contact :  " + iter.next());
                        }
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
                
                */
                
                //*******************************************************//
                recievedmessage = is.readUTF();
                String[] cutMessage = recievedmessage.split(":");
                String fromUser = cutMessage[0];
                System.out.println("fromuser: " + fromUser);
                String message = cutMessage[1];
                System.out.println("message " + message);
                
                //checks if incoming message is for broadcasttab
                if (recievedmessage.length() > 0 && recievedmessage.endsWith("&&all")) {
                    System.out.println("INCOMING: " + recievedmessage);
                    line = history.getText();
                    line = line + "\n" + recievedmessage.replace("&&all", "");
                    history.setText(line);
                } 
                //check if tab for current message already exists
                else if (!fromUser.equals(username)) {
                    int getNewTab = 1;
                    System.out.println("nombre de tab " +panel.getTabCount());
                    for (int i = 0; i < panel.getTabCount(); i++)
                        if (panel.getTitleAt(i).equals(fromUser))
                            getNewTab = 0;
                        
                    if (getNewTab == 1){
                        System.out.println("new tab has to be created".toUpperCase());
                        //prepare the new tab
                        JTextArea newChatTab = new JTextArea();
                        newChatTab.setEditable(false);
                        //add the tab to the tab panel
                        panel.addTab(fromUser, newChatTab);
                    } else
                        System.out.println("tab for this conversation already exists");
                    
                     System.out.println("nombre de tab 2 " +panel.getTabCount());   
                    //iterate through the tabs to see what is already existing
                    for (int i = 0; i < panel.getTabCount(); i++) {
                        if (panel.getTitleAt(i).equals(fromUser)) {
                            
                            System.out.println("Test creation new tab:::: "+panel.getTitleAt(i));
                            //get tab for filling in message, tabCounter+1 creates an additional tab
                            JTextArea currentTabHistory = (JTextArea) panel.getComponentAt(i);
                            //fill chathistory
                            String oldHistory = currentTabHistory.getText();
                            String newHistory = oldHistory + "\n" + recievedmessage;
                            currentTabHistory.setText(newHistory);
                        }
                    }
                }
                


            }
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
