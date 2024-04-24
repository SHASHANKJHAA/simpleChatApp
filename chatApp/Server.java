import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> Connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server(){
        Connections = new ArrayList<ConnectionHandler>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while(!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client,  this);
                Connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutdown();
        }
    }

    public void broadcast(String message) {
        for(ConnectionHandler ch : Connections) {
            if(ch != null){
                ch.sendMessage(message);
            }
        }
    }

    public void broadcast(String message, ConnectionHandler sender) {
        for(ConnectionHandler ch : Connections) {
            if(ch != sender){
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown() {
        try {
            done = true;

            if (!server.isClosed()) {
                server.close();
            }
            for(ConnectionHandler ch: Connections){
                ch.shutdown();
            }
        }catch (IOException e) {
            // ignore
        }
    }

    public ArrayList<ConnectionHandler> getConnections() {
        return Connections;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}

class ConnectionHandler implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;
    private Server server;

    public ConnectionHandler(Socket client, Server server){
        this.client = client;
        this.server = server;
    }

    @Override
    public void run(){
        try{
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out.println("Please enter your name: ");
            nickname = in.readLine();
            System.out.println(nickname + " connected");
            server.broadcast(nickname + " joined the chat!");
            String message;
            while((message = in.readLine()) != null){
                if(message.startsWith("/nick ")){
                    String[] messageSplit = message.split(" ", 2);
                    if(messageSplit.length == 2){
                        server.broadcast(nickname + "renamed themselves to " + messageSplit[1]);
                        nickname = messageSplit[1];
                        out.println("Successfully renamed themselves to " + nickname);
                    }else{
                        out.println("No nickname provided!");
                    }
                }else if(message.startsWith("quit")){
                    server.broadcast(nickname + " left the chat!");
                    shutdown();
                    System.out.println(nickname + " disconnected");
                }else{
                    server.broadcast(nickname + ": " + message);
                }
            }
        }catch(IOException e){
            shutdown();
        }

    }public void sendMessage(String message){
        out.println(message);
    }

    public void shutdown(){
        try {
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
            server.getConnections().remove(this);
        }catch (IOException e) {
            //ignore
        }
    }
}