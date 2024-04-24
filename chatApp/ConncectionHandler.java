import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;



class connectionHandler implements Runnable {
    private Socket client;
    private BufferedReader in;
    private String nickname;
    private List<PrintWriter> writers;

    public connectionHandler(Socket client, List<PrintWriter> writers) {
        this.client = client;
        this.writers = writers;
    }

    @Override
    public void run() {
        PrintWriter out;
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
            out.println("Please enter your name: ");
            nickname = in.readLine();
            out.println(nickname + " connected");
            broadcast(nickname + " has joined the chat!");
            writers.add(out);

            String message;
            while ((message = in.readLine()) != null) {
                broadcast(nickname + ": " + message);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void broadcast(String message) {
        for (PrintWriter writer : writers) {
            writer.println(message);
        }
    }
    private void disconnect() {
        PrintWriter out = null;
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                System.err.println("Error closing input stream: " + e.getMessage());
            }
        }
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}
