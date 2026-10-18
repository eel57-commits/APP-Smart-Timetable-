/**
 * Represents an academic subject or course in the Smart Timetable Maker.
 */
public class Subject {
    private String id;
    private String name;
    private String code;
    private String teacher;
    private String room;
    private int weeklyHours;
    private String color;

    public Subject(String id, String name, String code, String teacher, String room, int weeklyHours, String color) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.teacher = teacher;
        this.room = room;
        this.weeklyHours = weeklyHours;
        this.color = color;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getTeacher() { return teacher; }
    public String getRoom() { return room; }
    public int getWeeklyHours() { return weeklyHours; }
    public String getColor() { return color; }

    public void setWeeklyHours(int weeklyHours) { this.weeklyHours = weeklyHours; }

    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"code\":\"%s\",\"teacher\":\"%s\",\"room\":\"%s\",\"weeklyHours\":%d,\"color\":\"%s\"}",
            escape(id), escape(name), escape(code), escape(teacher), escape(room), weeklyHours, escape(color)
        );
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
