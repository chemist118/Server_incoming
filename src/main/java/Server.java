import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    // IO Streams
    private ObjectInputStream objectFromClient;
    private ObjectOutputStream objectToClient;
    private DataOutputStream dataToClient;
    private DataInputStream dataFromClient;

    // Create a list of Tasks
    private CopyOnWriteArrayList<Task> Info = new CopyOnWriteArrayList<>();

    // Limit the concurrent connections to two clients
    private static final int CONCURRENT_CONNECTIONS = 2;

    // Create a semaphore
    private static Semaphore semaphore = new Semaphore(CONCURRENT_CONNECTIONS);

    public static void main(String[] args) {
        new Server();
    }

    public Server() {
        try {
            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(8000);
            System.out.println("Server started ");
            ExecutorService exec = Executors.newFixedThreadPool(2);
            while (true) {
                // Listen for a new connection request
                Socket socket = serverSocket.accept();
                // Create and start a new thread for the connection
                exec.execute(new HandleAClient(socket));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                objectFromClient.close();
                objectToClient.close();
                dataFromClient.close();
                dataToClient.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Define the thread class for handling new connection
    class HandleAClient implements Runnable {
        private Socket socket; // A connected socket

        /**
         * Construct a thread
         */
        public HandleAClient(Socket socket) {
            this.socket = socket;
            System.out.println("connected: " + socket.toString());
        }

        /* Run a thread */
        public void run() {
            try {
                semaphore.acquire(); // Acquire a permit
                while (socket.isConnected()) {
                    // Create an input stream from the socket
                    dataFromClient = new DataInputStream(socket.getInputStream());
                    String command = dataFromClient.readUTF();
                    switch (command) {
                        case "LOAD_INFO": { // Send all data
                            objectToClient = new ObjectOutputStream(socket.getOutputStream());
                            objectToClient.writeObject(Info);
                            break;
                        }
                        case "ADD_TASK": { // Add task to list
                            objectFromClient = new ObjectInputStream(socket.getInputStream());

                            // Read task from input and add it to Info
                            Task task = (Task) objectFromClient.readObject();
                            Info.add(task);
                            task.setId(Info.indexOf(task));
                            task.header += task.id;
                            System.out.println("Number of Tasks: " + Info.size());
                            objectToClient = new ObjectOutputStream(socket.getOutputStream());
                            objectToClient.writeObject(task);
                            break;
                        }
                        case "UPDATE_TASK": { // Update task
                            objectFromClient = new ObjectInputStream(socket.getInputStream());
                            // Read task from input and add it to Info
                            Task task = (Task) objectFromClient.readObject();
                            Info.set(task.id, task);
                            System.out.println("Task update: " + task.getId());
                            break;
                        }
                        case "REMOVE": { // Remove task from list
                            dataFromClient = new DataInputStream(socket.getInputStream());
                            int id = dataFromClient.readInt();
                            Task t = Info.get(id);
                            boolean ans = false;
                            if (id > 0) {
                                try {
                                    Info.get(id).setArchived(true);
                                    ans = true;
                                } catch (Exception ex) {
                                    ans = false;
                                }
                            }
                            dataToClient = new DataOutputStream(socket.getOutputStream());
                            dataToClient.writeBoolean(ans);
                            break;
                        }
                    }
                }
            } catch (SocketException ex) {
                System.out.println("Client " + socket.toString() +" disconnected");
            } catch (ClassNotFoundException | IOException | InterruptedException ex) {
                ex.printStackTrace();
            } finally {
                semaphore.release(); // Release a permit
            }
        }
    }
}