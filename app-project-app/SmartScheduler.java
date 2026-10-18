import java.util.*;

public class SmartScheduler {

    public static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

    // 7 Periods (matching university timetable)
    public static final String[][] PERIODS = {
        {"08:00", "08:50"}, // Period 1 (index 0)
        {"08:50", "09:40"}, // Period 2 (index 1)
        {"09:40", "10:30"}, // Period 3 (index 2)
        // 10:30 - 10:50 Morning Break
        {"10:50", "11:40"}, // Period 4 (index 3)
        {"11:40", "12:30"}, // Period 5 (index 4)
        // 12:30 - 13:15 Lunch Break
        {"13:15", "14:00"}, // Period 6 (index 5)
        {"14:00", "14:45"}  // Period 7 (index 6)
    };

    /**
     * Generates an intelligent, balanced timetable for 7 periods.
     * Handles:
     * 1. 2-period consecutive Lab sessions in Lab slots (P4-P5 or P6-P7).
     * 2. Balanced theory distribution across weekdays.
     * 3. Prevents room collisions and instructor clashes.
     * 4. Respects course weekly credit hours.
     */
    public static List<TimetableSlot> generateTimetable(List<Subject> subjects) {
        List<TimetableSlot> slots = new ArrayList<>();
        if (subjects == null || subjects.isEmpty()) {
            return slots;
        }

        // Separate Labs and Theory
        List<Subject> labSubjects = new ArrayList<>();
        List<Subject> theorySubjects = new ArrayList<>();

        for (Subject s : subjects) {
            if (s.getName().toLowerCase().contains("lab") || s.getCode().toLowerCase().contains("lab")) {
                labSubjects.add(s);
            } else {
                theorySubjects.add(s);
            }
        }

        // Track remaining hours
        Map<Subject, Integer> remainingHours = new LinkedHashMap<>();
        for (Subject s : subjects) {
            remainingHours.put(s, s.getWeeklyHours());
        }

        // Track grid occupation: grid[dayIndex][periodIndex] = TimetableSlot
        TimetableSlot[][] grid = new TimetableSlot[DAYS.length][PERIODS.length];
        int slotCounter = 1;

        // 1. Schedule LABS first in 2-period consecutive blocks (Periods 4-5 or 6-7)
        // Possible lab slots: Day 0 P4-P5, Day 1 P6-P7, Day 3 P4-P5, Day 4 P4-P5
        int[][] labBlocks = {
            {0, 3, 4}, // Monday Periods 4 & 5
            {1, 5, 6}, // Tuesday Periods 6 & 7
            {3, 3, 4}, // Thursday Periods 4 & 5
            {4, 3, 4}  // Friday Periods 4 & 5
        };

        int blockIdx = 0;
        for (Subject lab : labSubjects) {
            int needed = remainingHours.getOrDefault(lab, 0);
            while (needed >= 2 && blockIdx < labBlocks.length) {
                int day = labBlocks[blockIdx][0];
                int p1 = labBlocks[blockIdx][1];
                int p2 = labBlocks[blockIdx][2];

                if (grid[day][p1] == null && grid[day][p2] == null) {
                    TimetableSlot s1 = new TimetableSlot(
                        "slot-" + (slotCounter++), DAYS[day],
                        PERIODS[p1][0], PERIODS[p1][1],
                        lab.getName(), lab.getCode(), lab.getTeacher(), lab.getRoom(), lab.getColor()
                    );
                    TimetableSlot s2 = new TimetableSlot(
                        "slot-" + (slotCounter++), DAYS[day],
                        PERIODS[p2][0], PERIODS[p2][1],
                        lab.getName(), lab.getCode(), lab.getTeacher(), lab.getRoom(), lab.getColor()
                    );
                    grid[day][p1] = s1;
                    grid[day][p2] = s2;
                    slots.add(s1);
                    slots.add(s2);

                    needed -= 2;
                    remainingHours.put(lab, needed);
                    blockIdx++;
                } else {
                    blockIdx++;
                }
            }
        }

        // 2. Schedule THEORY subjects across remaining open slots
        // Track daily count per theory subject to avoid crowding
        Map<String, Map<String, Integer>> daySubjectCount = new HashMap<>();
        for (String day : DAYS) {
            daySubjectCount.put(day, new HashMap<>());
        }

        // Fill remaining slots period by period, day by day
        for (int p = 0; p < PERIODS.length; p++) {
            for (int d = 0; d < DAYS.length; d++) {
                if (grid[d][p] != null) continue; // Already occupied (by lab)

                // Period 7 is often free/light on Mon, Wed, Thu, Fri (as in the timetable photo)
                if (p == 6 && d != 1 && countRemainingTheory(remainingHours, theorySubjects) == 0) {
                    continue;
                }

                String dayName = DAYS[d];
                Subject chosen = selectBestTheory(theorySubjects, remainingHours, dayName, p, grid, daySubjectCount);

                if (chosen != null) {
                    TimetableSlot slot = new TimetableSlot(
                        "slot-" + (slotCounter++), dayName,
                        PERIODS[p][0], PERIODS[p][1],
                        chosen.getName(), chosen.getCode(), chosen.getTeacher(), chosen.getRoom(), chosen.getColor()
                    );
                    grid[d][p] = slot;
                    slots.add(slot);

                    // Update remaining hours & day count
                    remainingHours.put(chosen, remainingHours.get(chosen) - 1);
                    Map<String, Integer> sc = daySubjectCount.get(dayName);
                    sc.put(chosen.getId(), sc.getOrDefault(chosen.getId(), 0) + 1);
                }
            }
        }

        return slots;
    }

    private static int countRemainingTheory(Map<Subject, Integer> remainingHours, List<Subject> theorySubjects) {
        int total = 0;
        for (Subject s : theorySubjects) {
            total += remainingHours.getOrDefault(s, 0);
        }
        return total;
    }

    private static Subject selectBestTheory(
        List<Subject> theorySubjects,
        Map<Subject, Integer> remainingHours,
        String day,
        int periodIndex,
        TimetableSlot[][] grid,
        Map<String, Map<String, Integer>> daySubjectCount
    ) {
        Subject selected = null;
        int maxRemaining = 0;
        int dayIndex = Arrays.asList(DAYS).indexOf(day);

        // Previous period subject in same day (to prevent back-to-back same subjects)
        String prevSubjectId = null;
        if (periodIndex > 0 && grid[dayIndex][periodIndex - 1] != null) {
            prevSubjectId = grid[dayIndex][periodIndex - 1].getSubjectCode();
        }

        for (Subject candidate : theorySubjects) {
            int remaining = remainingHours.getOrDefault(candidate, 0);
            if (remaining <= 0) continue;

            int countToday = daySubjectCount.get(day).getOrDefault(candidate.getId(), 0);
            // Max 1 period per day if hours <= 4, max 2 if hours > 4
            if (countToday >= 2) continue;
            if (countToday >= 1 && remaining < 4) continue;

            // Avoid back-to-back same subject
            if (candidate.getCode().equalsIgnoreCase(prevSubjectId)) {
                continue;
            }

            // Priority: highest remaining hours
            if (selected == null || remaining > maxRemaining ||
                (remaining == maxRemaining && countToday < daySubjectCount.get(day).getOrDefault(selected.getId(), 0))) {
                selected = candidate;
                maxRemaining = remaining;
            }
        }

        // Fallback: if strictly no candidate found without constraints, pick any with remaining > 0
        if (selected == null) {
            for (Subject candidate : theorySubjects) {
                int remaining = remainingHours.getOrDefault(candidate, 0);
                if (remaining > 0) {
                    return candidate;
                }
            }
        }

        return selected;
    }
}
