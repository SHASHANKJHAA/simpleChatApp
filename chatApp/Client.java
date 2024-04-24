import java.io.*;
import java.net.*;

public class Client implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    public Client() {
        try {
            client = new Socket("localhost", 9999);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: localhost");
            shutdown();
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to localhost.");
            shutdown();
        }
    }

    @Override
    public void run() {
        try {

            inputHandler inHandler = new inputHandler();

            Thread t = new Thread(inHandler);

            t.start();


            if (in != null) {

                String inMessage;

                while ((inMessage = in.readLine()) != null) {

                    System.out.println(inMessage);

                }

            } else {

                System.err.println("Error: in variable is null");

            }

        } catch (IOException e) {

            System.err.println("Error reading from server: " + e.getMessage());

            shutdown();

        }
    }

    class inputHandler implements Runnable {

        @Override
        public void run() {

            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inReader.readLine();
                    if (message.equals("exit")) {
                        inReader.close();
                        shutdown();

                    } else {
                        out.println(message);
                    }
                }

            } catch (IOException e) {
                System.err.println("Error reading from console: " + e.getMessage());
                shutdown();
            }
        }
    }

    public void shutdown() {
        done = true;
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (client != null && !client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.run();
        } catch (Exception e) {
            System.err.println("Error running client: " + e.getMessage());
        }
    }
}