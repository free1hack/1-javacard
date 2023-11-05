import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClient extends Thread {
    private static final String SERVER = "localhost";
    private static final int PORT = 4000;
    private static final int PORTFILE = 4100;
    //private static String serverMsg;
    private  static BufferedReader inputNetwork ; //what's the clients is recieving from the server.
    private  static PrintWriter outputNetwork, outputConsole, outputNetwork2;// outputNetwork = What the clients is sending to the Server /// outputConsole = printing what the clients is typing
    private static Socket socket, socketFile ;
    private   DataOutputStream dataOutStream; //For sending file
    //private DataInputStream  fileDataIntStream;//for recieving file
    private  Scanner inputConsole;//what the client has entered.
    private static boolean isUserAuthenticated, userAuthenticating,
         isServerMenuPrinted, msgTooptionOk, msgAllOptionOk, msgGrpOptionOK, 
         sendFileOptionOk, fileNameRequested, receivingFile, 
         remoteUserConnected, stratSendingFile, isFileSent, fileRecievedFromServer  = false;
    private int numberTimesOfFileOptionCalled = 0;
    private String fileName;
    private String totoo;

    public ChatClient() throws IOException{
        //socket = new Socket(SERVER, PORT);
        initStream();
        userAuthentication();
        start();
        listenConsole ();
        
        

    } 

    private void initStream() {
        this.inputConsole = new Scanner(System.in);
        ChatClient.outputConsole = new PrintWriter(System.out);
        try {
            this.socket = new Socket(SERVER, PORT);
            //this.socketFile = new Socket(SERVER,PORTFILE);
            this.inputNetwork = new BufferedReader(new InputStreamReader(socket.getInputStream())); //Ecouter
            this.outputNetwork = new PrintWriter(socket.getOutputStream(), true);
            
            dataOutStream = new DataOutputStream(socket.getOutputStream());//To send files
            //fileDataIntStream = new DataInputStream(socket.getInputStream());//To receive the file from the server.

            //dataOutStream = new DataOutputStream(socketFile.getOutputStream());//To send files
            //fileDataIntStream = new DataInputStream(socket.getInputStream());//To receive the file from the server.
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }//END initStream

    /*    
    private void initStreamFile() {

        try {
            socketFile = new Socket(SERVER, PORTFILE);
            //To send the file to the server
            dataOutStream = new DataOutputStream(socketFile.getOutputStream());
            //To receive file from server
            fileDataIntStream = new DataInputStream(socketFile.getInputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    } 
    */
    
    
     

    public static void main(String[] args) throws IOException  {
        
       
        new ChatClient();
       
        //listenConsole ();
        //System.out.print("Enter your name: ");
        //String name = scanner.nextLine();
        //out.println(name);
        //function listnerConsol
        //(new Thread(new IncomingMessageHandler(inputNetwork))).start();
        /* 
        while (true){
            String serverMsg = inputNetwork.readLine().trim();
            //printInConsole(serverMsg);
           // System.out.println(serverMsg);
            if(serverMsg.equals(" ")){ 
                String response = inputConsole.nextLine();
                outputNetwork.println(response);
                if (response.equals("quit")){
                    break;
                }
            } 
 
        } 
        */
        /* 
        System.out.print("Enter the group to join: ");
        String group = inputConsole.nextLine();
        out.println(group);
        */
        //System.out.println(in.readLine());

        
        /* 
        while (true) {
            String message = inputConsole.nextLine();
            outputNetwork.println(message);
        }
        */
    } //End main.

    //this function is to print a message from the Server of the Client
    private void printInConsole (String msg){
        ChatClient.outputConsole.println(msg);
        ChatClient.outputConsole.flush();
    } 

    private String readUserResponse(){
        ///String response="none";
        //if (this.inputConsole.hasNextLine()){
        
            String response = this.inputConsole.nextLine().trim();
        //} 
            
        
        return response;
    } 

    private static void sendMsgToServer(String msg){
        ChatClient.outputNetwork.println(msg);
        ChatClient.outputNetwork.flush();
    } 

    private void closingConsole(){
        this.inputConsole.close();
    }
    
    private void  closeNetwork() throws IOException{
        this.inputNetwork.close();
    } 
    private void listenNetwork() throws IOException{
        String serverMsg;
        
        
            //to be changed with a switch + serverMsgParger() function
            while((serverMsg = inputNetwork.readLine()) != null){
                //printInConsole(serverMsg);
                if(serverMsg.startsWith("<Server MSG> User authentication")){
                    //userAuthentication();

                }
                if(serverMsg.startsWith("<Server MSG> printing the menu")){
                    printServerMenu();  
                }
                if(serverMsg.startsWith("<Server MSG>listing users")){
                    listUsers();
                }
                if(serverMsg.startsWith("<Server MSG>printing the help menu")){
                    printHelpMenue();
                }
                //msgTo option:
                if(serverMsg.startsWith("<Server MSG>msgTo option")){
                    msgTooptionOk = true;
                    //sendMsgToOneUser();
                }
                if (msgTooptionOk == true){
                    printInConsole(serverMsg);
                } 
                if (serverMsg.equals("you left msgTo option")){
                    msgTooptionOk = false;
                } 
                
                //MsgAll Option
                if(serverMsg.startsWith("<Server MSG>msgAll option")){
                    msgAllOptionOk = true;
                    //sendMsgToOneUser();
                }
                if (msgAllOptionOk == true){
                    printInConsole(serverMsg);
                } 
                if (serverMsg.equals("you left msgAll option")){
                    msgAllOptionOk = false;
                }

                //MsgGrp Option
                if(serverMsg.startsWith("<Server MSG>Chat group option")){
                    msgGrpOptionOK = true;
                    //sendMsgToOneUser();
                }
                if (msgGrpOptionOK == true){
                    printInConsole(serverMsg);
                } 
                if (serverMsg.equals("<Server MSG>User left the group")){
                    msgGrpOptionOK = false;
                }

                //SendFile Option
                if(serverMsg.startsWith("<Server MSG>sendFile option")){
                    
                    numberTimesOfFileOptionCalled++;
                    sendFileOptionOk = true;
                    //sendMsgToOneUser();
                }
                if (sendFileOptionOk == true && serverMsg.startsWith("Please enter the file name")){
                    //printInConsole(serverMsg);
                    fileNameRequested = true;
                    
                } 
                if (serverMsg.startsWith("<Server MSG>User is connected !")){
                    remoteUserConnected = true;
                }
                if (serverMsg.startsWith("<Server MSG>Server is ready, you can start sending the file NOW !")){
                    stratSendingFile = true;
                }
                if (serverMsg.startsWith("Please send the file Size:")){
                    File f = new File (fileName);
                    outputNetwork.println(f.length());
                } 
                if (sendFileOptionOk){
                    //printInConsole(serverMsg);
                    /* 
                    if(numberTimesOfFileOptionCalled >1){
                        System.out.println("value of fileOptionNotFirstOcc: "+numberTimesOfFileOptionCalled);
                        //initStreamFile(); 
                    }
                    */ 
                    isFileSent=sendFile(serverMsg, fileName, remoteUserConnected, stratSendingFile );
                }
                if (isFileSent){
                    sendFileOptionOk = false;
                    remoteUserConnected = false;
                    stratSendingFile = false;
                    isFileSent = false;
                    //initOutStream();
                    //initStream();
                    
                }     
                if (serverMsg.equals("<Server MSG>file transfert has finished !")){
                   
                    printInConsole(serverMsg);
                } 

                //Recive file from server:
                if(serverMsg.startsWith("<Server MSG>You have a file to receive !")){
                    receivingFile = true;
                    
                }
                if (receivingFile){
                      fileRecievedFromServer = recieveFile(serverMsg);
                    
                } 
                if (fileRecievedFromServer){
                    receivingFile = false;
                } 
                if(serverMsg.startsWith("<Server MSG>") && serverMsg.endsWith("is now connected")){
                    receivingFile = true;
                    
                }
                

                

            }           

    } 

    private void initOutStream() {
        try {
            outputNetwork = new PrintWriter(socket.getOutputStream(), true);
            outputNetwork.println("hello their");
            sendMsgToServer("vsdfvsdfb");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean recieveFile(String serverMsg) throws IOException {
        String ReceivedFileName;
        boolean fileRecievedFromServer2=false;
        printInConsole(serverMsg);
        
        if (serverMsg.startsWith("[FILENAME]:")){
            String[] attributes = serverMsg.split(" ");
            //ReceivedFileName = serverMsg.substring(2, serverMsg.indexOf(" "));
            ReceivedFileName = attributes[1]; 
            printInConsole("Receiving file :"+ReceivedFileName + " ....");
            DataInputStream fileDataIntStream = new DataInputStream(socket.getInputStream());
            //Recieve file from sender
            FileOutputStream fos = new FileOutputStream(ReceivedFileName);
            byte[] buffer = new byte[8000];
            int count;
            while ((count = fileDataIntStream.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
           
            fos.flush();
            //fos.close();
            fileDataIntStream.close();
            fileRecievedFromServer2= true;
            
        }//END if
        return fileRecievedFromServer2;
    }//End function

    private boolean sendFile(String serverMsg, String fileName2, boolean remoteUserConnected2, boolean stratSendingFile2) {
        
        File file;
        printInConsole(serverMsg);
        boolean isFileSent2=false;
        long fileSize;


        if (remoteUserConnected2){
            //while (true){
                file = new File(fileName2.trim());
                fileSize = file.length();
                //sendMsgToServer("serverMsg");
                if (!file.exists()) {
                    System.err.println(" File "+fileName2+" does not exist");
                    printInConsole(" File "+fileName2+" does not exist");
                    sendMsgToServer("File does not exist");    
                    //return;
                    //break;
                }else if (stratSendingFile2){ 
                    try {                  
                            //DataOutputStream dataOutStream = new DataOutputStream(socket.getOutputStream());
                             
                            FileInputStream fis = new FileInputStream(fileName2);
                            
                            byte[] buffer = new byte[8000];
                            int count;
                            printInConsole("start sending file to server....");
                            while ((count = fis.read(buffer)) > 0) {
                                dataOutStream.write(buffer, 0, count);
                                //socket.getOutputStream().write(buffer, 0, count);
                            }
                            dataOutStream.flush();
                            fis.close();
                            
                            dataOutStream.close();
                            //socketFile.close();
                            //socket.getOutputStream().flush();

                            //socket.shutdownOutput();
                            
                            //outputNetwork.flush();
                            //inputNetwork.close();
                            //outputNetwork.close();
                            //this.outputNetwork.flush();
                            //PrintWriter outputNetworkNew = new PrintWriter(socket.getOutputStream(), true);
                            //this.outputNetwork = outputNetworkNew;
                            //this.outputNetwork.println("END");
                            
                            //ChatClient.sendMsgToServer("<Client MSG>End Sending file");
                            
                            printInConsole("Finishing sending file to server....");
                            //sendMsgToServer("End sending file");
                            
                            isFileSent2=true;
                            
                            //break;
                        


                    } catch (SocketException e) {
                        if (e.getMessage().equals("Socket is closed")) {
                        //if (e.getMessage().startsWith("java.net.SocketException: Socket closed")) {    
                            printInConsole("Closing connection with the server for sending files....");
                            printInConsole("Connection closed with the server for sending files.");
                           
                            //break;
                        } else if (e.getMessage().equals("Socket output is shutdown")) {
                            //if (e.getMessage().startsWith("java.net.SocketException: Socket closed")) {    
                                printInConsole("Socket output is shutdown with the server for sending files....");
                                
                               
                                //break;
                        }else {
                            //printInConsole("problem with the client. Please contact your administrator.");
                            e.printStackTrace();
                            //break;
                        }
                    
                    } catch (IOException e) {
                        //if (e.getMessage().equals("Socket is closed")) {
                        if (e.getMessage().startsWith("java.net.SocketException: Socket closed")) {    
                            printInConsole("Closing connection with the server for sending files....");
                            printInConsole("Connection closed with the server for sending files.");
                        } else {
                            //printInConsole("problem with the client. Please contact your administrator.");
                            //System.out.println("toto2");
                            e.printStackTrace();
                            //break;
                        }
                    
                    } 

                } //End else if (stratSendingFile2)
                else{
                    //break;
                }  

            //}//end while(true)
        
            
        }
        return isFileSent2; 


    } //end function sendFile

    

    private  void sendMsgToOneUser() throws IOException {
        String serverMsg;// = this.inputNetwork.readLine().trim();
        String r;

        while((serverMsg = inputNetwork.readLine()) != null){
            printInConsole(serverMsg);
      
            if (serverMsg.equals("Please enter the name of the user that you would like to send a message to")){
                //System.out.println("response");
                                //r = readUserResponse();
                                //r = this.inputConsole.nextLine();
                                //Scanner inputConsole2 = new Scanner(System.in);
                                //r = inputConsole2.nextLine();
                //Scanner inputConsole2 = new Scanner(System.in);
                try{ 
                    //inputConsole.reset();
                    
                    //r = inputConsole.nextLine().trim();
                    //System.out.println(r);
                    //sendMsgToServer(r);
                }catch(IndexOutOfBoundsException e){
                    e.printStackTrace();
                } 
                 
                            
            } 
            if (serverMsg.equals("you can start chating now")){
                while(true){
                    printInConsole(serverMsg);
                    Scanner inputConsole3 = new Scanner(System.in);
                    String r3 = inputConsole3.nextLine();
                    sendMsgToServer(r3);
                    if (r3.equals("quit")){
                        break;
                    } 
                } 
                //printInConsole(serverMsg);
                //sendMsgToServer(readUserResponse());
            } 
            /* 
            if (readUserResponse().equals("quit")){
                break;
            } 
            */
        } 

    }

    private void printHelpMenue() throws IOException {
        String serverMsg;// = this.inputNetwork.readLine().trim();

        while((serverMsg = inputNetwork.readLine()) != null){
            printInConsole(serverMsg);
            if (serverMsg.startsWith("<Server MSG>printing the help menu ends")){
               
                break;
            } 
        } 
    }

    private void listUsers() throws IOException{
        String serverMsg;// = this.inputNetwork.readLine().trim();

        while((serverMsg = inputNetwork.readLine()) != null){
            printInConsole(serverMsg);
            if (serverMsg.startsWith("<Server MSG>listing users ends")){
               
                break;
            } 
        } 


    } 
    private void printServerMenu() throws IOException {
        String serverMsg;// = this.inputNetwork.readLine().trim();

        while((serverMsg = inputNetwork.readLine()) != null){
            printInConsole(serverMsg);
            if (serverMsg.startsWith("<Server MSG> end printing the menu")){
                isServerMenuPrinted = true;
                break;
            } 
        } 

    }

    private void userAuthentication() throws IOException {
        String serverMsg;// = this.inputNetwork.readLine().trim();
       
        while((serverMsg = inputNetwork.readLine()) != null){
         
            if (serverMsg.startsWith("Please enter your login :")){
                    printInConsole(serverMsg);
                    sendMsgToServer(readUserResponse());
            } 
            else if(serverMsg.startsWith("Please enter your password :")){
                printInConsole(serverMsg);
                sendMsgToServer(readUserResponse());
            } 
            else if(serverMsg.startsWith("Authentication successful")){
                printInConsole(serverMsg);
                isUserAuthenticated = true;
                break;
                
            }
            else if(serverMsg.startsWith("your account is created")){
                printInConsole(serverMsg);
                isUserAuthenticated = true;
                break;
                
            } else if (serverMsg.startsWith("You're already connected !")){

                printInConsole(serverMsg);
                printInConsole("please disconnect from other onpened session. Click on < Ctrl+C > to quit");

            } else if (serverMsg.startsWith("[ERROR]")|| serverMsg.startsWith("<Server MSG>") ){
                printInConsole(serverMsg);
            } 
            
            
        } 
        
    }

    private static int responseParser(String resp) {
        int respInt = 0;
        if (resp.equals("quit")|| resp.equals("0")){
            respInt = 0;
        } 
        if (resp.equals("msgTo")|| resp.equals("1")){
            respInt = 1;
        } 
        if (resp.equals("msgAll")|| resp.equals("2")){
            respInt = 2;
        } 
        if (resp.equals("list")|| resp.equals("3")){
            respInt = 3;
        } 
        if (resp.equals("chatGrp")|| resp.equals("4")){
            respInt = 4;
        } 
        if (resp.equals("sendFile")|| resp.equals("5")){
            respInt = 5;
        } 
        if (resp.equals("help")|| resp.equals("6")){
            respInt = 6;
        } 
        return respInt;
        
    }


    private  void listenConsole (){
        String resp;
        while (inputConsole.hasNextLine()){
            resp = inputConsole.nextLine().trim();
           
            if (fileNameRequested == true){
                fileName = resp;
                fileNameRequested = false;

            } /*else if (toto){
                System.out.println("inside listerconsole");
                totoo= resp;
                toto = false;
                sendMsgToServer(totoo);

            } */
            //sendMsgToServer(resp);
            switch(responseParser(resp)){
                case 0:  sendMsgToServer(resp);break;
                case 1:  sendMsgToServer(resp);break;
                case 2:  sendMsgToServer(resp);break;
                case 3:  sendMsgToServer(resp);break;
                case 4:  sendMsgToServer(resp);break;
                case 5:  sendMsgToServer(resp);break;
                case 6:  sendMsgToServer(resp);break;
                default :break;

            }//End switch   
            

        } 

    } 

   

    @Override
    public void run() {
        try {
            listenNetwork();
        } catch (IOException e) {
            // TODO Auto-generated catch block
           // e.printStackTrace();
        }
        /* 
        while (true) {
            try {
                String message = inputNetwork.readLine(); //reda from input
                if (message == null) {
                    return;
                }
                    System.out.println(message);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
            */
        }

}
