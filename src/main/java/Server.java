import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
    private static final int CONCURRENT_CONNECTIONS = 4;

    // Create a semaphore
    private static Semaphore semaphore = new Semaphore(CONCURRENT_CONNECTIONS);

    public static void main(String[] args) {
        new Server();
    }


    void loadFile() {
        try {
            File file = new File("Data.save");
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Info = (CopyOnWriteArrayList<Task>) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception ignored) {
        }
    }

    void saveFile() {
        try {
            File file = new File("Data.save");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(Info);
            oos.close();
            fos.close();
        } catch (Exception ignored) {
        }
    }

    public Server() {
        try {
            loadFile();
            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(8000);
            System.out.println("Server started ");
            ExecutorService exec = Executors.newFixedThreadPool(4);
            exec.execute(new ProgramStopper(exec));
            while (true) {
                // Listen for a new connection request
                Socket socket = serverSocket.accept();
                // Create and start a new thread for the connection
                exec.execute(new HandleAClient(socket));
            }
        } catch (IOException ex) {
            System.out.println("Catched ex in body of Server():\n" + Arrays.toString(ex.getStackTrace()));
        } finally {
            try {
                objectFromClient.close();
                objectToClient.close();
                dataFromClient.close();
                dataToClient.close();
            } catch (Exception ex) {
                System.out.println("Catched ex in final of Server():\n" + Arrays.toString(ex.getStackTrace()));
            }
        }
    }

    class ProgramStopper implements Runnable {
        ExecutorService exec;

        public ProgramStopper(ExecutorService exec) {
            this.exec = exec;
        }

        @Override
        public void run() {
            while (true) {
                String in = new Scanner(System.in).next();
                if (in.equalsIgnoreCase("End")) {
                    saveFile();
                    try {
                        exec.shutdown();
                        objectFromClient.close();
                        objectToClient.close();
                        dataFromClient.close();
                        dataToClient.close();
                    } catch (Exception ex) {
                        System.out.println("Empty end");
                    }
                    System.exit(100);
                }
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
            System.out.println("Connected: " + socket.toString());
        }

        /* Run a thread */
        public void run() {
            try {
                semaphore.acquire(); // Acquire a permit
                while (socket.isConnected()) {
                    saveFile();
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
                            System.out.println("New task added, number of Tasks: " + Info.size());
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
                            boolean ans = false;
                            if (id >= 0) {
                                try {
                                    Info.get(id).setArchived(true);
                                    ans = true;
                                    System.out.println("Task archived: " + id);
                                } catch (Exception ex) {
                                    System.out.println("Client " + socket.toString() + "tried to remove nonexistent task");
                                }
                            }
                            dataToClient = new DataOutputStream(socket.getOutputStream());
                            dataToClient.writeBoolean(ans);
                            break;
                        }
                    }
                }
            } catch (SocketException ex) {
                System.out.println("Client " + socket.toString() + " disconnected");
            } catch (ClassNotFoundException | IOException | InterruptedException ex) {
                System.out.println("Catched ex in run() of client:\n" + Arrays.toString(ex.getStackTrace()));
            } finally {
                semaphore.release(); // Release a permit
            }
        }
    }
}