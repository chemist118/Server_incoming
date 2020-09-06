import java.io.*;
import java.net.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
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

    synchronized CopyOnWriteArrayList<Task> LoadTasks(String command) {
        CopyOnWriteArrayList<Task> tasks = new CopyOnWriteArrayList<>();
        try (Connection conn = DBHandler.getConn()) {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(command);
            tasks = readRS(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    CopyOnWriteArrayList<Task> readRS(ResultSet rs) {
        CopyOnWriteArrayList<Task> tasks = new CopyOnWriteArrayList<>();
        try {
            int lastID = -1;
            Task lastTask = null;
            while (rs.next()) {
                int id = rs.getInt("id");
                String head = rs.getString("t.Header");
                String des = rs.getString("t.Description");
                Boolean done = rs.getBoolean("t.Done");
                Boolean arch = rs.getBoolean("t.Archived");
                Date temp = rs.getDate("t.Date");
                LocalDate date;
                if (temp != null)
                    date = temp.toLocalDate();
                else date = LocalDate.MAX;
                Boolean haveDate = rs.getBoolean("t.HaveDate");
                String tag = rs.getString("tags.tag");
                SubTask subTask = new SubTask(rs.getString("s.Header"), rs.getString("s.Description"), rs.getBoolean("s.Done"), false);
                if (lastID != id) {
                    lastTask = new Task(id, head, des, null, null, done, arch, haveDate, date);
                    lastID = id;
                    tasks.add(lastTask);
                }
                if (subTask.header != null & subTask.description != null & lastTask != null)
                    if (!lastTask.subtasks.contains(subTask))
                        lastTask.subtasks.add(subTask);
                if (tag != null & lastTask != null)
                    if (!lastTask.tags.contains(tag))
                        lastTask.setListOfTags(lastTask.listOfTags += ',' + tag);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    synchronized void ArchiveTask(Integer id) {
        try (Connection conn = DBHandler.getConn()) {
            PreparedStatement statement = conn.prepareStatement(
                    "UPDATE tasks SET Archived = TRUE WHERE id = ?;",
                    Statement.RETURN_GENERATED_KEYS
            );
            Task task = Info.get(id);
            statement.setInt(1, task.id);
            statement.executeUpdate();
            Optional.ofNullable(task.subtasks).orElse(new ArrayList<>())
                    .forEach(subTask -> ArchiveSubTask(id));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    synchronized void ArchiveSubTask(int taskId) {
        try (Connection conn = DBHandler.getConn()) {
            PreparedStatement statement = conn.prepareStatement(
                    "UPDATE subtasks SET Archived = TRUE WHERE Task_id = ?;",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setInt(1, taskId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    synchronized void InsertTask(Task task) {
        try (Connection conn = DBHandler.getConn()) {
            PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO tasks(id, Header, Description, Done, Archived, HaveDate, Date) VALUES (?,?,?,?,?,?,?);",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setInt(1, task.id);
            statement.setString(2, task.header);
            statement.setString(3, task.description);
            statement.setBoolean(4, task.done);
            statement.setBoolean(5, task.archived);
            statement.setBoolean(6, task.haveDate);
            statement.setObject(7, task.date.isAfter(LocalDate.of(9999, 1, 1)) ? null : Date.valueOf(task.date), Types.DATE);
            statement.executeUpdate();
            Optional.ofNullable(task.tags).orElse(new ArrayList<>())
                    .forEach(tag -> InsertTag(task.id, tag));
            Optional.ofNullable(task.subtasks).orElse(new ArrayList<>())
                    .forEach(subTask -> InsertSubTask(task.id, subTask));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    synchronized void InsertTag(Integer id, String tag) {
        try (Connection conn = DBHandler.getConn()) {
            PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO tags(task_id, tag) VALUES (?,?);",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setInt(1, id);
            statement.setString(2, tag);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    synchronized void InsertSubTask(Integer id, SubTask subTask) {
        try (Connection conn = DBHandler.getConn()) {
            PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO subtasks(task_id, header, description, done, archived) VALUES (?,?,?,?,?);",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setInt(1, id);
            statement.setString(2, subTask.header);
            statement.setString(3, subTask.description);
            statement.setBoolean(4, subTask.done);
            statement.setBoolean(5, subTask.archived);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("fuck this id  " + id);
        }
    }

    synchronized void DeleteTask(Integer id) {
        try (Connection conn = DBHandler.getConn()) {
            PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM tasks WHERE id = ?;",
                    Statement.RETURN_GENERATED_KEYS
            );
            Task task = Info.get(id);
            statement.setInt(1, task.id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            //loadFile();
            //Info.forEach(this::InsertTask);
            Info = LoadTasks(sqlRequests.loadAllData);
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
        public HandleAClient(Socket socket) throws SocketException {
            this.socket = socket;
            socket.setSoTimeout(7500);
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
                            Info.add(task); // add in localStore
                            task.setId(Info.indexOf(task));
                            task.header += task.id;
                            InsertTask(task); // add in DB
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
                            DeleteTask(task.id);
                            InsertTask(task);
                            System.out.println("Task update: " + task.getId());
                            break;
                        }
                        case "REMOVE": { // Remove task from list
                            dataFromClient = new DataInputStream(socket.getInputStream());
                            int id = dataFromClient.readInt();
                            boolean ans = false;
                            if (id >= 0) {
                                try {
                                    ArchiveTask(id);
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
                        case "FILTER": {
                            int filter = dataFromClient.readInt();
                            String tag = dataFromClient.readUTF();
                            String desc = dataFromClient.readUTF();
                            CopyOnWriteArrayList<Task> temp = new CopyOnWriteArrayList<>();
                            switch (filter) {
                                case 1: {
                                    try (Connection conn = DBHandler.getConn()) {
                                        PreparedStatement statement = conn.prepareStatement(
                                                sqlRequests.load1,
                                                Statement.RETURN_GENERATED_KEYS
                                        );
                                        statement.setString(1, tag);
                                        statement.setString(2, '%' + desc + '%');
                                        ResultSet rs = statement.executeQuery();
                                        temp = readRS(rs);
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                                case 2: {
                                    temp = LoadTasks(sqlRequests.load2); // 50% subTask is done
                                    break;
                                }
                                case 3: {
                                    temp = LoadTasks(sqlRequests.load3); // used >= 1 of 3 popular tags
                                    break;
                                }
                                case 4: {
                                    try (Connection conn = DBHandler.getConn()) {
                                        PreparedStatement statement = conn.prepareStatement(
                                                sqlRequests.load4,
                                                Statement.RETURN_GENERATED_KEYS
                                        );
                                        statement.setString(1, tag);
                                        ResultSet rs = statement.executeQuery();
                                        temp = readRS(rs);
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                                case 5: {
                                    temp = LoadTasks(sqlRequests.load5);
                                    break;
                                }
                                case 6: {
                                    temp = LoadTasks(sqlRequests.load6);
                                    break;
                                }
                                case 7: {
                                    temp = LoadTasks(sqlRequests.load7);
                                    break;
                                }
                                case 8: {
                                    try (Connection conn = DBHandler.getConn()) {
                                        PreparedStatement statement = conn.prepareStatement(
                                                sqlRequests.load8,
                                                Statement.RETURN_GENERATED_KEYS
                                        );
                                        statement.setString(1, tag);
                                        ResultSet rs = statement.executeQuery();
                                        temp = readRS(rs);
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                                case 9: {
                                    temp = LoadTasks(sqlRequests.load9);
                                    break;
                                }
                                default:
                                    throw new ClassNotFoundException();
                            }
                            objectToClient = new ObjectOutputStream(socket.getOutputStream());
                            objectToClient.writeObject(temp);
                            break;
                        }
                        default: {
                            System.out.println("worng usr command");
                        }
                    }
                }
            } catch (SocketException ex) {
                System.out.println("Client " + socket.toString() + " disconnected");
                try {
                    socket.close();
                    socket = null;
                } catch (IOException | NullPointerException e) {

                }
            } catch (ClassNotFoundException | IOException | InterruptedException ex) {
            } finally {
                semaphore.release(); // Release a permit
                try {
                    socket.close();
                    socket = null;
                } catch (IOException | NullPointerException e) {

                }

            }
        }
    }
}