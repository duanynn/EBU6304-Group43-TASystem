<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bupt.is.ta.model.User" %>
<%
    User current = (User) session.getAttribute("currentUser");
    String scheduleError = (String) request.getAttribute("scheduleError");
    String courseName = request.getAttribute("courseName") != null ? String.valueOf(request.getAttribute("courseName")) : "";
    String requiredSkills = request.getAttribute("requiredSkills") != null ? String.valueOf(request.getAttribute("requiredSkills")) : "";
    String description = request.getAttribute("description") != null ? String.valueOf(request.getAttribute("description")) : "";
    String jobType = request.getAttribute("jobType") != null ? String.valueOf(request.getAttribute("jobType")) : "MODULE_TA";
    int requiredCount = 1;
    if (request.getAttribute("requiredCount") instanceof Integer) {
        requiredCount = (Integer) request.getAttribute("requiredCount");
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Post New Job - Instructor Workspace</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css?v=20260519-schedule">
</head>
<body>
<header class="app-header">
    <h1>TA Recruitment System - Instructor Workspace</h1>
    <span class="user-info user-info-with-avatar"><%= current != null ? current.getName() : "" %>
        <jsp:include page="/WEB-INF/jsp/account-menu.jsp"/>
    </span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/mo/home">My Home</a>
    <a href="<%= request.getContextPath() %>/mo/dashboard">My Jobs</a>
    <a href="<%= request.getContextPath() %>/mo/postJob">Post New Job</a>
</nav>
<main class="app-main">
    <h2 class="page-title">Post New Job</h2>
    <div class="section">
        <% if (scheduleError != null && !scheduleError.isBlank()) { %>
        <p class="error"><%= scheduleError %></p>
        <% } %>
        <form method="post" action="<%= request.getContextPath() %>/mo/postJob" id="postJobForm">
            <div class="form-group">
                <label>Job Type</label>
                <select name="jobType" id="jobType">
                    <option value="MODULE_TA" <%= "MODULE_TA".equals(jobType) ? "selected" : "" %>>Module TA</option>
                    <option value="INVIGILATION" <%= "INVIGILATION".equals(jobType) ? "selected" : "" %>>Invigilation</option>
                    <option value="OTHER" <%= "OTHER".equals(jobType) ? "selected" : "" %>>Other activity</option>
                </select>
            </div>
            <div class="form-group">
                <label>Course Name</label>
                <input type="text" name="courseName" required placeholder="e.g. Software Engineering" value="<%= courseName %>">
            </div>
            <div class="form-group">
                <label>Required TA Count</label>
                <input type="number" name="requiredCount" value="<%= requiredCount %>" min="1" required>
            </div>
            <div class="form-group">
                <label>Required Skills (comma-separated)</label>
                <input type="text" name="requiredSkills" placeholder="Java, Git, Python" value="<%= requiredSkills %>">
            </div>
            <div class="form-group">
                <label>Weekly Time Slots</label>
                <p class="muted">Add one or more weekly sessions (day + start/end time). A course may have multiple slots per week.</p>
                <div id="scheduleSlotRows" class="schedule-slot-rows">
                    <div class="schedule-slot-row">
                        <select name="slotDay" required>
                            <option value="1">Monday</option>
                            <option value="2">Tuesday</option>
                            <option value="3">Wednesday</option>
                            <option value="4">Thursday</option>
                            <option value="5">Friday</option>
                            <option value="6">Saturday</option>
                            <option value="7">Sunday</option>
                        </select>
                        <input type="time" name="slotStart" value="09:00" min="08:00" max="23:00" required>
                        <span class="schedule-slot-sep">to</span>
                        <input type="time" name="slotEnd" value="11:00" min="09:00" max="23:00" required>
                        <button type="button" class="btn btn-small btn-secondary slot-remove-btn" hidden>Remove</button>
                    </div>
                </div>
                <button type="button" class="btn btn-small btn-secondary" id="addScheduleSlotBtn">Add time slot</button>
            </div>
            <div class="form-group">
                <div class="label-row">
                    <label for="jobDescription">Description</label>
                    <button type="button" class="btn btn-small btn-secondary" id="generateDescriptionBtn">Generate with AI</button>
                </div>
                <textarea id="jobDescription" name="description" rows="7" class="input-area" placeholder="Describe responsibilities, workload, candidate expectations, and course support needs."><%= description %></textarea>
                <div class="ai-inline-status muted" id="descriptionAiStatus"></div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">Post</button>
                <a href="<%= request.getContextPath() %>/mo/dashboard" class="btn btn-secondary">Cancel</a>
            </div>
        </form>
    </div>
</main>
<script src="<%= request.getContextPath() %>/js/schedule-time.js"></script>
<script>
    (function () {
        var rowsContainer = document.getElementById('scheduleSlotRows');
        var addBtn = document.getElementById('addScheduleSlotBtn');
        var form = document.getElementById('postJobForm');

        function refreshRemoveButtons() {
            var rows = rowsContainer.querySelectorAll('.schedule-slot-row');
            rows.forEach(function (row, index) {
                var removeBtn = row.querySelector('.slot-remove-btn');
                if (removeBtn) {
                    removeBtn.hidden = rows.length <= 1;
                }
            });
        }

        function addRow() {
            var first = rowsContainer.querySelector('.schedule-slot-row');
            if (!first) return;
            var clone = first.cloneNode(true);
            rowsContainer.appendChild(clone);
            if (window.ScheduleTimeUtil) {
                window.ScheduleTimeUtil.bindRow(clone);
            }
            refreshRemoveButtons();
        }

        if (addBtn) {
            addBtn.addEventListener('click', addRow);
        }

        rowsContainer.addEventListener('click', function (e) {
            if (e.target.classList.contains('slot-remove-btn')) {
                var row = e.target.closest('.schedule-slot-row');
                if (rowsContainer.querySelectorAll('.schedule-slot-row').length > 1) {
                    row.remove();
                    refreshRemoveButtons();
                }
            }
        });

        refreshRemoveButtons();

        var button = document.getElementById('generateDescriptionBtn');
        var description = document.getElementById('jobDescription');
        var status = document.getElementById('descriptionAiStatus');
        if (!form || !button || !description) return;

        function setStatus(text) {
            if (status) status.textContent = text || '';
        }

        button.addEventListener('click', function () {
            var courseInput = form.querySelector('input[name="courseName"]');
            if (!courseInput || !courseInput.value.trim()) {
                setStatus('Enter a course name before generating a description.');
                courseInput && courseInput.focus();
                return;
            }
            var body = new URLSearchParams(new FormData(form));
            button.disabled = true;
            setStatus('Generating description...');
            fetch('<%= request.getContextPath() %>/mo/generateJobDescription', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: body.toString()
            }).then(function (response) {
                if (!response.ok) throw new Error('Request failed');
                return response.json();
            }).then(function (data) {
                description.value = data && data.description ? data.description : '';
                setStatus(description.value ? 'Description generated. Review and edit before posting.' : 'No description was generated.');
            }).catch(function () {
                setStatus('Unable to generate right now. Please write the description manually.');
            }).finally(function () {
                button.disabled = false;
            });
        });
    })();
</script>
</body>
</html>
