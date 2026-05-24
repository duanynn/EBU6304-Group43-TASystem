<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Student Registration - TA Recruitment System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css?v=20260518-ui2">
</head>
<body>
<div class="login-wrap">
    <h2>Student Registration</h2>
    <% if (request.getAttribute("error") != null) { %>
        <p class="alert alert-error" role="alert"><%= request.getAttribute("error") %></p>
    <% } %>
    <form method="post" action="<%= request.getContextPath() %>/register" enctype="multipart/form-data">
        <label>Student ID <span style="color:red">*</span>
            <input type="text" name="id" required pattern="\d{10}" maxlength="10" placeholder="10 digits" title="Student ID must be 10 digits">
        </label>
        <label>Password <span style="color:red">*</span>
            <input type="password" name="password" required placeholder="Set password">
        </label>
        <label>ID card last 6 digits <span style="color:red">*</span>
            <input type="password" name="idCardSuffix" required pattern="\d{6}" maxlength="6" placeholder="6 digits" autocomplete="off">
        </label>
        <p class="muted" style="margin: -8px 0 12px;">Used only if you forget your password. Never shown on your profile.</p>
<<<<<<< HEAD
=======
        <label>ID card last 6 digits <span style="color:red">*</span>
            <input type="password" name="idCardSuffix" required pattern="\d{6}" maxlength="6" placeholder="6 digits" autocomplete="off">
        </label>
        <p class="muted" style="margin: -8px 0 12px;">Used only if you forget your password. Never shown on your profile.</p>
>>>>>>> 9fc713c8d26730f0b4abfebe4af96d9412dcb2bc
        <label>Name
            <input type="text" name="name" placeholder="Defaults to student ID if empty">
        </label>
        <label>GPA (optional)
            <input type="text" name="gpa" placeholder="e.g. 3.8">
        </label>
        <label>Skill Tags (optional, comma-separated)
            <input type="text" name="skillTags" placeholder="e.g. Java, Python, IELTS">
        </label>
        <label>Weekly Availability <span style="color:red">*</span></label>
        <p class="muted" style="margin: -8px 0 10px;">Select at least one weekly time slot when you can work.</p>
        <div id="availSlotRows" class="schedule-slot-rows">
            <div class="schedule-slot-row">
                <select name="availDay" required>
                    <option value="1">Monday</option><option value="2">Tuesday</option><option value="3">Wednesday</option>
                    <option value="4">Thursday</option><option value="5">Friday</option><option value="6">Saturday</option><option value="7">Sunday</option>
                </select>
                <input type="time" name="availStart" value="09:00" min="08:00" max="23:00" required>
                <span class="schedule-slot-sep">to</span>
                <input type="time" name="availEnd" value="12:00" min="09:00" max="23:00" required>
                <button type="button" class="btn btn-small btn-secondary avail-remove-btn" hidden>Remove</button>
            </div>
        </div>
        <button type="button" class="btn btn-small btn-secondary" id="addAvailSlotBtn" style="margin-bottom:14px;">Add availability slot</button>
<<<<<<< HEAD
=======
        <label>Weekly Availability <span style="color:red">*</span></label>
        <p class="muted" style="margin: -8px 0 10px;">Select at least one weekly time slot when you can work.</p>
        <div id="availSlotRows" class="schedule-slot-rows">
            <div class="schedule-slot-row">
                <select name="availDay" required>
                    <option value="1">Monday</option><option value="2">Tuesday</option><option value="3">Wednesday</option>
                    <option value="4">Thursday</option><option value="5">Friday</option><option value="6">Saturday</option><option value="7">Sunday</option>
                </select>
                <input type="time" name="availStart" value="09:00" min="08:00" max="23:00" required>
                <span class="schedule-slot-sep">to</span>
                <input type="time" name="availEnd" value="12:00" min="09:00" max="23:00" required>
                <button type="button" class="btn btn-small btn-secondary avail-remove-btn" hidden>Remove</button>
            </div>
        </div>
        <button type="button" class="btn btn-small btn-secondary" id="addAvailSlotBtn" style="margin-bottom:14px;">Add availability slot</button>
>>>>>>> 9fc713c8d26730f0b4abfebe4af96d9412dcb2bc
        <label>Upload CV on registration (optional, PDF/DOC/DOCX, <=5MB)</label>
        <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap;margin-bottom:12px;">
            <label for="registerCvFileInput" class="btn btn-small btn-secondary" style="margin:0;">Choose File</label>
            <input type="file" id="registerCvFileInput" name="cvFile" accept=".pdf,.doc,.docx" style="position:absolute;left:-9999px;width:1px;height:1px;opacity:0;">
            <span id="registerCvFileName" class="muted">No file selected</span>
        </div>
        <button type="submit" class="btn">Register</button>
        <p style="margin-top:16px; font-size:14px;">
            <a href="<%= request.getContextPath() %>/login.jsp">Already have an account? Login</a>
        </p>
    </form>
</div>
<script src="<%= request.getContextPath() %>/js/schedule-time.js"></script>
<script src="<%= request.getContextPath() %>/js/upload-limits.js"></script>
<script>
    (function () {
        var fileInput = document.getElementById('registerCvFileInput');
        var fileName = document.getElementById('registerCvFileName');
        if (!fileInput || !fileName) return;
        fileInput.addEventListener('change', function () {
            var name = (fileInput.files && fileInput.files.length > 0) ? fileInput.files[0].name : 'No file selected';
            fileName.textContent = name;
        });
        var rowsContainer = document.getElementById('availSlotRows');
        var addBtn = document.getElementById('addAvailSlotBtn');
        function refreshRemove() {
            var rows = rowsContainer.querySelectorAll('.schedule-slot-row');
            rows.forEach(function (row) {
                var btn = row.querySelector('.avail-remove-btn');
                if (btn) btn.hidden = rows.length <= 1;
            });
        }
        if (addBtn) addBtn.addEventListener('click', function () {
            var first = rowsContainer.querySelector('.schedule-slot-row');
            if (first) {
                var clone = first.cloneNode(true);
                rowsContainer.appendChild(clone);
                if (window.ScheduleTimeUtil) window.ScheduleTimeUtil.bindRow(clone);
                refreshRemove();
            }
        });
        rowsContainer.addEventListener('click', function (e) {
            if (e.target.classList.contains('avail-remove-btn') && rowsContainer.querySelectorAll('.schedule-slot-row').length > 1) {
                e.target.closest('.schedule-slot-row').remove();
                refreshRemove();
            }
        });
        refreshRemove();
        var form = document.querySelector('form[action$="/register"]');
        if (form) {
            form.addEventListener('submit', function (evt) {
                if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
                    return;
                }
                if (window.UploadLimitsClient && !window.UploadLimitsClient.checkCv(fileInput.files[0])) {
                    evt.preventDefault();
                }
            });
        }
    })();
</script>
</body>
</html>
