import java.io.Serializable;

public class SubTask extends Mission implements Serializable {

    public SubTask(String header, String description, Boolean done, Boolean archived) {
        super(header, description, done, archived);
    }
}
