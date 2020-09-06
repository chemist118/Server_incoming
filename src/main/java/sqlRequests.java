public class sqlRequests {
    // всё
    public static String loadAllData =
            "SELECT t.id,\n" +
                    "       t.Header,\n" +
                    "       t.Description,\n" +
                    "       t.Done,\n" +
                    "       t.Archived,\n" +
                    "       t.HaveDate,\n" +
                    "       t.Date,\n" +
                    "       s.Header,\n" +
                    "       s.Description,\n" +
                    "       s.Done,\n" +
                    "       tags.tag\n" +
                    "FROM tasks t\n" +
                    "         LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "         LEFT JOIN tags ON tags.Task_id = t.id\n" +
                    "ORDER BY id;";

    // на ближайший месяц с тегом и описанием
    public static String load1 =
            "SELECT t.id,\n" +
                    "       t.Header,\n" +
                    "       t.Description,\n" +
                    "       t.Done,\n" +
                    "       t.Archived,\n" +
                    "       t.HaveDate,\n" +
                    "       t.Date,\n" +
                    "       s.Header,\n" +
                    "       s.Description,\n" +
                    "       s.Done,\n" +
                    "       tags.tag\n" +
                    "FROM tasks t\n" +
                    "         LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "         LEFT JOIN tags ON t.id = tags.Task_id\n" +
                    "WHERE t.id IN (\n" +
                    "    SELECT t.id" +
                    "    FROM tasks t\n" +
                    "             LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "             LEFT JOIN tags ON t.id = tags.Task_id\n" +
                    "    WHERE t.archived = FALSE\n" +
                    "      AND tags.tag LIKE ?\n" +
                    "      AND t.description LIKE ?\n" +
                    "      AND date(t.date)\n" +
                    "        BETWEEN date(now()) AND date_add(date(now()), INTERVAL 1 MONTH)\n" +
                    ")\n" +
                    "ORDER BY t.id;";

    // половина подзадач завершена
    public static String load2 =
            "SELECT t.id,\n" +
                    "       t.Header,\n" +
                    "       t.Description,\n" +
                    "       t.Done,\n" +
                    "       t.Archived,\n" +
                    "       t.HaveDate,\n" +
                    "       t.Date,\n" +
                    "       s.Header,\n" +
                    "       s.Description,\n" +
                    "       s.Done,\n" +
                    "       tags.tag\n" +
                    "FROM tasks t\n" +
                    "         LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "         LEFT JOIN tags ON t.id = tags.Task_id\n" +
                    "WHERE t.id IN (\n" +
                    "    SELECT t.id\n" +
                    "    FROM tasks t\n" +
                    "             LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "             LEFT JOIN tags ON t.id = tags.Task_id\n" +
                    "    WHERE t.Archived = FALSE\n" +
                    "    GROUP BY s.Task_id\n" +
                    "    HAVING avg(if(s.Done IS NOT NULL, s.Done, 0)) >= 0.5\n" +
                    ")\n" +
                    "ORDER BY id;";

    // 3 популярных тега просроченных
    public static String load3 =
            "SELECT t.id,\n" +
                    "       t.Header,\n" +
                    "       t.Description,\n" +
                    "       t.Done,\n" +
                    "       t.Archived,\n" +
                    "       t.HaveDate,\n" +
                    "       t.Date,\n" +
                    "       s.Header,\n" +
                    "       s.Description,\n" +
                    "       s.Done,\n" +
                    "       tags.tag\n" +
                    "FROM tasks t\n" +
                    "         LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "         LEFT JOIN tags ON t.id = tags.Task_id\n" +
                    "WHERE t.id IN (\n" +
                    "    SELECT DISTINCT t.id\n" +
                    "    FROM tasks t\n" +
                    "             LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "             LEFT JOIN tags ON t.id = tags.Task_id\n" +
                    "    WHERE t.Archived = FALSE\n" +
                    "      AND date(t.date) BETWEEN date_sub(date(now()), INTERVAL 999 YEAR) AND date_sub(date(now()), INTERVAL 1 DAY)\n" +
                    "      AND tags.tag IN (\n" +
                    "        SELECT tag\n" +
                    "        FROM (\n" +
                    "                 SELECT tag, count(tag) AS count\n" +
                    "                 FROM tags\n" +
                    "                 GROUP BY tag\n" +
                    "                 ORDER BY count DESC\n" +
                    "                 LIMIT 3\n" +
                    "             ) AS sas)\n" +
                    ")\n" +
                    "ORDER BY id;";

    // 3 с ближайшим крайним сроком и тэгом
    public static String load4 =
            "SELECT t.id,\n" +
                    "       t.Header,\n" +
                    "       t.Description,\n" +
                    "       t.Done,\n" +
                    "       t.Archived,\n" +
                    "       t.HaveDate,\n" +
                    "       t.Date,\n" +
                    "       s.Header,\n" +
                    "       s.Description,\n" +
                    "       s.Done,\n" +
                    "       tags.tag\n" +
                    "FROM tasks t\n" +
                    "         LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "         JOIN tags ON tags.Task_id = t.id\n" +
                    "WHERE t.id IN (\n" +
                    "    SELECT t.id\n" +
                    "    FROM tasks t\n" +
                    "             LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "             JOIN tags ON tags.Task_id = t.id\n" +
                    "    WHERE t.Date IS NOT NULL\n" +
                    "      AND date(t.date) BETWEEN date(now()) AND date_add(date(now()), INTERVAL 999 YEAR)\n" +
                    "      AND tags.tag LIKE ?\n" +
                    "      AND t.Archived = FALSE\n" +
                    "    ORDER BY t.Date\n" +
                    ")\n" +
                    "ORDER BY t.Date" +
                    "    LIMIT 3;";

    // 4 задачи с отдаленным крайним сроком без тэгов
    public static String load5 =
            "SELECT t.id,\n" +
                    "       t.Header,\n" +
                    "       t.Description,\n" +
                    "       t.Done,\n" +
                    "       t.Archived,\n" +
                    "       t.HaveDate,\n" +
                    "       t.Date,\n" +
                    "       s.Header,\n" +
                    "       s.Description,\n" +
                    "       s.Done,\n" +
                    "       tags.tag\n" +
                    "FROM tasks t\n" +
                    "         LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "         LEFT JOIN tags ON t.id = tags.Task_id\n" +
                    "WHERE t.archived = FALSE\n" +
                    "  AND t.date IS NOT NULL\n" +
                    "      AND date(t.date) BETWEEN date(now()) AND date_add(date(now()), INTERVAL 999 YEAR)\n" +
                    "  AND tags.tag IS NULL\n" +
                    "ORDER BY t.date DESC\n" +
                    "LIMIT 4;";

    // незавершённых задач, отсортированных по названию по возрастанию
    public static String load6 =
            "SELECT t.id,\n" +
                    "       t.Header,\n" +
                    "       t.Description,\n" +
                    "       t.Done,\n" +
                    "       t.Archived,\n" +
                    "       t.HaveDate,\n" +
                    "       t.Date,\n" +
                    "       s.Header,\n" +
                    "       s.Description,\n" +
                    "       s.Done,\n" +
                    "       tags.tag\n" +
                    "FROM tasks t\n" +
                    "         LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "         LEFT JOIN tags ON t.id = tags.Task_id\n" +
                    "WHERE t.Archived = FALSE\n" +
                    "  AND t.Done = FALSE\n" +
                    "ORDER BY t.Header;";

    // незавершённых задач, отсортированных по дате и названию по возрастанию просроченные или близ неделя
    public static String load7 =
            "SELECT t.id,\n" +
                    "       t.Header,\n" +
                    "       t.Description,\n" +
                    "       t.Done,\n" +
                    "       t.Archived,\n" +
                    "       t.HaveDate,\n" +
                    "       t.Date,\n" +
                    "       s.Header,\n" +
                    "       s.Description,\n" +
                    "       s.Done,\n" +
                    "       tags.tag\n" +
                    "FROM tasks t\n" +
                    "         LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "         LEFT JOIN tags ON t.id = tags.Task_id\n" +
                    "WHERE t.Archived = FALSE\n" +
                    "  AND t.Done = FALSE\n" +
                    "  AND date(t.date)\n" +
                    "    BETWEEN date_sub(date(now()), INTERVAL 999 YEAR) AND date_add(date(now()), INTERVAL 1 WEEK)\n" +
                    "ORDER BY t.Date, t.Header;";

    // незавершённых задач, отсортированных по названию по возрастанию + tag
    public static String load8 =
            "SELECT t.id,\n" +
                    "       t.Header,\n" +
                    "       t.Description,\n" +
                    "       t.Done,\n" +
                    "       t.Archived,\n" +
                    "       t.HaveDate,\n" +
                    "       t.Date,\n" +
                    "       s.Header,\n" +
                    "       s.Description,\n" +
                    "       s.Done,\n" +
                    "       tags.tag\n" +
                    "FROM tasks t\n" +
                    "         LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "         LEFT JOIN tags ON t.id = tags.Task_id\n" +
                    "WHERE t.id IN (\n" +
                    "    SELECT t.id\n" +
                    "    FROM tasks t\n" +
                    "             LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "             LEFT JOIN tags ON t.id = tags.Task_id\n" +
                    "    WHERE t.Archived = FALSE\n" +
                    "      AND t.Done = FALSE\n" +
                    "      AND tag LIKE ?\n" +
                    ")\n" +
                    "ORDER BY t.Header;";

    // завершённых задач, отсортированных по названию по возрастанию.
    public static String load9 =
            "SELECT t.id,\n" +
                    "       t.Header,\n" +
                    "       t.Description,\n" +
                    "       t.Done,\n" +
                    "       t.Archived,\n" +
                    "       t.HaveDate,\n" +
                    "       t.Date,\n" +
                    "       s.Header,\n" +
                    "       s.Description,\n" +
                    "       s.Done,\n" +
                    "       tags.tag\n" +
                    "FROM tasks t\n" +
                    "         LEFT JOIN subtasks s ON t.id = s.Task_id\n" +
                    "         LEFT JOIN tags ON t.id = tags.Task_id\n" +
                    "WHERE t.Archived = FALSE\n" +
                    "  AND t.Done = TRUE\n" +
                    "ORDER BY t.Header;";
}
