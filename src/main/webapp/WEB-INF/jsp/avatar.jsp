<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.service.AvatarService" %>
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
%>
<%
    User avatarUser = (User) request.getAttribute("avatarUser");
    if (avatarUser == null) {
        avatarUser = (User) session.getAttribute("currentUser");
    }
    String size = request.getParameter("size");
    if (size == null || size.isBlank()) {
        Object sizeAttr = request.getAttribute("avatarSize");
        size = sizeAttr == null ? "36" : String.valueOf(sizeAttr);
    }
    String extraClass = request.getParameter("class");
    if (extraClass == null || extraClass.isBlank()) {
        Object clsAttr = request.getAttribute("avatarClass");
        extraClass = clsAttr == null ? "" : String.valueOf(clsAttr);
    }
    AvatarService avatarService = new AvatarService();
    String url = avatarService.resolveDisplayUrl(avatarUser);
    String ctx = request.getContextPath();
    if (!url.startsWith("http") && !url.startsWith(ctx)) {
        url = ctx + url;
    }
%>
<img src="<%= h(url) %>" alt="" class="user-avatar <%= h(extraClass) %>" width="<%= h(size) %>" height="<%= h(size) %>" style="width:<%= h(size) %>px;height:<%= h(size) %>px;" loading="lazy">
