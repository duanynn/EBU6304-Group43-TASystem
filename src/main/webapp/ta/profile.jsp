<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.web.TAController" %>
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
    List<TAController.JobAdviceView> jobAdviceList = (List<TAController.JobAdviceView>) request.getAttribute("jobAdviceList");
    Boolean pendingNewJobAnalysis = (Boolean) request.getAttribute("pendingNewJobAnalysis");
    Boolean profileNeedsConfirm = (Boolean) request.getAttribute("profileNeedsConfirm");
    Boolean manualAiRefresh = (Boolean) request.getAttribute("manualAiRefresh");
    Boolean profileInitialized = (Boolean) request.getAttribute("profileInitialized");
    Integer profileCompletion = (Integer) request.getAttribute("profileCompletion");
    String loginPromptHint = (String) request.getAttribute("loginPromptHint");
    if (current == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    if (jobAdviceList == null) jobAdviceList = List.of();
    boolean initialized = Boolean.TRUE.equals(profileInitialized);
    boolean needsConfirm = Boolean.TRUE.equals(profileNeedsConfirm);
    boolean startInEdit = !initialized || needsConfirm;
    int completion = profileCompletion == null ? 0 : profileCompletion;
    int bestFit = jobAdviceList.isEmpty() ? 0 : (int) Math.round(jobAdviceList.get(0).getMatch().getAiScore());
    long strongMatches = jobAdviceList.stream()
            .filter(item -> item != null && item.getMatch() != null && item.getMatch().getAiScore() >= 75)
            .count();
    String lastEvaluation = current.getProfile().getLastAiAdviceTime() > 0
            ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(current.getProfile().getLastAiAdviceTime()))
            : "Not evaluated yet";
    List<String> skillTags = current.getSkillTags() == null ? List.of() : current.getSkillTags();
    List<String> extractedSkills = current.getProfile().getExtractedSkills() == null ? List.of() : current.getProfile().getExtractedSkills();
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
    <% if (pendingNewJobAnalysis != null && pendingNewJobAnalysis) { %>
    <div class="alert alert-warning" id="newJobUpdateNotice">
        New jobs need refreshed fit analysis.
        <form method="post" action="<%= request.getContextPath() %>/ta/loginProfileDecision" style="display:inline-block;margin-left:8px;">
            <input type="hidden" name="decision" value="ai">
            <button type="submit" class="btn btn-small btn-secondary">Refresh Now</button>
        </form>
    </div>
    <% } %>
    <% if (needsConfirm) { %>
    <div class="alert alert-warning" id="materialConfirmNotice">Review the parsed profile, then save. Job recommendations will be recalculated after saving.</div>
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
                <span>Best job fit</span>
                <strong><%= bestFit %>%</strong>
            </div>
            <div class="talent-metric">
                <span>Strong matches</span>
                <strong><%= strongMatches %></strong>
            </div>
        </div>
    </section>

    <div id="profileWorkspace" class="talent-workspace <%= startInEdit ? "profile-edit-mode" : "profile-view-mode" %>">
        <section class="profile-preview-pane">
            <div class="profile-toolbar">
                <div>
                    <h3>Profile Preview</h3>
                    <p class="muted">Last fit evaluation: <%= h(lastEvaluation) %></p>
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
                        <div><span>CV</span><strong><%= current.getCvPath() == null || current.getCvPath().isBlank() ? "Missing" : "Uploaded" %></strong></div>
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
                    <p class="muted">Saving refreshes the fit score for every open TA position.</p>
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

        <aside class="job-recommendation-pane">
            <div class="profile-toolbar">
                <div>
                    <h3>Recommended TA Roles</h3>
                    <p class="muted">Ranked by current profile fit.</p>
                </div>
                <a class="btn btn-secondary" href="<%= request.getContextPath() %>/ta/jobs">Browse Jobs</a>
            </div>
            <% if (!jobAdviceList.isEmpty()) { %>
                <div class="recommendation-list">
                <% for (TAController.JobAdviceView item : jobAdviceList) {
                    int itemScore = (int) Math.round(item.getMatch().getAiScore());
                    String itemClass = itemScore >= 75 ? "fit-high" : (itemScore >= 45 ? "fit-mid" : "fit-low");
                    List<String> requiredSkills = item.getJob().getRequiredSkills() == null ? List.of() : item.getJob().getRequiredSkills();
                    List<String> gaps = item.getMatch().getAiGaps() == null ? List.of() : item.getMatch().getAiGaps();
                    if (gaps.isEmpty()) gaps = item.getMatch().getMissingSkills() == null ? List.of() : item.getMatch().getMissingSkills();
                %>
                    <article class="recommendation-card">
                        <div class="job-advice-head">
                            <h4><%= h(item.getJob().getCourseName() == null ? "Unnamed Position" : item.getJob().getCourseName()) %></h4>
                            <span class="fit-pill <%= itemClass %>"><%= itemScore %>%</span>
                        </div>
                        <span class="score-meter"><span class="<%= itemClass %>" style="width:<%= itemScore %>%"></span></span>
                        <div class="chip-wrap compact">
                            <% if (!requiredSkills.isEmpty()) {
                                for (String skill : requiredSkills) { %>
                            <span class="chip"><%= h(skill) %></span>
                            <% }} else { %>
                            <span class="muted">N/A</span>
                            <% } %>
                        </div>
                        <p><%= h(item.getMatch().getAiFitSummary() == null || item.getMatch().getAiFitSummary().isBlank() ? "Local fit based on required skills and your profile." : item.getMatch().getAiFitSummary()) %></p>
                        <div class="chip-wrap compact">
                            <% if (!gaps.isEmpty()) {
                                for (String gap : gaps) { %>
                            <span class="chip chip-gap"><%= h(gap) %></span>
                            <% }} else { %>
                            <span class="chip chip-success">No obvious gap</span>
                            <% } %>
                        </div>
                        <form method="post" action="<%= request.getContextPath() %>/ta/apply">
                            <input type="hidden" name="jobId" value="<%= h(item.getJob().getId()) %>"/>
                            <button type="submit" class="btn btn-small">Review Match</button>
                        </form>
                    </article>
                <% } %>
                </div>
            <% } else { %>
                <p class="empty-hint">No open positions. Recommendations will appear when instructors post jobs.</p>
            <% } %>
        </aside>
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
                showMask('Re-parsing CV and updating recommendations...');
            });
        }

        var manualAiRefresh = <%= (manualAiRefresh != null && manualAiRefresh) ? "true" : "false" %>;
        if (manualAiRefresh) {
            showMask('Refreshing job recommendations...');
            function pollRefresh(attempt) {
                fetch('<%= request.getContextPath() %>/ta/refreshNewJobsAi', {
                    method: 'POST',
                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                }).then(function (r) { return r.json(); }).then(function (data) {
                    if (data && data.inProgress && attempt < 8) {
                        setTimeout(function () { pollRefresh(attempt + 1); }, 2500);
                        return;
                    }
                    window.location.reload();
                }).catch(function () {
                    var loadingText = mask ? mask.querySelector('.ai-loading-box div:last-child') : null;
                    if (loadingText) loadingText.textContent = 'Refresh failed. Please reload and try again.';
                });
            }
            pollRefresh(0);
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
