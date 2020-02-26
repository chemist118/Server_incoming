import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.util.Callback;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

public class Task extends Mission implements Serializable {
    LocalDate date;
    ArrayList<SubTask> subtasks;
    ArrayList<String> tags;
    String listOfTags = "";
    Boolean haveDate;
    Integer id = -1;

    public Boolean getHaveDate() {
        return haveDate;
    }

    public void setHaveDate(Boolean haveDate) {
        if (archived) return;
        this.haveDate = haveDate;
        date = haveDate ? LocalDate.now() : LocalDate.MAX; // неФП
    }

    public Task(Integer id, String header, String description, ArrayList<SubTask> subtasks,
                ArrayList<String> tags, Boolean done, Boolean archived, Boolean haveDate, LocalDate date) {
        super(header, description, done, archived);
        this.subtasks = new ArrayList<>(Optional.ofNullable(subtasks).orElse(new ArrayList<>()));
        this.date = date;
        this.tags = new ArrayList<>(Optional.ofNullable(tags).orElse(new ArrayList<>()));
        this.tags.forEach(s -> this.listOfTags += s + ","); //  ФП
        this.haveDate = haveDate;
        this.id = id;
    }

    public Task(Task task) {
        super(task.getHeader(), task.getDescription(), task.getDone(), task.getArchived());
        this.subtasks = new ArrayList<>();
        task.subtasks = Optional.ofNullable(task.subtasks).orElse(new ArrayList<>());
        task.subtasks.forEach(s -> {
            this.subtasks.add(new SubTask(s));
        });
        this.date = task.getDate().getValue();
        this.tags = new ArrayList<>(Optional.ofNullable(task.tags).orElse(new ArrayList<>()));  // ФП
        this.tags.forEach(s -> this.listOfTags += s + ","); //  ФП
        this.haveDate = task.getHaveDate();
        this.id = task.id;
    }

    public String getListOfTags() {
        return listOfTags;
    }

    public void setListOfTags(String listOfTags) {
        if (archived) return;
        tags.clear();
        Stream.of(listOfTags.split(","))  // ФП
                .filter(x -> isUnicAndNotEmptyTag(x.trim()))
                .forEach(x -> tags.add(x.trim()));
        this.listOfTags = "";
        this.tags.forEach(tag -> this.listOfTags += tag + ","); // ФП
    }

    public String getId() {
        return id.toString();
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DatePicker getDate() {
        DatePicker picker = new DatePicker(date);
        restrictDatePicker(picker, LocalDate.now(), LocalDate.MAX);  // установка ограничения даты
        picker.setEditable(false);
        picker.setOnAction(actionEvent -> {
            picker.setValue(this.date =
                    haveDate ? Optional.ofNullable(picker.getValue()).orElse(this.date) : LocalDate.MAX); // ФП неФП
        });
        return picker;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public void setDone(Boolean done) {
        if (archived) return;
        super.setDone(done);
        if (done)  //  неФП
            subtasks.forEach(x -> x.setDone(true));  // ФП
    }

    @Override
    public void setArchived(Boolean archived) {
        super.setArchived(archived);
        if (archived)  //  неФП
            subtasks.forEach(x -> x.setArchived(true));  // ФП
    }

    private boolean isUnicAndNotEmptyTag(String tag) {
        return tags.stream().noneMatch(x -> x.equalsIgnoreCase(tag)) && !tag.isEmpty();  // ФП
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Task other = (Task) obj;
        if (!this.header.equals(other.header))
            return false;
        if (!this.description.equals(other.description))
            return false;
        if (!this.done.equals(other.done))
            return false;
        if (!this.archived.equals(other.archived))
            return false;
        if (!this.date.equals(other.date))
            return false;
        if (!this.listOfTags.equals(other.listOfTags))
            return false;
        if (!this.haveDate.equals(other.haveDate))
            return false;
        if (!this.id.equals(other.id))
            return false;
        if (!this.subtasks.equals(other.subtasks))
            return false;
        return true;
    }

    private void restrictDatePicker(DatePicker datePicker, LocalDate minDate, LocalDate maxDate) {
        final Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(minDate)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        } else if (item.isAfter(maxDate)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        };
        datePicker.setDayCellFactory(dayCellFactory);
    }
}