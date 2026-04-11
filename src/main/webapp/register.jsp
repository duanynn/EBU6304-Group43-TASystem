<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Student Registration - TA Recruitment System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div class="login-wrap">
    <h2>Student Registration</h2>
    <% if (request.getAttribute("error") != null) { %>
        <p class="error"><%= request.getAttribute("error") %></p>
    <% } %>
    <form method="post" action="<%= request.getContextPath() %>/register" enctype="multipart/form-data">
        <label>Student ID <span style="color:red">*</span>
            <input type="text" name="id" required pattern="\d{10}" maxlength="10" placeholder="10 digits" title="Student ID must be 10 digits">
        </label>
        <label>Password <span style="color:red">*</span>
            <input type="password" name="password" required placeholder="Set password">
        </label>
        <label>Name
            <input type="text" name="name" placeholder="Defaults to student ID if empty">
        </label>
        <label>GPA (optional)
            <input type="text" name="gpa" placeholder="e.g. 3.8">
        </label>
        <label>Skill Tags (optional, comma-separated)
            <input type="text" name="skillTags" placeholder="e.g. Java, Python, IELTS">
        </label>
        <label>Available Time <span style="color:red">*</span>
            <input type="text" name="availableTime" required placeholder="e.g. Mon evening / Wed afternoon / 8-10 hrs per week">
        </label>
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
<script>
    (function () {
        var fileInput = document.getElementById('registerCvFileInput');
        var fileName = document.getElementById('registerCvFileName');
        if (!fileInput || !fileName) return;
        fileInput.addEventListener('change', function () {
            var name = (fileInput.files && fileInput.files.length > 0) ? fileInput.files[0].name : 'No file selected';
            fileName.textContent = name;
        });
    })();
</script>
</body>
</html>
