import java.util.*;

/**
 * TimetableManager - State manager for 7-period schedule, courses, and conflict checks.
 * Pre-seeded with SRM CSE II-Year timetable curriculum.
 */
public class TimetableManager {
    private final List<Subject> subjects = Collections.synchronizedList(new ArrayList<>());
    private final List<TimetableSlot> slots = Collections.synchronizedList(new ArrayList<>());

    public TimetableManager() {
        seedDefaultSubjects();
        seedDefaultSchedule(); // Pre-load official 7-period schedule
    }

    private void seedDefaultSubjects() {
        subjects.add(new Subject("sub-1", "Transforms & Boundary Value Problems", "TBVP", "Dr. S. Thalapathiraj", "BW106", 4, "#3b82f6"));
        subjects.add(new Subject("sub-2", "Design Thinking & Methodology", "DTM", "Ms. R. Saranya", "BW106", 3, "#10b981"));
        subjects.add(new Subject("sub-3", "Computer Organization & Architecture", "COA", "Ms. M. Indumathy", "BW106", 4, "#8b5cf6"));
        subjects.add(new Subject("sub-4", "Data Structures & Algorithms", "DS", "Dr. K. Akila", "BW106", 4, "#f59e0b"));
        subjects.add(new Subject("sub-5", "Operating Systems", "OS", "Dr. S. Sridevi", "BW106", 4, "#ef4444"));
        subjects.add(new Subject("sub-6", "Advanced Programming Practice", "APP", "Mr. D. Illakkiaselvan", "BW106", 3, "#06b6d4"));
        subjects.add(new Subject("sub-7", "Professional Ethics", "PE", "Mr. Suhail Habeeb A", "BW106", 1, "#84cc16"));
        subjects.add(new Subject("sub-8", "Critical Reasoning & Workplace Comm.", "CRWC", "Ms. S. Harshini", "BW106", 1, "#ec4899"));
        subjects.add(new Subject("sub-9", "Library", "LIB", "Dr. G. Paavai Anand", "Library", 1, "#64748b"));
        subjects.add(new Subject("sub-10", "APP Lab", "APP LAB", "Mr. D. Illakkiaselvan", "Computing Lab", 2, "#0ea5e9"));
        subjects.add(new Subject("sub-11", "DS / OS Lab", "DS/OS LAB", "Dr. K. Akila / Dr. Sridevi", "Computing Lab", 4, "#d97706"));
        subjects.add(new Subject("sub-12", "DTM Lab", "DTM LAB", "Ms. R. Saranya", "Design Lab", 2, "#059669"));
    }

    private void seedDefaultSchedule() {
        // Monday (Day 1)
        slots.add(new TimetableSlot("s1", "Monday", "08:00", "08:50", "Data Structures", "DS", "Dr. K. Akila", "BW106", "#f59e0b"));
        slots.add(new TimetableSlot("s2", "Monday", "08:50", "09:40", "Design Thinking", "DTM", "Ms. R. Saranya", "BW106", "#10b981"));
        slots.add(new TimetableSlot("s3", "Monday", "09:40", "10:30", "Computer Org & Arch", "COA", "Ms. M. Indumathy", "BW106", "#8b5cf6"));
        slots.add(new TimetableSlot("s4", "Monday", "10:50", "11:40", "APP Lab", "APP LAB", "Mr. D. Illakkiaselvan", "Computing Lab", "#0ea5e9"));
        slots.add(new TimetableSlot("s5", "Monday", "11:40", "12:30", "APP Lab", "APP LAB", "Mr. D. Illakkiaselvan", "Computing Lab", "#0ea5e9"));
        slots.add(new TimetableSlot("s6", "Monday", "13:15", "14:00", "Data Structures", "DS", "Dr. K. Akila", "BW106", "#f59e0b"));

        // Tuesday (Day 2)
        slots.add(new TimetableSlot("s7", "Tuesday", "08:00", "08:50", "Operating Systems", "OS", "Dr. S. Sridevi", "BW106", "#ef4444"));
        slots.add(new TimetableSlot("s8", "Tuesday", "08:50", "09:40", "Critical Reasoning", "CRWC", "Ms. S. Harshini", "BW106", "#ec4899"));
        slots.add(new TimetableSlot("s9", "Tuesday", "09:40", "10:30", "Professional Ethics", "PE", "Mr. Suhail Habeeb A", "BW106", "#84cc16"));
        slots.add(new TimetableSlot("s10", "Tuesday", "10:50", "11:40", "Transforms & Boundary", "TBVP", "Dr. S. Thalapathiraj", "BW106", "#3b82f6"));
        slots.add(new TimetableSlot("s11", "Tuesday", "11:40", "12:30", "Adv Programming Practice", "APP", "Mr. D. Illakkiaselvan", "BW106", "#06b6d4"));
        slots.add(new TimetableSlot("s12", "Tuesday", "13:15", "14:00", "DS / OS Lab", "DS/OS LAB", "Dr. K. Akila", "Computing Lab", "#d97706"));
        slots.add(new TimetableSlot("s13", "Tuesday", "14:00", "14:45", "DS / OS Lab", "DS/OS LAB", "Dr. S. Sridevi", "Computing Lab", "#d97706"));

        // Wednesday (Day 3)
        slots.add(new TimetableSlot("s14", "Wednesday", "08:00", "08:50", "Transforms & Boundary", "TBVP", "Dr. S. Thalapathiraj", "BW106", "#3b82f6"));
        slots.add(new TimetableSlot("s15", "Wednesday", "08:50", "09:40", "Computer Org & Arch", "COA", "Ms. M. Indumathy", "BW106", "#8b5cf6"));
        slots.add(new TimetableSlot("s16", "Wednesday", "09:40", "10:30", "Design Thinking", "DTM", "Ms. R. Saranya", "BW106", "#10b981"));
        slots.add(new TimetableSlot("s17", "Wednesday", "10:50", "11:40", "Data Structures", "DS", "Dr. K. Akila", "BW106", "#f59e0b"));
        slots.add(new TimetableSlot("s18", "Wednesday", "11:40", "12:30", "Operating Systems", "OS", "Dr. S. Sridevi", "BW106", "#ef4444"));
        slots.add(new TimetableSlot("s19", "Wednesday", "13:15", "14:00", "Data Structures", "DS", "Dr. K. Akila", "BW106", "#f59e0b"));

        // Thursday (Day 4)
        slots.add(new TimetableSlot("s20", "Thursday", "08:00", "08:50", "Adv Programming Practice", "APP", "Mr. D. Illakkiaselvan", "BW106", "#06b6d4"));
        slots.add(new TimetableSlot("s21", "Thursday", "08:50", "09:40", "Transforms & Boundary", "TBVP", "Dr. S. Thalapathiraj", "BW106", "#3b82f6"));
        slots.add(new TimetableSlot("s22", "Thursday", "09:40", "10:30", "Operating Systems", "OS", "Dr. S. Sridevi", "BW106", "#ef4444"));
        slots.add(new TimetableSlot("s23", "Thursday", "10:50", "11:40", "DS / OS Lab", "DS/OS LAB", "Dr. K. Akila", "Computing Lab", "#d97706"));
        slots.add(new TimetableSlot("s24", "Thursday", "11:40", "12:30", "DS / OS Lab", "DS/OS LAB", "Dr. S. Sridevi", "Computing Lab", "#d97706"));
        slots.add(new TimetableSlot("s25", "Thursday", "13:15", "14:00", "Computer Org & Arch", "COA", "Ms. M. Indumathy", "BW106", "#8b5cf6"));

        // Friday (Day 5)
        slots.add(new TimetableSlot("s26", "Friday", "08:00", "08:50", "Computer Org & Arch", "COA", "Ms. M. Indumathy", "BW106", "#8b5cf6"));
        slots.add(new TimetableSlot("s27", "Friday", "08:50", "09:40", "Operating Systems", "OS", "Dr. S. Sridevi", "BW106", "#ef4444"));
        slots.add(new TimetableSlot("s28", "Friday", "09:40", "10:30", "Transforms & Boundary", "TBVP", "Dr. S. Thalapathiraj", "BW106", "#3b82f6"));
        slots.add(new TimetableSlot("s29", "Friday", "10:50", "11:40", "DTM Lab", "DTM LAB", "Ms. R. Saranya", "Design Lab", "#059669"));
        slots.add(new TimetableSlot("s30", "Friday", "11:40", "12:30", "DTM Lab", "DTM LAB", "Ms. R. Saranya", "Design Lab", "#059669"));
        slots.add(new TimetableSlot("s31", "Friday", "13:15", "14:00", "Library", "LIB", "Dr. G. Paavai Anand", "Library", "#64748b"));
    }

    public synchronized void generateSmartTimetable() {
        slots.clear();
        slots.addAll(SmartScheduler.generateTimetable(subjects));
    }

    public synchronized List<Subject> getSubjects() {
        return new ArrayList<>(subjects);
    }

    public synchronized void addSubject(Subject subject) {
        subjects.add(subject);
    }

    public synchronized boolean deleteSubject(String id) {
        return subjects.removeIf(s -> s.getId().equalsIgnoreCase(id));
    }

    public synchronized List<TimetableSlot> getSlots() {
        return new ArrayList<>(slots);
    }

    public synchronized void addSlot(TimetableSlot slot) {
        slots.add(slot);
    }

    public synchronized boolean deleteSlot(String id) {
        return slots.removeIf(s -> s.getId().equalsIgnoreCase(id));
    }

    public synchronized void clearSlots() {
        slots.clear();
    }

    public synchronized List<String> detectConflicts() {
        List<String> conflicts = new ArrayList<>();

        for (int i = 0; i < slots.size(); i++) {
            TimetableSlot a = slots.get(i);
            for (int j = i + 1; j < slots.size(); j++) {
                TimetableSlot b = slots.get(j);

                if (a.overlapsWith(b)) {
                    if (a.getTeacher().equalsIgnoreCase(b.getTeacher())) {
                        conflicts.add(String.format(
                            "Teacher Clashing: '%s' is scheduled twice on %s (%s vs %s).",
                            a.getTeacher(), a.getDay(), a.getSubjectName(), b.getSubjectName()
                        ));
                    }
                    if (a.getRoom().equalsIgnoreCase(b.getRoom())) {
                        conflicts.add(String.format(
                            "Room Collision: '%s' double-booked on %s (%s and %s).",
                            a.getRoom(), a.getDay(), a.getSubjectName(), b.getSubjectName()
                        ));
                    }
                }
            }
        }

        return conflicts;
    }

    public synchronized String getSubjectsJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < subjects.size(); i++) {
            sb.append(subjects.get(i).toJson());
            if (i < subjects.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public synchronized String getSlotsJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < slots.size(); i++) {
            sb.append(slots.get(i).toJson());
            if (i < slots.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
