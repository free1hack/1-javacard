import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.validation.SchemaFactoryConfigurationError;

import java.nio.channels.ShutdownChannelGroupException;
import java.nio.charset.StandardCharsets;


public class Service implements Runnable {
    private Socket socket, socketFile;
  
    private final  int nbUserMax = 3;
    private BufferedReader ins, inNew;
    private final Scanner in;
    private PrintWriter out;
    private DataInputStream  fileDataIntStream; //For file recieving
    private static  DataOutputStream dataOutStream;//For sending a file
    private String name;
    private String group;
    private String password;
    private String answer;
    private static HashMap<String, ArrayList<Service>> chatGroups = new HashMap<>();
    private static HashMap<String, ArrayList<Service>> socketUser = new HashMap<>(); //String = user_name, ArrayList<Service>= list of socket ID of the same user
    protected static List<String> userList = new ArrayList<String>(); //list of connected users
    private static List<Service> allsockets = new ArrayList<Service>(); // comme ArrayList<Service>
    protected static Map<String, String> userAuth = new HashMap<>();//Hash MAP containing login and passwords of all users.
    private static Map<String, String> userListStatus = new HashMap<>();
    private boolean  isRecieverConnected = false;
    
    private Client client;
 
    
    //For User
    public Service(Socket socket) throws IOException {
        this.socket = socket;
        //this.nbUserMax = nbUserMax;
        this.in = new Scanner(socket.getInputStream());
        this.client = new Client(new PrintWriter(socket.getOutputStream(), true), socket);
        //this.out = new Client(new PrintWriter(socket.getOutputStream(), true), socket);
        //mainLoop();
    }

    /* 
    //For Admin
    public Service() throws IOException {
        //this.nbUserMax = nbUserMax;
        this.in = new Scanner(System.in);
        this.client = new Client(new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true));
        //adminMainLoop();
    }
    */
     /* 
    public Service(Socket socket, Socket socketFile) {
        this.socket = socket;
        this.socketFile = socketFile;
    }
    */
   

    @Override
    public void run() {
        try {
            //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //out = new PrintWriter(socket.getOutputStream(), true);
            
            //fileDataIntStream = new DataInputStream(socketFile.getInputStream());
            //dataOutStream = new DataOutputStream(socketFile.getOutputStream());


            fileDataIntStream = new DataInputStream(socket.getInputStream());
            dataOutStream = new DataOutputStream(socket.getOutputStream());
            

            userAuth = loadExistingUsers();
           
            //userLogin(in, userList,userAuth );
            userLogin();
            
            //mainLoop(this.in, out,allsockets);
            mainLoop();
            adminMainLoop();


            
        } catch (IOException e) {
            System.out.println(e);
        } /*finally {
            try {
                this.socket.close();
            } catch (IOException e) {}
            
        }*/
    }//End run

   
    
    

    private void userLogin() throws IOException {
        while(true){ 
               
            //out.println("<Server MSG> User authentication");
            //out.println("Please enter your login :");
            sendMessage("<Server MSG> User authentication");
            sendMessage("Please enter your login :");
            //name = in.readLine();
            name = this.in.nextLine().trim();

            //Check if the name = admin. It's not allowed.
            if (name.toLowerCase().equals("admin")){
                sendMessage("[ERROR] name is not allowed. Press Ctl+C to leave.");
                System.out.println ("[FATAL] name <admin> is being used");
                continue;//To go to the begining to the while loop.
            }//End  if (!name.toLowerCase().equals("admin"))
            
            //out.println("Please enter your password :");
            sendMessage("Please enter your password :");
            password = this.in.nextLine().trim();

            if (!isMaxNumberOfConnectedUsersReached()){ 
                 

                    //provisioned user authentication
                    if (userAuthCheck(name, password, userAuth) && (!name.isEmpty()) && (!password.isEmpty())){
                        if (isUserconnected(name)){
                            this.sendMessage("You're already connected !");
                            System.out.println("User :" +name+" is already connected !");
                            
                            //this.socket.close();
                            break;
                        } else{ 
                        sendMessage("Authentication successful !");
                        userList.add(name);
                        System.out.println("Authentication successful");
                        //sendMessage("<Server MSG> Authentication finished !");
                        break;
                        }
                    }
                    //Create account for unknown user. 
                    else if (!isUserExists (name, userAuth)  && (!name.isEmpty()) && (!password.isEmpty())){
                        sendMessage("your account is created !");
                        System.out.println("Account created for " + name);
                        addUser(name, password, userAuth);
                        userList.add(name);
                        //sendMessage("<Server MSG> Authentication finished !");
                        break;
                    }else if(isUserExists (name, userAuth)  && (!name.isEmpty()) && (!password.isEmpty())){
                        sendMessage("Wrong password. Please try again");
                        System.out.println("User " + name + " entered wrong password");
                    } else if ((name.isEmpty()) || (password.isEmpty())){
                        sendMessage("You didn't enter your login or pwd");
                        System.out.println("User " + name + " didn't enter login or pwd");
                    } else{
                        sendMessage("something went wrong. Please contact your administrator");
                        System.out.println("User " + name + " faced an unexpected problem while connecting");
                    }
                
            }//end  if (isMaxNumberOfConnectedUsersReached())  
        }//end while 
        ArrayList<Service> socketId = socketUser.get(name);
            
            if (socketId == null) {
                socketId = new ArrayList<>();
                socketUser.put(name, socketId);
            }
            
            socketId.add(this);
            
            allsockets.add(this);

            sendMsgToAll ("<Server MSG>"+name +" is now connected");  
    }//End funciton userLogin

    private synchronized boolean isMaxNumberOfConnectedUsersReached() throws IOException{

        if (Service.userList.size() >= nbUserMax){
            sendMessage("<Server MSG>The maximum number of connected users is reached ! Please try later");
            System.out.println("The maximum number of connected users is reached ! Please try later");
            closeClient();
            return true;

        } 


        return false;
    } 

    private synchronized void closeClient() throws IOException {
        this.client.getOut().close();
        this.socket.close();
    }

    private  boolean isUserExists(String name2, Map<String, String> userAuth2) {

        boolean found = false;
        String usr;
        for(Map.Entry<String, String> index : userAuth.entrySet()){
            usr = index.getKey();
            
            if(name.equals(usr)){
                found = true;
            }
            if (found){
                break;
            } 
        }
       

        return found;
    }

    private Map<String, String> loadExistingUsers() {
        Map<String, String> mapList = new  HashMap<>();
        
        try{ 
           BufferedReader bffReader = new BufferedReader(new FileReader("userAuthInfo.txt"));
           String line;
           while ((line = bffReader.readLine()) != null) {
            String[] attributes = line.split(",");
            mapList.put(attributes[0], attributes[1]);
          }
          bffReader.close();
            
        } catch (IOException e) {
       
        }
        
        
        return mapList;            
    } 

    private boolean userAuthCheck(String name2, String password2, Map<String, String> mapList) {
         boolean authOK = false;
         String login;
         String passwd;
         System.out.println("Authenticating : " + name2);
         for(Map.Entry<String, String> index : mapList.entrySet()){
            login = index.getKey();
            passwd = index.getValue();
            if (login.equals(name2)  && passwd.equals(password2)){
                authOK = true;
                System.out.println(login);
                System.out.println(passwd);

        
            } 
         }//End for 
         return authOK;


    }

    private void addUser(String login, String pwd, Map<String, String> mapList2) {
        mapList2.put(login, pwd);

        try {
            
            FileWriter writeIntoFile = new FileWriter("userAuthInfo.txt");
            for(Map.Entry<String, String> index : mapList2.entrySet()){
                writeIntoFile.write(index.getKey() + "," + index.getValue() + System.lineSeparator());
            
            } 
           
            writeIntoFile.close();

        } catch (IOException e) {
            
            e.printStackTrace();
        }


    }

    public Map<String, String> loadUserListStatus(List<String> users ,Map<String, String> hashmap){
        Map<String, String> map = new HashMap<>();
        String user;
        
        boolean userFound = false;
        for(Map.Entry<String, String> index : hashmap.entrySet()){
            user = index.getKey();
            userFound = false;
            for (String str: users) {
                if(user.equals(str)){
                    userFound = true;
                    map.put(user, " Connected");           
                }                                
            }//end For
            if(!userFound){
                map.put(user, " NOT connected");
            } 
           
         }//End for 
        

        return map;
    } 

    public void sendUsersStatusList(Map<String, String> hashmap){
        String user;
        String status;
        for(Map.Entry<String, String> index : hashmap.entrySet()){
            user = index.getKey();
            status = index.getValue();
            sendMessage(user +" -> "+ status);
                       
         }//End for 

    } 


    public void sendMessage(String message) {
        //out.println(message);
        this.client.getOut().println(message);
    }
    private void printMenu() {
        /*
        out.println("<Server MSG> printing the menu");
        out.println("Please select one of the following options:");
        out.println("0) quit");
        out.println("1) msgTo");
        out.println("2) msgAll");
        out.println("3) list");
        out.println("4) chatGrp");
        out.println("5) sendFile");
        out.println("6) help");
        out.println("<Server MSG> end printing the menu");
        */
        sendMessage("<Server MSG> printing the menu");
        sendMessage("Please select one of the following options:");
        sendMessage("0) quit");
        sendMessage("1) msgTo");
        sendMessage("2) msgAll");
        sendMessage("3) list");
        sendMessage("4) chatGrp");
        sendMessage("5) sendFile");
        sendMessage("6) help");
        sendMessage("<Server MSG> end printing the menu");

    }
    private void printAdminMenu() {
        
        sendMessage("<Server MSG> printing the menu for ADMIN");
        sendMessage("Please select one of the following options:");
        sendMessage("0) quit");
        sendMessage("1) msgTo");
        sendMessage("2) msgAll");
        sendMessage("3) list");
        sendMessage("4) chatGrp");
        sendMessage("5) sendFile");
        sendMessage("6) help");
        sendMessage("<Server MSG> end printing the menu");

    }

    

    
    private void  printHelp(){
        /* 
        out.println("<Server MSG>printing the help menu !");
        out.println("Welcome to the help menu !");
        out.println("0) quit    : This is for quiting the chat");
        out.println("1) msgTo   : This level allows you to chat with a specific user in the chat");
        out.println("2) msgAll  : This level allows you to chat with all connected users");
        out.println("3) list    : This level allows you to list all connected users");
        out.println("4) chatGrp : connect to chat group");
        out.println("5) sendFile: This lvel to sed a file to a user");
        out.println("6) help    : This lvel displays the help menu");
        out.println("<Server MSG>printing the help menu ends !");
        */
        sendMessage("<Server MSG>printing the help menu !");
        sendMessage("Welcome to the help menu !");
        sendMessage("0) quit    : This is for quiting the chat");
        sendMessage("1) msgTo   : This level allows you to chat with a specific user in the chat");
        sendMessage("2) msgAll  : This level allows you to chat with all connected users");
        sendMessage("3) list    : This level allows you to list all connected users");
        sendMessage("4) chatGrp : connect to chat group");
        sendMessage("5) sendFile: This lvel to sed a file to a user");
        sendMessage("6) help    : This lvel displays the help menu");
        sendMessage("<Server MSG>printing the help menu ends !");

    } 

    //mainloop for Admin
    private void adminMainLoop() throws IOException {
        while (true) {            
            printAdminMenu();
            String input = this.in.nextLine().trim();
             
           
            //To re-display the menu when clicking "enter" without typing anything.
            if (input == null){
                return;
            } 
            
            if(input.equals("quit") ||  input.equals("0")){
                /* 
                out.println("You're going to be disconnected.....");
                out.println("Diconnected !");
                */
                sendMessage("You're going to be disconnected.....");
                sendMessage("Diconnected !");
               
                removeUserFromUserList(getUserName (socketUser), userList );      
                break;    
            } 
            if(input.equals("msgTo") ||  input.equals("1")){
                System.out.println("user choose : msgTo option");
                //out.println("<Server MSG>msgTo option");
                //out.println("Please enter the name of the user that you would like to send a message to");
                sendMessage("<Server MSG>msgTo option");
                sendMessage("Please enter the name of the user that you would like to send a message to");
                input = this.in.nextLine().trim();
                System.out.println(input);
                //out.println("You have selected to send essage to user <" +input+"> only" );
                //out.println("Please enter <<quit>> to leave this level" );
                //out.println("you can start chating now" );
                sendMessage("You have selected to send essage to user <" +input+"> only" );
                sendMessage("Please enter <<quit>> to leave this level" );
                sendMessage("you can start chating now" );
                //TODO: ask the user2 to accept or not the conversation from user1
                //TODO: add <userX:> infront of the user messages. 
                ArrayList<Service> socketId2 = socketUser.get(input);
                while(true){ 
                    input = this.in.nextLine().trim();
                    if (input.equals("quit"))
                   {
                        //TODO: send that a user has left the conversation to the other user
                        //out.println("you left msgTo option" );
                        sendMessage("you left msgTo option" );
                        break;

                   }else{  
                        for (Service client : socketId2) {
                            client.sendMessage(name + ": " + input);
                        } 
                    }//end Else    
                }          

            } 
            if(input.equals("msgAll") ||  input.equals("2")){
                System.out.println("user choose : msgAll option");
                //out.println("<Server MSG>msgAll option");
                //out.println("You have select msgAll option. You can start sending messages now to all participants - enter quit to leave this mode :");
                //out.println("you can start chating now" );
                sendMessage("<Server MSG>msgAll option");
                sendMessage("You have select msgAll option. You can start sending messages now to all participants - enter quit to leave this mode :");
                sendMessage("you can start chating now" );

                while(true){ 
                    
                    input = this.in.nextLine().trim();
                    if (input.equals("quit")){   

                        //TODO: send that a user has left the conversation to the other user
                        //out.println("you left msgAll option" );
                        sendMessage("you left msgAll option" );    
                        break;
                    
                    }else{
                        for (Service client : allsockets) {
                            client.sendMessage(name + ": " + input);
                        }     
                    }//end else 
                        
                }//End While

            }
            if(input.equals("list") ||  input.equals("3")){
                //out.println("<Server MSG>listing users");
                //out.println("Printing the list of all users to this chat with their status....");
                sendMessage("<Server MSG>listing users");
                sendMessage("Printing the list of all users to this chat with their status....");
                
                /* 
                for (String str: userList) {
                    out.println(str + " (connected)");                        
                }
                */
                userListStatus = loadUserListStatus(userList ,userAuth);
                sendUsersStatusList(userListStatus);
                //out.println("Printing ends !");
                //out.println("<Server MSG>listing users ends");
                sendMessage("Printing ends !");
                sendMessage("<Server MSG>listing users ends");

            }
            if(input.equals("chatGrp") ||  input.equals("4")){
                //out.println("<Server MSG>Chat group option");
                sendMessage("<Server MSG>Chat group option");
                chatGroupHandler(chatGroups);

            }     
            if(input.equals("sendFile") ||  input.equals("5")){
                //out.println("<Server MSG>Send file option");
                sendMessage("<Server MSG>Send file option");
                userFileHandler();
                //out.println("<Server MSG> toujours là ?");
                System.out.println("just after sender handler");
                //printMenu();
                //break;

            }

            if(input.equals("help") ||  input.equals("6")){
                printHelp();
                //break;

            }
            
            
            
        }//End while (true)
    }//END adminMainLoop()

    //Main loop for users
    //private void mainLoop(Scanner in,PrintWriter out, List<Service> allsockets) throws IOException{
    private void mainLoop() throws IOException{

        while (true) {            
            printMenu();
            String input = this.in.nextLine().trim();
             
           
            //To re-display the menu when clicking "enter" without typing anything.
            if (input == null){
                return;
            } 
            
            if(input.equals("quit") ||  input.equals("0")){
                /* 
                out.println("You're going to be disconnected.....");
                out.println("Diconnected !");
                */
                sendMessage("You're going to be disconnected.....");
                sendMessage("Diconnected !");
               
                removeUserFromUserList(getUserName (socketUser), userList );      
                break;    
            } 
            if(input.equals("msgTo") ||  input.equals("1")){
                System.out.println("user choose : msgTo option");
                //out.println("<Server MSG>msgTo option");
                //out.println("Please enter the name of the user that you would like to send a message to");
                sendMessage("<Server MSG>msgTo option");
                sendMessage("Please enter the name of the user that you would like to send a message to");
                input = this.in.nextLine().trim();
                System.out.println(input);
                //out.println("You have selected to send essage to user <" +input+"> only" );
                //out.println("Please enter <<quit>> to leave this level" );
                //out.println("you can start chating now" );
                sendMessage("You have selected to send essage to user <" +input+"> only" );
                sendMessage("Please enter <<quit>> to leave this level" );
                sendMessage("you can start chating now" );
                //TODO: ask the user2 to accept or not the conversation from user1
                //TODO: add <userX:> infront of the user messages. 
                ArrayList<Service> socketId2 = socketUser.get(input);
                while(true){ 
                    input = this.in.nextLine().trim();
                    if (input.equals("quit"))
                   {
                        //TODO: send that a user has left the conversation to the other user
                        //out.println("you left msgTo option" );
                        sendMessage("you left msgTo option" );
                        break;

                   }else{  
                        for (Service client : socketId2) {
                            client.sendMessage(name + ": " + input);
                        } 
                    }//end Else    
                }          

            } 
            if(input.equals("msgAll") ||  input.equals("2")){
                System.out.println("user choose : msgAll option");
                //out.println("<Server MSG>msgAll option");
                //out.println("You have select msgAll option. You can start sending messages now to all participants - enter quit to leave this mode :");
                //out.println("you can start chating now" );
                sendMessage("<Server MSG>msgAll option");
                sendMessage("You have select msgAll option. You can start sending messages now to all participants - enter quit to leave this mode :");
                sendMessage("you can start chating now" );

                while(true){ 
                    
                    input = this.in.nextLine().trim();
                    if (input.equals("quit")){   

                        //TODO: send that a user has left the conversation to the other user
                        //out.println("you left msgAll option" );
                        sendMessage("you left msgAll option" );    
                        break;
                    
                    }else{
                        for (Service client : allsockets) {
                            client.sendMessage(name + ": " + input);
                        }     
                    }//end else 
                        
                }//End While

            }
            if(input.equals("list") ||  input.equals("3")){
                //out.println("<Server MSG>listing users");
                //out.println("Printing the list of all users to this chat with their status....");
                sendMessage("<Server MSG>listing users");
                sendMessage("Printing the list of all users to this chat with their status....");
                
                /* 
                for (String str: userList) {
                    out.println(str + " (connected)");                        
                }
                */
                userListStatus = loadUserListStatus(userList ,userAuth);
                sendUsersStatusList(userListStatus);
                //out.println("Printing ends !");
                //out.println("<Server MSG>listing users ends");
                sendMessage("Printing ends !");
                sendMessage("<Server MSG>listing users ends");

            }
            if(input.equals("chatGrp") ||  input.equals("4")){
                //out.println("<Server MSG>Chat group option");
                sendMessage("<Server MSG>Chat group option");
                chatGroupHandler(chatGroups);

            }     
            if(input.equals("sendFile") ||  input.equals("5")){
                //out.println("<Server MSG>Send file option");
                sendMessage("<Server MSG>Send file option");
                userFileHandler();
                //out.println("<Server MSG> toujours là ?");
                System.out.println("just after sender handler");
                //printMenu();
                //break;

            }

            if(input.equals("help") ||  input.equals("6")){
                printHelp();
                //break;

            }
            
            
            
        }//End while (true)

    }//END Function mainLoop

   
    private synchronized void sendMsgToAll(String msg){
        
        for (Service client : allsockets) {
            client.sendMessage(msg);
        }

    } 
    

    private synchronized void userFileHandler() throws IOException {
        String recieverName, fileName, newfileName, fileNameToSend, endSendingFile;
        String senderUserName = getUserName(socketUser);
        long fileSize;
        
        //This while is to re-invite the user if it's entered a wrong user name or filename
        while (true){ 
            System.out.println("user "+senderUserName+" has choosen : send file option");
            //out.println("<Server MSG>sendFile option");
            //out.println("Please enter the name of the user that you would like to send a file to");
            sendMessage("<Server MSG>sendFile option");
            sendMessage("Please enter the name of the user that you would like to send a file to");
            recieverName = this.in.nextLine().trim();
            System.out.println(senderUserName +" would like to send file to user "+recieverName);
            //out.println("You have selected to send file to user <" +recieverName+"> only" );
            //out.println("Please enter <<quit>> to leave this level" );
            //out.println("Please enter the file name" );
            sendMessage("You have selected to send file to user <" +recieverName+"> only" );
            sendMessage("Please enter <<quit>> to leave this level" );
            sendMessage("Please enter the file name" );
            fileName = this.in.nextLine().trim();
            //out.println("Please send the file Size:" );
            sendMessage("Please send the file Size:" );
            fileSize = Long.parseLong(this.in.nextLine().trim());
            System.out.println(senderUserName +" would like to send file: "+fileName+" to user "+recieverName);  
            System.out.println("The Size of the file is: "+fileSize);
            ArrayList<Service> socketId2 = socketUser.get(recieverName);
            
            this.isRecieverConnected = isUserconnected(recieverName);
            
            //File name recieved from the sender
            newfileName = "ServerSide_RecievedFrom_"+senderUserName+"_TO_"+recieverName+"_"+fileName;

            //File name recieved from the server after it get it from the sender.
            fileNameToSend = "ClientSide_RecievedFrom_"+senderUserName+"_TO_"+recieverName+"_"+fileName;
            
            if(this.isRecieverConnected){
                //out.println("<Server MSG>User is connected !" );
                //out.println("User <"+recieverName+"> is connected !" );
                sendMessage("<Server MSG>User is connected !" );
                sendMessage("User <"+recieverName+"> is connected !" );
                System.out.println("User <"+recieverName+"> is connected !" );
                //ut.println("<Server MSG>Server is ready, you can start sending the file NOW !" );
                sendMessage("<Server MSG>Server is ready, you can start sending the file NOW !" );

                //Recieve file from sender
                //DataInputStream fileDataIntStream = new DataInputStream(socket.getInputStream());
                FileOutputStream fos = new FileOutputStream(newfileName);
                byte[] buffer = new byte[8000];
                int count;
                 
                while ((count = fileDataIntStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);                    
                }
                /*
                while (fileSize > 0 && (count = socket.getInputStream().read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                    fos.write(buffer, 0, count);
                    fileSize -= count;
                }
                */
                fos.flush();
                //fos.close();
                System.out.println("finish receiving file from user.");
                //fileDataIntStream.close();ccc
               
            //} //End if (this.isRecieverConnected) 
                //inNew = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //in = inNew;
        
                
               // while ((endSendingFile = in.readLine()) == null){    
                //endSendingFile = in.readLine();     
              
                //}
                //System.out.println(endSendingFile);
                //out.println("<Server MSG>file transfert has finished !" );
                //this.sendMessage("<Server MSG>file transfert has finished !");
                //System.out.println(in.readLine());
                
                
                //Send File to reciever
                //Get the ucer reciver socket:
                //trasferFileToClient(recieverName, senderUserName, newfileName,fileNameToSend);
                 
                ArrayList<Service> socketId3 = socketUser.get(recieverName);
                for (Service client : socketId3) {
                        System.out.println ("send to recever");
                        client.sendMessage("<Server MSG>You have a file to receive !");
                        client.sendMessage("[SENDER]: " +senderUserName);
                        client.sendMessage("[FILENAME]: " +fileNameToSend);
                        client.sendMessage(senderUserName + " send you a file");
                        System.out.println(senderUserName + " sent a file to " + recieverName);
                        System.out.println("sending file to " +recieverName + "....");

                        try {
                            DataOutputStream dataOutStream = new DataOutputStream(this.socket.getOutputStream());
                            FileInputStream fis = new FileInputStream(newfileName);
                            byte[] bufferToSend = new byte[8000];
                            int count2=0;
                            while ((count2 = fis.read(bufferToSend)) > 0) {
                                client.dataOutStream.write(bufferToSend, 0, count2);
                                
                            }
                            dataOutStream.flush();
                            fis.close();
                            dataOutStream.close();

                            client.sendMessage("END");
                            client.sendMessage("Finishing sending file to " + recieverName + "....");
                            client.sendMessage("End sending file");
                            System.out.println("Finishing sending file to " + recieverName + "....");
                            System.out.println("End sending file to reciever");


                        
                            break;



                        } catch (IOException e) {
                            
                            e.printStackTrace();
                        }
                }//END for (Service client : socketId3)
                 
                break;

            } //End if (this.isRecieverConnected) 
        
            else{
                //out.println("<Server MSG>User"+recieverName+" is not connected !" );
                sendMessage("<Server MSG>User"+recieverName+" is not connected !" );
                System.out.println("User"+recieverName+" is not connected !");
                //out.println("<Server MSG>Please try agin !" );
                sendMessage("<Server MSG>Please try agin !" );
                
            }      
                
        } //end While(true)
    } 

    

    //This function is called by the previous one to send the files to the destination user
    private void trasferFileToClient(String recieverName, String senderUserName, String newfileName, String fileNameToSend) {
        ArrayList<Service> socketId3 = socketUser.get(recieverName);
                for (Service client : socketId3) {
                        System.out.println ("send to recever");
                        client.sendMessage("<Server MSG>You have a file to receive !");
                        client.sendMessage("[SENDER]: " +senderUserName);
                        client.sendMessage("[FILENAME]: " +fileNameToSend);
                        client.sendMessage(senderUserName + " send you a file");
                        System.out.println(senderUserName + " sent a file to " + recieverName);
                        System.out.println("sending file to " +recieverName + "....");

                        try {
                            //DataOutputStream dataOutStream = new DataOutputStream(this.socket.getOutputStream());
                            FileInputStream fis = new FileInputStream(newfileName);
                            byte[] bufferToSend = new byte[8000];
                            int count2=0;
                            while ((count2 = fis.read(bufferToSend)) > 0) {
                                client.dataOutStream.write(bufferToSend, 0, count2);
                                
                            }
                            dataOutStream.flush();
                            fis.close();
                            dataOutStream.close();

                            client.sendMessage("END");
                            client.sendMessage("Finishing sending file to " + recieverName + "....");
                            client.sendMessage("End sending file");
                            System.out.println("Finishing sending file to " + recieverName + "....");
                            System.out.println("End sending file to reciever");


                        
                            break;



                        } catch (IOException e) {
                            
                            e.printStackTrace();
                        }
                }//END for (Service client : socketId3) 
    }//END Funciton trasferFileToServer

    private boolean isUserconnected(String recieverName) {

        boolean connected = false;
        for (String usr : userList){
            if (recieverName.equals(usr))
           {
            connected = true;
           } 

        } 
        return connected;
    }//end function isUserconnected

    private void chatGroupHandler(HashMap<String, ArrayList<Service>> chatGroups2) throws IOException {
        String groupName;
        
        //out.println("Please enter the name of the groupe that you would like to connect to !"); 

        sendMessage("Please enter the name of the groupe that you would like to connect to !");
        groupName = this.in.nextLine().trim();

        ArrayList<Service> groupList = chatGroups.get(groupName);

        //check if there is a existing group named groupName
        //If it's a new group, we add an intry in the hashMap chatGroups.
        if(groupList == null){
            //out.println("New groupe was created: " + groupName);
            sendMessage("New groupe was created: " + groupName);
            System.out.println("New groupe was created: " + groupName);
            groupList = new ArrayList<>();
            chatGroups.put(groupName, groupList);
        }
        //In any case  we add the socket Id of the user to the groupList
        //out.println("You just joined this group: " + groupName);
        //out.println("Welcome to group " + groupName + "!");
        sendMessage("You just joined this group: " + groupName);
        sendMessage("Welcome to group " + groupName + "!");
        System.out.println("New groupe was created: " + groupName);
        groupList.add(this);

        //Launch group conversation
        while (true){ 
            String input = this.in.nextLine().trim();
            if (input == null) {
                return;
            }
            if (input.equals("quit")){
                //out.println("<Server MSG>User left the group");
                sendMessage("<Server MSG>User left the group");
                System.out.println("User left group " + groupName);
                //TODO: list users in the group + inform everyone when a user leave
                break;
            } 
            for (Service usr : groupList) {
                usr.sendMessage(name + ": " + input);
            }
        }//End while

    }//End Function

    private void removeUserFromUserList(String userName, List<String> userList2) {
        
        if (!userName.equals("") || userName.equalsIgnoreCase(null)){ 
            userList2.remove(userName);
        }
    }

    private String getUserName(HashMap<String, ArrayList<Service>> socketUser2) {
        String userName= "" ;        
        for(Entry<String, ArrayList<Service>> index : socketUser2.entrySet()){
                    String user = index.getKey();
                    ArrayList<Service> value = index.getValue();
            for (Service client : value) {
                if (client.equals(this)){
                    userName = user;
                           
                } 
            } 
                                   
        }//End for
        return userName;
    } 
}