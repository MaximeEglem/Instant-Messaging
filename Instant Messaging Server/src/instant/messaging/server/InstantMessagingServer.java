/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package instant.messaging.server;

/**
 *
 * @author Ste
 */
public class InstantMessagingServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       ServerLogic ser = new ServerLogic();
       ser.listenSocket();
    }
}
