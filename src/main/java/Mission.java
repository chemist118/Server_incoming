import java.io.Serializable;

public abstract class Mission implements Serializable {
    String header, description;
    Boolean done;
    Boolean archived;

    public Mission(String header, String description, Boolean done, Boolean archived) {
        this.header = header;
        this.description = description;
        this.done = done;
        this.archived = archived;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        if (archived) return;
        this.header = header;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (archived) return;
        this.description = description;
    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        if (archived) return;
        this.done = done;
    }
}