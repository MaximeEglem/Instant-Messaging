/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package instant.messaging.client;

import java.awt.Color;
import java.awt.Component;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
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
                    System.out.println("INCOMING BROADCAST: " + recievedmessage);
                    line = history.getText();
                    line = line + "\n" + recievedmessage.replace("&&all", "");
                    history.setText(line);
                } 
                else if (recievedmessage.length() > 0 && recievedmessage.contains("&&USERS::")) {
                    System.out.println("INCOMING &&USERS::: " + recievedmessage);
                    
                    cutMessage = recievedmessage.split("&&USERS::");
                    message = cutMessage[0];
                    String forUser = cutMessage[1];
                    forUser = forUser + "," + message.split(":")[0];
                    System.out.println("mesage : " + message);
                    System.out.println("forUser : " + forUser);
                    String[] usersChat = forUser.replaceAll(" ", "").split(",");
                    int tabNumber = 0;
                    boolean isGoodTab[] = new boolean[panel.getTabCount()];
                    
                    for (int i = 0; i < panel.getTabCount(); i++){
                        System.out.println("tab i : " + i);
                        for (int j = 0; j < usersChat.length; j++){
                            System.out.println("panel.getTabCount() : "+panel.getTabCount());
                            System.out.println("panel.getTitleAt(i).contains(usersChat[j]) : " + panel.getTitleAt(i).contains(usersChat[j]));
                            System.out.println("u sersChat[j].equals(this.username) : " + usersChat[j].equals(this.username));
                            System.out.println("this.username : (" + this.username + ")");
                            System.out.println("usersChat["+j+"] : (" + usersChat[j] + ")");
                            
                            if (panel.getTitleAt(i).contains(usersChat[j]) || usersChat[j].equals(this.username)){
                                isGoodTab[i] = true;
                                System.out.println("isGoodTab : true");
                             }
                            else{
                                isGoodTab[i] = false;
                                System.out.println("isGoodTab : false");
                            }
                        }
                        if(isGoodTab[i]){
                            System.out.println("tab final : "+ i);
                            JTextArea currentTabHistory = (JTextArea) panel.getComponentAt(i);
                            //fill chathistory
                            String oldHistory = currentTabHistory.getText();
                            String newHistory = oldHistory + "\n" + message;
                            currentTabHistory.setText(newHistory);
                        }
                    }
                    recievedmessage = "";
                            
                }
                else if (recievedmessage.length() > 0 && recievedmessage.endsWith("&&REQUEST")) {
                    JOptionPane jop = new JOptionPane();			
                    int option = jop.showConfirmDialog(null, fromUser +" asking you to add him and " + message.replaceAll("&&REQUEST", "") + " in a new chat group" , "Group chat" , JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
      
                    if(option == JOptionPane.OK_OPTION){
                        recievedmessage = "";
                        message = "[REQUEST-ANSWERED]" + "[" + cutMessage[0] + "," + cutMessage[1].replaceAll("&&REQUEST", "") + "]" + "[TRUE]";
                        os.writeUTF(message);
                        os.flush();
                        System.out.println("OUTGOING: " + message);
                        JTextArea newChatTab = new JTextArea();
                        newChatTab.setEditable(false);
                        //add the tab to the tab panel
                        panel.addTab(cutMessage[0] + "," + cutMessage[1].substring(1).replaceAll("&&REQUEST", ""), newChatTab);
                    }
                    else{
                        recievedmessage = "You refuse to join them";
                        message = "[REQUEST-ANSWERED]" + "[" + cutMessage[0] + "," + cutMessage[1] + "]" + "[FALSE]";
                        os.writeUTF(message);
                        os.flush();
                        System.out.println("OUTGOING: " + message);
                    }
                }
                else if (recievedmessage.length() > 0 && recievedmessage.endsWith("&&ANSWERED")) {
                    System.out.println("INCOMING ANSWERED: " + recievedmessage);
                    line = history.getText();
                    boolean isGoodTab[] = new boolean[panel.getTabCount()];
                    boolean tableUpadted = false;
                    if(recievedmessage.contains("TRUE")){
                        message = message.replaceAll("TRUE&&ANSWERED", "");
                        System.out.println("list des users a ajouter : " + message);
                        String[] usersChat = message.replaceAll(" ", "").split(",");
                        for (int i = 0; i < panel.getTabCount(); i++){
                            for (int j = 0; j < usersChat.length; j++){
                                if (!panel.getTitleAt(i).contains(usersChat[j])){
                                    isGoodTab[i] = false;
                                }
                                else
                                isGoodTab[i] = true;
                            }
                            if(isGoodTab[i]){
                            panel.setTitleAt(i, panel.getTitleAt(i) + "," + fromUser);
                            tableUpadted = true;
                         }
                        }
                        
                        if(!tableUpadted){
                            JTextArea newChatTab = new JTextArea();
                            newChatTab.setEditable(false);
                            //add the tab to the tab panel
                            String usersNewTab =  fromUser;
                            for (int i = 0; i < usersChat.length; i++)
                                if (!usersChat[i].equals(this.username))
                                    usersNewTab = usersNewTab + "," + usersChat[i];
                            panel.addTab(usersNewTab, newChatTab);
                        }
                        line = line + "\n" + fromUser + " join your group.";
                            
                    }else
                        line = line + "\n" + fromUser + " refused your invitation to join your group.";
                    
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
                            //if(panel.getTitleAt(i).)
                            //panel.setBackgroundAt(i, Color.BLUE);
                            
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
