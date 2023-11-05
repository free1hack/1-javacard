import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 4000;
    private static final int PORTFILE = 4100;
    private static  int nbUser = 0;
    private static  int nbUserMax = 3;
    private static HashMap<String, ArrayList<Service>> chatGroups;
    private static HashMap<String, ArrayList<Service>> socketUser;
    private static List<String> userList; //list of connected users
    private static List<Service> allsockets;
    private static Map<String, String> userAuth;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        ServerSocket serverSocketFile = new ServerSocket(PORTFILE);
        chatGroups = new HashMap<>();
        socketUser = new HashMap<>();
        userList = new ArrayList<String>();
        allsockets = new ArrayList<Service>();
        userAuth = new HashMap<>();//Hash MAP containing login and passwords of all users.
        
        
        new Thread(new ServiceAdmin()).start();//For Admin
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new Service(socket)).start(); //For user
        }//End while  
        
        
        
        /* 
        while (true) {
            
            //nbUser = nbUser +1;
            //if (nbUser <= nbUserMax){
                Socket socket = serverSocket.accept();
                //Socket socketFile = serverSocketFile.accept();
                //new Thread(new Service(socket, socketFile)).start();  
                new Thread(new Service(nbUserMax)).start();//For Admin
                new Thread(new Service(socket, nbUserMax)).start(); //For user
                
             
            } else{
                //TODO : print messages of cose socket.
                break;
            }
             
        }
        */
    }

    

    
}