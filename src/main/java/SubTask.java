import java.io.Serializable;

public class SubTask extends Mission implements Serializable {

    public SubTask(String header, String description, Boolean done, Boolean archived) {
        super(header, description, done, archived);
    }

    public SubTask(SubTask sub) {
        super(sub.getHeader(), sub.getDescription(), sub.getDone(), sub.getArchived());
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SubTask other = (SubTask) obj;
        if (!this.header.equals(other.header))
            return false;
        if (!this.description.equals(other.description))
            return false;
        if (!this.done.equals(other.done))
            return false;
        if (!this.archived.equals(other.archived))
            return false;
        return true;
    }
}
