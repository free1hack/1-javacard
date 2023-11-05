import java.io.*;
import java.net.*;




public class Client {
    
    private boolean isAdmin;
    private PrintWriter out;
    private Socket socket;
    private String username;
    private String password;


    //For User
    public Client(PrintWriter out, Socket socket){ 
        this.out = out;
        this.isAdmin = false;
        this.socket = socket;
    }

    //For Admin
    public Client(PrintWriter out){ 
        this.out = out;
        this.isAdmin = true;
        this.username = "ADMIN";
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public boolean isAdmin(){
        return isAdmin;
    } 

}
