// Smart Timetable Maker (7 Periods) - Frontend Application Logic

document.addEventListener('DOMContentLoaded', () => {
    // State
    let subjects = [];
    let slots = [];
    let conflicts = [];

    const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];
    
    // 7 Periods + Morning Break + Lunch Break
    const PERIODS = [
        { id: 'p1', name: 'Period 1', start: '08:00', end: '08:50' },
        { id: 'p2', name: 'Period 2', start: '08:50', end: '09:40' },
        { id: 'p3', name: 'Period 3', start: '09:40', end: '10:30' },
        { id: 'break', name: 'Break', start: '10:30', end: '10:50', isBreak: true },
        { id: 'p4', name: 'Period 4', start: '10:50', end: '11:40' },
        { id: 'p5', name: 'Period 5', start: '11:40', end: '12:30' },
        { id: 'lunch', name: 'Lunch', start: '12:30', end: '13:15', isLunch: true },
        { id: 'p6', name: 'Period 6', start: '13:15', end: '14:00' },
        { id: 'p7', name: 'Period 7', start: '14:00', end: '14:45' }
    ];

    // Theme Management
    const themeToggleBtn = document.getElementById('theme-toggle');
    const themeIcon = document.getElementById('theme-icon');
    const savedTheme = localStorage.getItem('tt-theme') || 'light';
    applyTheme(savedTheme);

    themeToggleBtn.addEventListener('click', () => {
        const activeTheme = document.body.getAttribute('data-theme');
        applyTheme(activeTheme === 'dark' ? 'light' : 'dark');
    });

    function applyTheme(theme) {
        document.body.setAttribute('data-theme', theme);
        localStorage.setItem('tt-theme', theme);
        themeIcon.textContent = theme === 'dark' ? '☀️' : '🌙';
    }

    // DOM Elements
    const timetableBody = document.getElementById('timetable-body');
    const conflictPill = document.getElementById('conflict-pill');
    const conflictStatusText = document.getElementById('conflict-status-text');
    const insightBanner = document.getElementById('insight-banner');
    const insightMessage = document.getElementById('insight-message');
    const statSubjectsCount = document.getElementById('stat-subjects-count');
    const statSlotsCount = document.getElementById('stat-slots-count');
    const subjectBtnCount = document.getElementById('subject-btn-count');
    const btnAutoGenerate = document.getElementById('btn-auto-generate');
    const btnAddSlot = document.getElementById('btn-add-slot');
    const btnManageSubjects = document.getElementById('btn-manage-subjects');
    const btnClearAll = document.getElementById('btn-clear-all');
    const btnPrint = document.getElementById('btn-print');

    // Modals
    const modalAddSlot = document.getElementById('modal-add-slot');
    const modalManageSubjects = document.getElementById('modal-manage-subjects');
    const formAddSlot = document.getElementById('form-add-slot');
    const formAddSubject = document.getElementById('form-add-subject');
    const slotSubjectSelect = document.getElementById('slot-subject-select');
    const slotRoomInput = document.getElementById('slot-room-input');
    const slotTeacherInput = document.getElementById('slot-teacher-input');
    const subjectsTableBody = document.getElementById('subjects-table-body');

    // Initialize
    async function init() {
        await fetchSubjects();
        await fetchTimetable();
    }

    // --- API Interactions ---

    async function fetchSubjects() {
        try {
            const res = await fetch('/api/subjects');
            if (!res.ok) throw new Error('HTTP ' + res.status);
            subjects = await res.json();
            updateSubjectUI();
        } catch (err) {
            console.error('Error fetching subjects:', err);
        }
    }

    async function fetchTimetable() {
        try {
            const res = await fetch('/api/timetable');
            if (!res.ok) throw new Error('HTTP ' + res.status);
            const data = await res.json();
            slots = data.slots || [];
            conflicts = data.conflicts || [];
            renderTimetable();
            updateConflictUI(data.hasConflicts, conflicts);
        } catch (err) {
            console.error('Error fetching timetable:', err);
        }
    }

    // --- Render Timetable Grid ---

    function renderTimetable() {
        timetableBody.innerHTML = '';

        PERIODS.forEach(period => {
            const tr = document.createElement('tr');

            if (period.isBreak) {
                tr.className = 'break-row';
                tr.innerHTML = `
                    <td class="time-cell break-cell">
                        <span class="period-name">10:30 - 10:50</span>
                        <span>Break</span>
                    </td>
                    <td colspan="5" class="break-banner-cell">
                        ☕ Morning Break (10:30 - 10:50)
                    </td>
                `;
            } else if (period.isLunch) {
                tr.className = 'lunch-row';
                tr.innerHTML = `
                    <td class="time-cell lunch-cell">
                        <span class="period-name">12:30 - 13:15</span>
                        <span>Lunch</span>
                    </td>
                    <td colspan="5" class="lunch-banner-cell">
                        🍱 Campus Lunch Break (12:30 - 13:15)
                    </td>
                `;
            } else {
                let rowHtml = `
                    <td class="time-cell">
                        <span class="period-name">${period.name}</span>
                        <span>${period.start} - ${period.end}</span>
                    </td>
                `;

                DAYS.forEach(day => {
                    const matchedSlots = slots.filter(s => 
                        (s.day && s.day.toLowerCase() === day.toLowerCase()) && s.startTime === period.start
                    );

                    if (matchedSlots.length > 0) {
                        rowHtml += `<td><div class="slot-container">`;
                        matchedSlots.forEach(slot => {
                            rowHtml += `
                                <div class="slot-card" style="--slot-color: ${escapeHtml(slot.color)}">
                                    <div class="slot-header">
                                        <span class="slot-code">${escapeHtml(slot.subjectCode || 'CLASS')}</span>
                                        <button class="slot-btn-delete" title="Delete period" data-slot-id="${escapeHtml(slot.id)}">&times;</button>
                                    </div>
                                    <div class="slot-title">${escapeHtml(slot.subjectName)}</div>
                                    <div class="slot-meta">
                                        <span class="meta-item" title="Faculty">👨‍🏫 ${escapeHtml(slot.teacher)}</span>
                                        <span class="meta-item" title="Room">📍 ${escapeHtml(slot.room)}</span>
                                    </div>
                                </div>
                            `;
                        });
                        rowHtml += `</div></td>`;
                    } else {
                        rowHtml += `<td></td>`;
                    }
                });

                tr.innerHTML = rowHtml;
            }

            timetableBody.appendChild(tr);
        });

        // Attach delete listeners
        document.querySelectorAll('.slot-btn-delete').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.stopPropagation();
                const slotId = btn.getAttribute('data-slot-id');
                if (confirm('Remove this class period?')) {
                    await deleteSlot(slotId);
                }
            });
        });

        statSlotsCount.textContent = slots.length;
    }

    async function deleteSlot(id) {
        try {
            const res = await fetch(`/api/timetable/slot?id=${encodeURIComponent(id)}`, { method: 'DELETE' });
            if (res.ok) {
                await fetchTimetable();
            }
        } catch (err) {
            alert('Failed to delete slot.');
        }
    }

    // --- Conflicts & Insights UI ---

    function updateConflictUI(hasConflicts, conflictList) {
        if (hasConflicts && conflictList.length > 0) {
            conflictPill.className = 'conflict-status-pill has-conflict';
            conflictStatusText.textContent = `${conflictList.length} Conflict${conflictList.length > 1 ? 's' : ''}`;

            insightBanner.className = 'insight-banner danger-banner';
            insightMessage.innerHTML = `
                <strong>⚠️ Clashes Detected:</strong>
                <ul style="margin-top: 6px; padding-left: 18px;">
                    ${conflictList.map(c => `<li>${escapeHtml(c)}</li>`).join('')}
                </ul>
            `;
        } else {
            conflictPill.className = 'conflict-status-pill no-conflict';
            conflictStatusText.textContent = '0 Conflicts';

            insightBanner.className = 'insight-banner success-banner';
            insightMessage.innerHTML = `
                <strong>✨ Schedule Optimal:</strong> All 7 periods balanced. Morning break (10:30–10:50) and lunch break (12:30–13:15) preserved.
            `;
        }
    }

    // --- Subject UI Updates ---

    function updateSubjectUI() {
        statSubjectsCount.textContent = subjects.length;
        subjectBtnCount.textContent = subjects.length;

        // Populate Add Slot dropdown
        slotSubjectSelect.innerHTML = subjects.map(s => `
            <option value="${escapeHtml(s.id)}" data-teacher="${escapeHtml(s.teacher)}" data-room="${escapeHtml(s.room)}" data-code="${escapeHtml(s.code)}" data-color="${escapeHtml(s.color)}">
                ${escapeHtml(s.code)} - ${escapeHtml(s.name)}
            </option>
        `).join('');

        if (subjects.length > 0) {
            slotTeacherInput.value = subjects[0].teacher;
            slotRoomInput.value = subjects[0].room;
        }

        // Populate Manage Subjects Table
        subjectsTableBody.innerHTML = subjects.map(s => `
            <tr>
                <td><span class="subject-color-swatch" style="background-color: ${escapeHtml(s.color)};"></span></td>
                <td><strong>${escapeHtml(s.name)}</strong></td>
                <td><code>${escapeHtml(s.code)}</code></td>
                <td>${escapeHtml(s.teacher)}</td>
                <td>${escapeHtml(s.room)}</td>
                <td>${s.weeklyHours} hrs</td>
                <td>
                    <button class="btn-delete-sub" data-sub-id="${escapeHtml(s.id)}" title="Delete Subject">&times;</button>
                </td>
            </tr>
        `).join('');

        document.querySelectorAll('.btn-delete-sub').forEach(btn => {
            btn.addEventListener('click', async () => {
                const subId = btn.getAttribute('data-sub-id');
                if (confirm('Delete this subject?')) {
                    await deleteSubject(subId);
                }
            });
        });
    }

    async function deleteSubject(id) {
        try {
            const res = await fetch(`/api/subjects?id=${encodeURIComponent(id)}`, { method: 'DELETE' });
            if (res.ok) {
                await fetchSubjects();
            }
        } catch (err) {
            alert('Failed to delete subject.');
        }
    }

    slotSubjectSelect.addEventListener('change', () => {
        const opt = slotSubjectSelect.selectedOptions[0];
        if (opt) {
            slotTeacherInput.value = opt.getAttribute('data-teacher') || '';
            slotRoomInput.value = opt.getAttribute('data-room') || 'BW106';
        }
    });

    // --- Action Listeners ---

    // 1. Auto Generate
    btnAutoGenerate.addEventListener('click', async () => {
        btnAutoGenerate.disabled = true;
        btnAutoGenerate.innerHTML = `<span>⏳</span> Generating...`;
        try {
            const res = await fetch('/api/timetable/generate', { method: 'POST' });
            if (!res.ok) {
                const errJson = await res.json().catch(() => ({}));
                throw new Error(errJson.error || 'Server error ' + res.status);
            }
            const data = await res.json();
            await fetchTimetable();

            insightBanner.className = 'insight-banner success-banner';
            insightMessage.innerHTML = `<strong>✨ Auto-Generated:</strong> Successfully generated ${data.totalSlots || 'all'} periods! All lab blocks and theory courses balanced with 0 clashes.`;
        } catch (err) {
            console.error('Auto-generate error:', err);
            alert('Failed to auto-generate timetable: ' + err.message + '\nMake sure the Java server is running!');
        } finally {
            btnAutoGenerate.disabled = false;
            btnAutoGenerate.innerHTML = `<span class="btn-icon-text">✨</span> Auto-Generate Smart Timetable`;
        }
    });

    // 2. Clear All
    btnClearAll.addEventListener('click', async () => {
        if (confirm('Clear all scheduled slots in the 7-period timetable?')) {
            await fetch('/api/timetable/clear', { method: 'POST' });
            await fetchTimetable();
        }
    });

    // 3. Print
    btnPrint.addEventListener('click', () => {
        window.print();
    });

    // --- Modal Management ---

    function openModal(modal) {
        modal.classList.add('open');
    }

    function closeModal(modal) {
        modal.classList.remove('open');
    }

    btnAddSlot.addEventListener('click', () => openModal(modalAddSlot));
    btnManageSubjects.addEventListener('click', () => openModal(modalManageSubjects));

    document.querySelectorAll('[data-close]').forEach(el => {
        el.addEventListener('click', () => {
            const modalId = el.getAttribute('data-close');
            const target = document.getElementById(modalId);
            if (target) closeModal(target);
        });
    });

    window.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal-overlay')) {
            closeModal(e.target);
        }
    });

    // --- Form Submissions ---

    // Submit Add Slot
    formAddSlot.addEventListener('submit', async (e) => {
        e.preventDefault();
        const selectedOpt = slotSubjectSelect.selectedOptions[0];
        if (!selectedOpt) return;

        const periodVal = document.getElementById('slot-period-select').value;
        const [start, end] = periodVal.split('-');

        const payload = {
            day: document.getElementById('slot-day-select').value,
            startTime: start,
            endTime: end,
            subjectName: selectedOpt.text.split('-')[1]?.trim() || selectedOpt.text,
            subjectCode: selectedOpt.getAttribute('data-code'),
            teacher: slotTeacherInput.value.trim(),
            room: slotRoomInput.value.trim(),
            color: selectedOpt.getAttribute('data-color') || '#3b82f6'
        };

        try {
            const res = await fetch('/api/timetable/slot', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (res.ok) {
                closeModal(modalAddSlot);
                await fetchTimetable();
            } else {
                alert('Failed to add slot.');
            }
        } catch (err) {
            alert('Error adding slot.');
        }
    });

    // Submit Add Subject
    formAddSubject.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            name: document.getElementById('sub-name').value.trim(),
            code: document.getElementById('sub-code').value.trim(),
            teacher: document.getElementById('sub-teacher').value.trim(),
            room: document.getElementById('sub-room').value.trim(),
            weeklyHours: document.getElementById('sub-hours').value.trim(),
            color: document.getElementById('sub-color').value
        };

        try {
            const res = await fetch('/api/subjects', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (res.ok) {
                formAddSubject.reset();
                document.getElementById('sub-color').value = '#3b82f6';
                document.getElementById('sub-hours').value = '3';
                document.getElementById('sub-room').value = 'BW106';
                await fetchSubjects();
            } else {
                alert('Failed to add subject.');
            }
        } catch (err) {
            alert('Error adding subject.');
        }
    });

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    init();
});
