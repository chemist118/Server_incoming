import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public interface TasksDAO {
    void Load(File file); // Загрузка сохранения
    void Save(File file); // Сохранение в файл
    CopyOnWriteArrayList<Task> getAll(); // Получить список всех задач
    Task getTask(int index); // Получить задачу по ндексу
    void Add(Task task); // Добавить задачу
    void Remove(Task task); // Удалить задачу
    void Update(Task oldTask, Task newTask); // Удалить задачу
    ArrayList<Task> filter(
            Boolean isNotEnded, // Выбраны незавершенные задачи
            Boolean isEnded, // Выбраны завершенные задачи
            Boolean isAll, // Выбраны все задачи
            Boolean FireTasks, // Выбраны просроченные задачи и задачи на ближайшую неделю
            String tags, // список тегов через запятую
            String Description, // строка в описании задачи
            int numOfSpecialFilter // код специального фильтра
    ); // Отфильтровать задачи
}
