/**
 * Represents a single scheduled period/slot in the timetable.
 */
public class TimetableSlot {
    private String id;
    private String day;         // e.g. "Monday", "Tuesday"
    private String startTime;   // e.g. "09:00"
    private String endTime;     // e.g. "10:00"
    private String subjectName;
    private String subjectCode;
    private String teacher;
    private String room;
    private String color;

    public TimetableSlot(String id, String day, String startTime, String endTime,
                         String subjectName, String subjectCode, String teacher,
                         String room, String color) {
        this.id = id;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.teacher = teacher;
        this.room = room;
        this.color = color;
    }

    public String getId() { return id; }
    public String getDay() { return day; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getSubjectName() { return subjectName; }
    public String getSubjectCode() { return subjectCode; }
    public String getTeacher() { return teacher; }
    public String getRoom() { return room; }
    public String getColor() { return color; }

    /**
     * Checks if this slot overlaps in time with another slot on the same day.
     */
    public boolean overlapsWith(TimetableSlot other) {
        if (!this.day.equalsIgnoreCase(other.day)) {
            return false;
        }
        int startA = toMinutes(this.startTime);
        int endA = toMinutes(this.endTime);
        int startB = toMinutes(other.startTime);
        int endB = toMinutes(other.endTime);

        return Math.max(startA, startB) < Math.min(endA, endB);
    }

    private static int toMinutes(String time) {
        try {
            String[] parts = time.split(":");
            int hours = Integer.parseInt(parts[0].trim());
            int mins = Integer.parseInt(parts[1].trim());
            return hours * 60 + mins;
        } catch (Exception e) {
            return 0;
        }
    }

    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"day\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\"," +
            "\"subjectName\":\"%s\",\"subjectCode\":\"%s\",\"teacher\":\"%s\",\"room\":\"%s\",\"color\":\"%s\"}",
            escape(id), escape(day), escape(startTime), escape(endTime),
            escape(subjectName), escape(subjectCode), escape(teacher), escape(room), escape(color)
        );
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
