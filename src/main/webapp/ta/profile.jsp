<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.web.TAController" %>
<%@ page import="bupt.is.ta.service.SkillMatchService" %>
<%@ page import="java.util.List" %>
<%
    User current = (User) session.getAttribute("currentUser");
    SkillMatchService.MatchResult profileMatch = (SkillMatchService.MatchResult) request.getAttribute("profileMatch");
    List<TAController.JobAdviceView> jobAdviceList = (List<TAController.JobAdviceView>) request.getAttribute("jobAdviceList");
    Boolean showProfilePrompt = (Boolean) request.getAttribute("showProfilePrompt");
    Boolean pendingNewJobAnalysis = (Boolean) request.getAttribute("pendingNewJobAnalysis");
    Boolean profileNeedsConfirm = (Boolean) request.getAttribute("profileNeedsConfirm");
    Boolean manualAiRefresh = (Boolean) request.getAttribute("manualAiRefresh");
    String loginPromptHint = (String) request.getAttribute("loginPromptHint");
    if (current == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My Profile - TA Recruitment System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="app-header">
    <h1>TA Recruitment System - Student Portal</h1>
    <span class="user-info"><%= current.getName() %> <a href="<%= request.getContextPath() %>/login">Logout</a></span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/ta/jobs">Job Board</a>
    <a href="<%= request.getContextPath() %>/ta/applications">My Applications</a>
    <a href="<%= request.getContextPath() %>/ta/profile">My Profile</a>
</nav>
<main class="app-main">
    <h2 class="page-title">My Profile</h2>
    <% if (loginPromptHint != null && !loginPromptHint.isBlank()) { %>
    <div class="alert alert-warning"><%= loginPromptHint %></div>
    <% } %>
    <% if (pendingNewJobAnalysis != null && pendingNewJobAnalysis) { %>
    <div class="alert alert-warning" id="newJobUpdateNotice">
        New jobs were detected and need AI analysis. Click "AI Refresh Now" and wait for completion.
        <form method="post" action="<%= request.getContextPath() %>/ta/loginProfileDecision" style="display:inline-block;margin-left:8px;">
            <input type="hidden" name="decision" value="ai">
            <button type="submit" class="btn btn-small btn-secondary">AI Refresh Now</button>
        </form>
    </div>
    <% } %>
    <% if (profileNeedsConfirm != null && profileNeedsConfirm) { %>
    <div class="alert alert-warning" id="materialConfirmNotice">Profile changes are not confirmed yet. Please click "Confirm Profile Update" before leaving this page.</div>
    <% } %>
    <section class="profile-hero">
        <div>
            <h3><%= current.getName() == null ? "Unnamed User" : current.getName() %></h3>
            <p>GPA: <%= current.getGpa() == null ? "-" : current.getGpa() %></p>
            <p>Available Time: <%= current.getAvailableTime() == null || current.getAvailableTime().isBlank() ? "-" : current.getAvailableTime() %></p>
            <p class="muted">Skill Tags: <%= current.getSkillTags() == null || current.getSkillTags().isEmpty() ? "N/A" : String.join(" / ", current.getSkillTags()) %></p>
        </div>
        <div class="score-box">
            <div class="score-label">AI Profile Score</div>
            <div class="score-value"><%= profileMatch == null ? "-" : Math.round(profileMatch.getAiScore()) + "%" %></div>
            <div class="muted"><%= profileMatch != null && profileMatch.isAiGenerated() ? "Real-time AI" : "Local evaluation" %></div>
        </div>
    </section>

    <div class="profile-grid profile-grid-reverse">
    <div class="left-col">
    <div class="section">
        <h3>Profile Editing</h3>
        <form method="post" action="<%= request.getContextPath() %>/ta/profile" id="profileEditForm">
            <div class="form-group">
                <label>Name</label>
                <input type="text" name="name" value="<%= current.getName() == null ? "" : current.getName() %>" data-profile-edit-field disabled/>
            </div>
            <div class="form-group">
                <label>GPA</label>
                <input type="text" name="gpa" value="<%= current.getGpa() == null ? "" : current.getGpa() %>" data-profile-edit-field disabled/>
            </div>
            <div class="form-group">
                <label>Skill Tags (comma-separated)</label>
                <input type="text" name="skillTags" value="<%= current.getSkillTags() == null ? "" : String.join(", ", current.getSkillTags()) %>" data-profile-edit-field disabled/>
            </div>
            <div class="form-group">
                <label>Available Time</label>
                <input type="text" name="availableTime" value="<%= current.getAvailableTime() == null ? "" : current.getAvailableTime() %>" data-profile-edit-field disabled/>
            </div>
            <div class="form-group">
                <label>Summary</label>
                <textarea name="summary" rows="3" class="input-area" data-profile-edit-field disabled><%= current.getProfile().getSummary() == null ? "" : current.getProfile().getSummary() %></textarea>
            </div>
            <div class="form-group">
                <label>Education</label>
                <textarea name="education" rows="3" class="input-area" data-profile-edit-field disabled><%= current.getProfile().getEducation() == null ? "" : current.getProfile().getEducation() %></textarea>
            </div>
            <div class="form-group">
                <label>Projects</label>
                <textarea name="projects" rows="4" class="input-area" data-profile-edit-field disabled><%= current.getProfile().getProjects() == null ? "" : current.getProfile().getProjects() %></textarea>
            </div>
            <div class="form-group">
                <label>Awards</label>
                <textarea name="awards" rows="3" class="input-area" data-profile-edit-field disabled><%= current.getProfile().getCertificates() == null ? "" : current.getProfile().getCertificates() %></textarea>
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-secondary" id="profileEditBtn">Edit Profile</button>
                <button type="button" class="btn btn-secondary" id="profileCancelBtn" style="display:none;">Cancel</button>
                <button type="submit" class="btn" id="profileSaveBtn" style="display:none;">Confirm Profile Update</button>
            </div>
        </form>
    </div>
    <div class="section">
        <h3>CV Upload and Parsing</h3>
        <form method="post" action="<%= request.getContextPath() %>/ta/uploadCv" enctype="multipart/form-data">
            <div class="form-group">
                <label>Upload CV (PDF/DOC/DOCX, <=5MB)</label>
                <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap;">
                    <label for="cvFileInput" class="btn btn-small btn-secondary" style="margin:0;">Choose File</label>
                    <input type="file" id="cvFileInput" name="cvFile" accept=".pdf,.doc,.docx" style="position:absolute;left:-9999px;width:1px;height:1px;opacity:0;"/>
                    <span id="cvFileName" class="muted">No file selected</span>
                </div>
            </div>
            <button type="submit" class="btn btn-small">Upload and Auto-Parse (Profile Only)</button>
        </form>
        <% if (current.getCvPath() != null && !current.getCvPath().isBlank()) { %>
        <p class="muted">CV uploaded. Last parsed at: <%= current.getProfile().getLastParsedAt() == null ? "-" : current.getProfile().getLastParsedAt() %></p>
        <form method="post" action="<%= request.getContextPath() %>/ta/reparseCv" style="display:inline-block;">
            <button type="submit" class="btn btn-small">Re-parse</button>
        </form>
        <form method="post" action="<%= request.getContextPath() %>/ta/deleteCv" style="display:inline-block;">
            <button type="submit" class="btn btn-small btn-danger">Delete CV</button>
        </form>
        <% } %>
    </div>
    </div>

    <div class="right-col">
    <div class="section">
        <h3>AI Advice by Position</h3>
        <% if (profileNeedsConfirm != null && profileNeedsConfirm) { %>
            <p class="muted">You recently updated your profile. Please click "Confirm Profile Update" first to regenerate AI analysis.</p>
        <% } else if (jobAdviceList != null && !jobAdviceList.isEmpty()) { %>
            <% for (TAController.JobAdviceView item : jobAdviceList) { %>
                <div class="job-advice-card">
                    <div class="job-advice-head">
                        <h4><%= item.getJob().getCourseName() == null ? "Unnamed Position" : item.getJob().getCourseName() %></h4>
                        <span class="fit-pill <%= item.getMatch().getAiScore() >= 75 ? "fit-high" : (item.getMatch().getAiScore() >= 45 ? "fit-mid" : "fit-low") %>">
                            Fit <%= Math.round(item.getMatch().getAiScore()) %>%
                        </span>
                    </div>
                    <p><strong>Requirements:</strong> <%= item.getJob().getRequiredSkills() == null || item.getJob().getRequiredSkills().isEmpty() ? "N/A" : String.join(" / ", item.getJob().getRequiredSkills()) %></p>
                    <p><strong>Fit Summary:</strong> <%= item.getMatch().getAiFitSummary() == null || item.getMatch().getAiFitSummary().isBlank() ? "N/A" : item.getMatch().getAiFitSummary() %></p>
                    <p><strong>Improvement Advice:</strong> <%= item.getMatch().getAiAdvice() == null || item.getMatch().getAiAdvice().isBlank() ? "N/A" : item.getMatch().getAiAdvice() %></p>
                    <p class="muted">Advice Source: <%= item.getMatch().isAiGenerated() ? "Real-time AI" : "Local fallback" %></p>
                </div>
            <% } %>
        <% } else { %>
            <p class="muted">No open positions. Position-level advice cannot be generated.</p>
        <% } %>
    </div>

    <div class="section">
        <h3>CV Parsing Preview</h3>
        <p><strong>Extracted Name:</strong> <%= current.getProfile().getExtractedName() == null || current.getProfile().getExtractedName().isBlank() ? "N/A" : current.getProfile().getExtractedName() %></p>
        <p><strong>Extracted Skills:</strong></p>
        <div class="chip-wrap">
            <% if (current.getProfile().getExtractedSkills() != null && !current.getProfile().getExtractedSkills().isEmpty()) {
                for (String sk : current.getProfile().getExtractedSkills()) { %>
            <span class="chip"><%= sk %></span>
            <% }} else { %>
            <span class="muted">N/A</span>
            <% } %>
        </div>
        <p><strong>Education:</strong> <%= current.getProfile().getEducation() == null || current.getProfile().getEducation().isBlank() ? "N/A" : current.getProfile().getEducation() %></p>
        <p><strong>Projects:</strong> <%= current.getProfile().getProjects() == null || current.getProfile().getProjects().isBlank() ? "N/A" : current.getProfile().getProjects() %></p>
        <p><strong>Awards:</strong> <%= current.getProfile().getCertificates() == null || current.getProfile().getCertificates().isBlank() ? "N/A" : current.getProfile().getCertificates() %></p>
        <details style="margin-top:10px;">
            <summary>View Parsed Raw Text (excerpt)</summary>
            <pre class="raw-cv"><%= current.getProfile().getRawCvText() == null || current.getProfile().getRawCvText().isBlank() ? "N/A" : current.getProfile().getRawCvText() %></pre>
        </details>
    </div>
    </div>
    </div>
</main>
<div id="aiLoadingMask" class="ai-loading-mask" style="display:none;">
    <div class="ai-loading-box">
        <div class="ai-loading-spinner"></div>
        <div>AI is analyzing, please wait...</div>
    </div>
</div>
<div id="profilePromptMask" class="ai-loading-mask" style="display:none;">
    <div class="ai-loading-box" style="display:block;min-width:360px;">
        <div style="font-weight:700;margin-bottom:12px;">First Visit: Choose Next Step</div>
        <div class="muted" style="margin-bottom:14px;">You can edit your full profile first, or trigger AI refresh directly. If no CV is uploaded, the system will ask you to upload one.</div>
        <form method="post" action="<%= request.getContextPath() %>/ta/loginProfileDecision" style="display:inline-block;margin-right:8px;">
            <input type="hidden" name="decision" value="edit">
            <button type="submit" class="btn">Edit Full Profile First</button>
        </form>
        <form method="post" action="<%= request.getContextPath() %>/ta/loginProfileDecision" style="display:inline-block;">
            <input type="hidden" name="decision" value="ai">
            <button type="submit" class="btn btn-secondary">AI Refresh Now</button>
        </form>
    </div>
</div>
<script>
    (function () {
        const profileForm = document.querySelector('form[action$="/ta/profile"]');
        const uploadForm = document.querySelector('form[action$="/ta/uploadCv"]');
        const reparseForm = document.querySelector('form[action$="/ta/reparseCv"]');
        const mask = document.getElementById('aiLoadingMask');
        if (!mask) return;
        function showMask() { mask.style.display = 'flex'; }
        if (profileForm) profileForm.addEventListener('submit', showMask);
        if (uploadForm) {
            uploadForm.addEventListener('submit', function (evt) {
                var fileInput = uploadForm.querySelector('input[type="file"][name="cvFile"]');
                if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
                    evt.preventDefault();
                    window.alert('Please upload a file.');
                    return;
                }
                showMask();
            });
            var cvFileInput = uploadForm.querySelector('input[type="file"][name="cvFile"]');
            var cvFileName = document.getElementById('cvFileName');
            if (cvFileInput && cvFileName) {
                cvFileInput.addEventListener('change', function () {
                    var name = (cvFileInput.files && cvFileInput.files.length > 0) ? cvFileInput.files[0].name : 'No file selected';
                    cvFileName.textContent = name;
                });
            }
        }
        if (reparseForm) reparseForm.addEventListener('submit', showMask);
        var profileNeedsConfirm = <%= (profileNeedsConfirm != null && profileNeedsConfirm) ? "true" : "false" %>;
        var hasUnsavedEdits = false;
        if (profileForm) {
            const editableFields = profileForm.querySelectorAll('[data-profile-edit-field]');
            const editBtn = document.getElementById('profileEditBtn');
            const cancelBtn = document.getElementById('profileCancelBtn');
            const saveBtn = document.getElementById('profileSaveBtn');
            const initialValues = Array.from(editableFields).map(function (field) {
                return field.value;
            });

            function setEditMode(enabled) {
                editableFields.forEach(function (field) {
                    field.disabled = !enabled;
                });
                if (!enabled) {
                    hasUnsavedEdits = false;
                }
                if (editBtn) editBtn.style.display = enabled ? 'none' : 'inline-block';
                if (cancelBtn) cancelBtn.style.display = enabled ? 'inline-block' : 'none';
                if (saveBtn) saveBtn.style.display = enabled ? 'inline-block' : 'none';
            }

            if (editBtn) {
                editBtn.addEventListener('click', function () {
                    setEditMode(true);
                });
            }
            if (cancelBtn) {
                cancelBtn.addEventListener('click', function () {
                    editableFields.forEach(function (field, idx) {
                        field.value = initialValues[idx];
                    });
                    setEditMode(false);
                });
            }
            editableFields.forEach(function (field) {
                field.addEventListener('input', function () {
                    hasUnsavedEdits = true;
                });
            });
            setEditMode(false);
        }
        var shouldPrompt = <%= (showProfilePrompt != null && showProfilePrompt) ? "true" : "false" %>;
        if (shouldPrompt) {
            var promptMask = document.getElementById('profilePromptMask');
            if (promptMask) promptMask.style.display = 'flex';
        }
        var manualAiRefresh = <%= (manualAiRefresh != null && manualAiRefresh) ? "true" : "false" %>;
        if (manualAiRefresh) {
            showMask();
            var loadingText = mask.querySelector('div:last-child');
            if (loadingText) loadingText.textContent = 'AI refresh in progress, please wait...';
            function pollRefresh(attempt) {
                fetch('<%= request.getContextPath() %>/ta/refreshNewJobsAi', {
                    method: 'POST',
                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                }).then(function (r) { return r.json(); }).then(function (data) {
                    if (data && data.inProgress) {
                        if (attempt < 8) {
                            setTimeout(function () { pollRefresh(attempt + 1); }, 2500);
                        } else {
                            if (loadingText) loadingText.textContent = 'Analysis is still running. Please refresh the page later.';
                        }
                        return;
                    }
                    if (data && data.updated) {
                        window.location.reload();
                    } else {
                        window.location.reload();
                    }
                }).catch(function () {
                    if (loadingText) loadingText.textContent = 'AI refresh failed. Please refresh and try again.';
                });
            }
            pollRefresh(0);
        }

        document.querySelectorAll('.app-nav a').forEach(function (link) {
            link.addEventListener('click', function (evt) {
                if (profileNeedsConfirm || hasUnsavedEdits) {
                    var ok = window.confirm('You have unconfirmed profile changes. Continue leaving this page?');
                    if (!ok) {
                        evt.preventDefault();
                    }
                }
            });
        });
        document.querySelectorAll('a[href$="/login"]').forEach(function (link) {
            link.addEventListener('click', function (evt) {
                if (profileNeedsConfirm || hasUnsavedEdits) {
                    var ok = window.confirm('You have unconfirmed profile changes. Confirm logout?');
                    if (!ok) {
                        evt.preventDefault();
                    }
                }
            });
        });
    })();
</script>
</body>
</html>
