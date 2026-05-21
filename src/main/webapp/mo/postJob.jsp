<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bupt.is.ta.model.User" %>
<%
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Post New Job - Instructor Workspace</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css?v=20260518-ui2">
</head>
<body>
<header class="app-header">
    <h1>TA Recruitment System - Instructor Workspace</h1>
    <span class="user-info"><%= current != null ? current.getName() : "" %> <a href="<%= request.getContextPath() %>/login">Logout</a></span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/mo/dashboard">My Jobs</a>
    <a href="<%= request.getContextPath() %>/mo/postJob">Post New Job</a>
</nav>
<main class="app-main">
    <h2 class="page-title">Post New Job</h2>
    <div class="section">
        <form method="post" action="<%= request.getContextPath() %>/mo/postJob">
            <div class="form-group">
                <label>Course Name</label>
                <input type="text" name="courseName" required placeholder="e.g. Software Engineering">
            </div>
            <div class="form-group">
                <label>Required TA Count</label>
                <input type="number" name="requiredCount" value="1" min="1" required>
            </div>
            <div class="form-group">
                <label>Required Skills (comma-separated)</label>
                <input type="text" name="requiredSkills" placeholder="Java, Git, Python">
            </div>
            <div class="form-group">
                <label>Required Working Time</label>
                <input type="text" name="requiredWorkTime" required placeholder="e.g. Tue afternoon / Thu evening / 8 hrs weekly">
            </div>
            <div class="form-group">
                <div class="label-row">
                    <label for="jobDescription">Description</label>
                    <button type="button" class="btn btn-small btn-secondary" id="generateDescriptionBtn">Generate with AI</button>
                </div>
                <textarea id="jobDescription" name="description" rows="7" class="input-area" placeholder="Describe responsibilities, workload, candidate expectations, and course support needs."></textarea>
                <div class="ai-inline-status muted" id="descriptionAiStatus"></div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">Post</button>
                <a href="<%= request.getContextPath() %>/mo/dashboard" class="btn btn-secondary">Cancel</a>
            </div>
        </form>
    </div>
</main>
<script>
    (function () {
        var form = document.querySelector('form[action$="/mo/postJob"]');
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
