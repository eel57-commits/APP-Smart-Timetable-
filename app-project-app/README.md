# 🗓️ Smart Timetable Maker

An intelligent, constraint-based timetable and schedule generator designed with a **pure Java backend** (compatible with BlueJ, standard JDK 8+, and zero external Maven/Gradle dependencies) and a **modern HTML/CSS/JavaScript web dashboard**.

---

## 🌟 Key Features

1. **Intelligent Constraint-Based Auto-Scheduler**:
   - **Credit Hours Satisfaction**: Balances each course's required weekly hours evenly throughout the week.
   - **Clash Prevention**: Prevents teacher overlaps and room double-bookings.
   - **Fatigue Protection**: Avoids scheduling more than 2 consecutive hours of the same subject on the same day.
   - **Protected Universal Lunch Break**: 12:00 PM – 1:00 PM is automatically preserved across all days.
2. **Conflict Detection Engine**:
   - Live scanning for room overlaps, teacher double-bookings, or time clashes.
   - Real-time status pill (`✅ 0 Conflicts` vs `⚠️ Conflicts Detected`) with descriptive diagnostic alerts.
3. **Interactive Timetable Grid**:
   - Weekly grid (Monday to Friday, Periods 1 through 6).
   - Visual color-coded subject cards with teacher name, room, and instant deletion controls.
   - Dedicated lunch break banner.
4. **Course & Subject Manager**:
   - Add/Remove courses, configure weekly hours, assign instructors, specify default lecture halls, and pick custom colors.
5. **Print & Export Ready**:
   - Clean `@media print` styling ready to export directly to PDF or paper with one click.
6. **Dark & Light Mode**:
   - Integrated theme switcher with persistent local storage.

---

## 📁 Project Architecture & BlueJ Structure

```text
app-project-app/
├── AppServer.java         # Standalone HTTP Server (com.sun.net.httpserver) & REST API routes
├── TimetableManager.java   # Central state manager for subjects, slots & conflict detection
├── SmartScheduler.java     # Constraint-satisfaction heuristic timetable generation algorithm
├── Subject.java            # Course data model (name, code, teacher, room, weekly hours, color)
├── TimetableSlot.java      # Schedule period model with time-interval collision math
├── package.bluej           # Visual BlueJ project definition with UML dependency arrows
├── run.bat                 # One-click Windows startup script
└── web/                    # Modern Web Frontend
    ├── index.html          # Timetable calendar layout, action toolbar & modals
    ├── style.css           # Styling, themes, responsive layout, print formatting
    └── app.js              # REST client, dynamic grid rendering, modals & state sync
```

---

## 🚀 Running the Project

### Method 1: Using BlueJ
1. Start **BlueJ**.
2. Click **Project** $\rightarrow$ **Open Project...** (or Open Non-BlueJ).
3. Select the folder:
   `C:\Users\neel5\.gemini\antigravity\scratch\app-project-app`
4. BlueJ will load all classes (`AppServer`, `TimetableManager`, `SmartScheduler`, `Subject`, `TimetableSlot`) onto its visual canvas.
5. **Right-click** on the [`AppServer`](file:///C:/Users/neel5/.gemini/antigravity/scratch/app-project-app/AppServer.java) class box and select `void main(String[] args)`.
6. Open your browser and visit: **[http://localhost:8080](http://localhost:8080)**.

---

### Method 2: Command Line or Windows `run.bat`
Double-click `run.bat` or open PowerShell inside the folder:

```bash
javac AppServer.java TimetableManager.java SmartScheduler.java Subject.java TimetableSlot.java
java AppServer
```

Then visit **[http://localhost:8080](http://localhost:8080)** in any browser.

---

## 📡 REST API Reference

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/timetable` | `GET` | Fetches all scheduled slots, conflict count, and diagnostics |
| `/api/timetable/generate` | `POST` | Triggers the smart algorithm to auto-populate a balanced timetable |
| `/api/timetable/clear` | `POST` | Clears all scheduled classes |
| `/api/timetable/slot` | `POST` / `DELETE` | Adds a manual class period or removes an existing one |
| `/api/subjects` | `GET` / `POST` / `DELETE` | Lists, creates, or removes course subjects |
| `/api/status` | `GET` | Health check, uptime, and server statistics |
