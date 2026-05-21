<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%!
    private String h(Object value) {
        if (value == null) return "";
        return String.valueOf(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String blankToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
%>
<%
    User current = (User) session.getAttribute("currentUser");
    Boolean profileNeedsConfirm = (Boolean) request.getAttribute("profileNeedsConfirm");
    Boolean profileInitialized = (Boolean) request.getAttribute("profileInitialized");
    Integer profileCompletion = (Integer) request.getAttribute("profileCompletion");
    String loginPromptHint = (String) request.getAttribute("loginPromptHint");
    if (current == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    boolean initialized = Boolean.TRUE.equals(profileInitialized);
    boolean needsConfirm = Boolean.TRUE.equals(profileNeedsConfirm);
    boolean startInEdit = !initialized || needsConfirm;
    int completion = profileCompletion == null ? 0 : profileCompletion;
    String lastEvaluation = current.getProfile().getLastAiAdviceTime() > 0
            ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(current.getProfile().getLastAiAdviceTime()))
            : "Not evaluated yet";
    List<String> skillTags = current.getSkillTags() == null ? List.of() : current.getSkillTags();
    List<String> extractedSkills = current.getProfile().getExtractedSkills() == null ? List.of() : current.getProfile().getExtractedSkills();
    String resumeState = current.getCvPath() == null || current.getCvPath().isBlank() ? "Missing" : "Uploaded";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My Profile - TA Recruitment System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css?v=20260518-ui2">
</head>
<body>
<header class="app-header">
    <h1>TA Recruitment System - Student Portal</h1>
    <span class="user-info"><%= h(current.getName()) %> <a href="<%= request.getContextPath() %>/login">Logout</a></span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/ta/jobs">Job Board</a>
    <a href="<%= request.getContextPath() %>/ta/applications">My Applications</a>
    <a href="<%= request.getContextPath() %>/ta/profile">My Profile</a>
</nav>
<main class="app-main talent-main">
    <% if (loginPromptHint != null && !loginPromptHint.isBlank()) { %>
    <div class="alert alert-warning"><%= h(loginPromptHint) %></div>
    <% } %>
    <% if (needsConfirm) { %>
    <div class="alert alert-warning" id="materialConfirmNotice">Review the parsed profile, then save. Job board fit scores will be recalculated after saving.</div>
    <% } %>

    <section class="talent-hero">
        <div class="talent-identity">
            <span class="eyebrow">Candidate profile</span>
            <h2><%= h(current.getName() == null || current.getName().isBlank() ? "Unnamed Student" : current.getName()) %></h2>
            <p><%= h(blankToDash(current.getId())) %> - <%= h(blankToDash(current.getAvailableTime())) %></p>
            <div class="chip-wrap compact">
                <% if (!skillTags.isEmpty()) {
                    for (String skill : skillTags) { %>
                <span class="chip"><%= h(skill) %></span>
                <% }} else { %>
                <span class="muted">No skills added yet</span>
                <% } %>
            </div>
        </div>
        <div class="talent-metrics">
            <div class="talent-metric">
                <span>Completeness</span>
                <strong><%= completion %>%</strong>
            </div>
            <div class="talent-metric">
                <span>GPA</span>
                <strong><%= current.getGpa() == null ? "-" : current.getGpa() %></strong>
            </div>
            <div class="talent-metric">
                <span>Resume</span>
                <strong><%= h(resumeState) %></strong>
            </div>
        </div>
    </section>

    <div id="profileWorkspace" class="talent-workspace <%= startInEdit ? "profile-edit-mode" : "profile-view-mode" %>">
        <section class="profile-preview-pane">
            <div class="profile-toolbar">
                <div>
                    <h3>Profile Preview</h3>
                    <p class="muted">Last profile evaluation: <%= h(lastEvaluation) %></p>
                </div>
                <button type="button" class="btn" id="profileEditBtn">Edit Profile</button>
            </div>

            <div class="talent-layout">
                <aside class="talent-sidebar">
                    <div class="completion-block">
                        <div class="completion-top">
                            <span>Profile completeness</span>
                            <strong><%= completion %>%</strong>
                        </div>
                        <span class="score-meter"><span style="width:<%= completion %>%"></span></span>
                    </div>
                    <div class="fact-list">
                        <div><span>GPA</span><strong><%= current.getGpa() == null ? "-" : current.getGpa() %></strong></div>
                        <div><span>CV</span><strong><%= h(resumeState) %></strong></div>
                        <div><span>Parsed</span><strong><%= h(blankToDash(current.getProfile().getLastParsedAt())) %></strong></div>
                    </div>
                    <div class="profile-upload-panel">
                        <h4>Resume</h4>
                        <form method="post" action="<%= request.getContextPath() %>/ta/uploadCv" enctype="multipart/form-data">
                            <label for="cvFileInput" class="btn btn-small btn-secondary">Choose File</label>
                            <input type="file" id="cvFileInput" name="cvFile" accept=".pdf,.doc,.docx" style="position:absolute;left:-9999px;width:1px;height:1px;opacity:0;"/>
                            <span id="cvFileName" class="muted">No file selected</span>
                            <button type="submit" class="btn btn-small">Upload and Parse</button>
                        </form>
                        <% if (current.getCvPath() != null && !current.getCvPath().isBlank()) { %>
                        <div class="inline-actions">
                            <form method="post" action="<%= request.getContextPath() %>/ta/reparseCv">
                                <button type="submit" class="btn btn-small btn-secondary">Re-parse</button>
                            </form>
                            <form method="post" action="<%= request.getContextPath() %>/ta/deleteCv">
                                <button type="submit" class="btn btn-small btn-danger">Delete</button>
                            </form>
                        </div>
                        <% } %>
                    </div>
                </aside>

                <div class="talent-content">
                    <section class="profile-band">
                        <h3>Candidate Summary</h3>
                        <p><%= h(blankToDash(current.getProfile().getSummary())) %></p>
                    </section>
                    <section class="profile-band">
                        <h3>Skills Intelligence</h3>
                        <div class="skill-columns">
                            <div>
                                <span class="kv-label">Declared Skills</span>
                                <div class="chip-wrap">
                                    <% if (!skillTags.isEmpty()) {
                                        for (String skill : skillTags) { %>
                                    <span class="chip"><%= h(skill) %></span>
                                    <% }} else { %>
                                    <span class="muted">N/A</span>
                                    <% } %>
                                </div>
                            </div>
                            <div>
                                <span class="kv-label">Extracted From CV</span>
                                <div class="chip-wrap">
                                    <% if (!extractedSkills.isEmpty()) {
                                        for (String skill : extractedSkills) { %>
                                    <span class="chip chip-neutral"><%= h(skill) %></span>
                                    <% }} else { %>
                                    <span class="muted">N/A</span>
                                    <% } %>
                                </div>
                            </div>
                        </div>
                    </section>
                    <section class="profile-band">
                        <h3>Experience Evidence</h3>
                        <div class="evidence-grid">
                            <div>
                                <span class="kv-label">Education</span>
                                <p><%= h(blankToDash(current.getProfile().getEducation())) %></p>
                            </div>
                            <div>
                                <span class="kv-label">Projects</span>
                                <p><%= h(blankToDash(current.getProfile().getProjects())) %></p>
                            </div>
                            <div>
                                <span class="kv-label">Awards</span>
                                <p><%= h(blankToDash(current.getProfile().getCertificates())) %></p>
                            </div>
                        </div>
                    </section>
                </div>
            </div>
        </section>

        <section class="profile-edit-pane">
            <div class="profile-toolbar">
                <div>
                    <h3><%= initialized ? "Edit Profile" : "Complete Your Profile" %></h3>
                    <p class="muted">Saving refreshes your profile and the fit scores shown on the job board.</p>
                </div>
            </div>
            <form method="post" action="<%= request.getContextPath() %>/ta/profile" id="profileEditForm" class="talent-edit-form">
                <div class="form-grid two-col-form">
                    <div class="form-group">
                        <label>Name</label>
                        <input type="text" name="name" value="<%= h(current.getName()) %>" data-profile-edit-field/>
                    </div>
                    <div class="form-group">
                        <label>GPA</label>
                        <input type="text" name="gpa" value="<%= current.getGpa() == null ? "" : current.getGpa() %>" data-profile-edit-field/>
                    </div>
                    <div class="form-group">
                        <label>Skill Tags</label>
                        <input type="text" name="skillTags" value="<%= h(skillTags.isEmpty() ? "" : String.join(", ", skillTags)) %>" placeholder="Java, Python, Git" data-profile-edit-field/>
                    </div>
                    <div class="form-group">
                        <label>Available Time</label>
                        <input type="text" name="availableTime" value="<%= h(current.getAvailableTime()) %>" placeholder="Mon evening / 8 hrs weekly" data-profile-edit-field/>
                    </div>
                </div>
                <div class="form-group">
                    <label>Summary</label>
                    <textarea name="summary" rows="3" class="input-area" data-profile-edit-field><%= h(current.getProfile().getSummary()) %></textarea>
                </div>
                <div class="form-grid two-col-form">
                    <div class="form-group">
                        <label>Education</label>
                        <textarea name="education" rows="4" class="input-area" data-profile-edit-field><%= h(current.getProfile().getEducation()) %></textarea>
                    </div>
                    <div class="form-group">
                        <label>Projects</label>
                        <textarea name="projects" rows="4" class="input-area" data-profile-edit-field><%= h(current.getProfile().getProjects()) %></textarea>
                    </div>
                </div>
                <div class="form-group">
                    <label>Awards</label>
                    <textarea name="awards" rows="3" class="input-area" data-profile-edit-field><%= h(current.getProfile().getCertificates()) %></textarea>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn-success" id="profileSaveBtn">Save and Recalculate Fits</button>
                    <button type="button" class="btn btn-secondary" id="profileCancelBtn">Cancel</button>
                </div>
            </form>
        </section>
    </div>
</main>

<div id="aiLoadingMask" class="ai-loading-mask" style="display:none;">
    <div class="ai-loading-box">
        <div class="ai-loading-spinner"></div>
        <div>Recalculating job fit, please wait...</div>
    </div>
</div>

<script>
    (function () {
        var workspace = document.getElementById('profileWorkspace');
        var profileForm = document.getElementById('profileEditForm');
        var uploadForm = document.querySelector('form[action$="/ta/uploadCv"]');
        var reparseForm = document.querySelector('form[action$="/ta/reparseCv"]');
        var editBtn = document.getElementById('profileEditBtn');
        var cancelBtn = document.getElementById('profileCancelBtn');
        var mask = document.getElementById('aiLoadingMask');
        var initialized = <%= initialized ? "true" : "false" %>;
        var profileNeedsConfirm = <%= needsConfirm ? "true" : "false" %>;
        var hasUnsavedEdits = false;

        function showMask(text) {
            if (!mask) return;
            var loadingText = mask.querySelector('.ai-loading-box div:last-child');
            if (loadingText && text) loadingText.textContent = text;
            mask.style.display = 'flex';
        }

        function setMode(editing) {
            if (!workspace) return;
            workspace.classList.toggle('profile-edit-mode', editing);
            workspace.classList.toggle('profile-view-mode', !editing);
        }

        if (editBtn) {
            editBtn.addEventListener('click', function () {
                setMode(true);
            });
        }
        if (cancelBtn) {
            cancelBtn.addEventListener('click', function () {
                if (!initialized || profileNeedsConfirm) return;
                setMode(false);
                hasUnsavedEdits = false;
            });
        }
        if (profileForm) {
            profileForm.addEventListener('submit', function () {
                showMask('Saving profile and recalculating every job fit...');
            });
            profileForm.querySelectorAll('[data-profile-edit-field]').forEach(function (field) {
                field.addEventListener('input', function () {
                    hasUnsavedEdits = true;
                });
            });
        }
        if (uploadForm) {
            uploadForm.addEventListener('submit', function (evt) {
                var fileInput = uploadForm.querySelector('input[type="file"][name="cvFile"]');
                if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
                    evt.preventDefault();
                    window.alert('Please choose a CV file first.');
                    return;
                }
                showMask('Parsing CV and rebuilding profile...');
            });
            var cvFileInput = uploadForm.querySelector('input[type="file"][name="cvFile"]');
            var cvFileName = document.getElementById('cvFileName');
            if (cvFileInput && cvFileName) {
                cvFileInput.addEventListener('change', function () {
                    cvFileName.textContent = (cvFileInput.files && cvFileInput.files.length > 0) ? cvFileInput.files[0].name : 'No file selected';
                });
            }
        }
        if (reparseForm) {
            reparseForm.addEventListener('submit', function () {
                showMask('Re-parsing CV and updating profile insights...');
            });
        }

        document.querySelectorAll('.app-nav a, a[href$="/login"]').forEach(function (link) {
            link.addEventListener('click', function (evt) {
                if (profileNeedsConfirm || hasUnsavedEdits) {
                    var ok = window.confirm('You have unsaved profile changes. Continue leaving this page?');
                    if (!ok) evt.preventDefault();
                }
            });
        });
    })();
</script>
</body>
</html>
