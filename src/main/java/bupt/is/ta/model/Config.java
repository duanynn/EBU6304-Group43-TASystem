package bupt.is.ta.model;

public class Config {
    private int maxCoursesPerTA = 2;
    private String cvRelativePath = "/WEB-INF/data/cvs";
    private String storageMode = "WEBAPP"; // or USER_HOME
    private String dashscopeApiKey = "";
    private String dashscopeEndpoint = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private String dashscopeModel = "qwen-plus";

    public int getMaxCoursesPerTA() {
        return maxCoursesPerTA;
    }

    public void setMaxCoursesPerTA(int maxCoursesPerTA) {
        this.maxCoursesPerTA = maxCoursesPerTA;
    }

    public String getCvRelativePath() {
        return cvRelativePath;
    }

    public void setCvRelativePath(String cvRelativePath) {
        this.cvRelativePath = cvRelativePath;
    }

    public String getStorageMode() {
        return storageMode;
    }

    public void setStorageMode(String storageMode) {
        this.storageMode = storageMode;
    }

    public String getDashscopeApiKey() {
        return dashscopeApiKey;
    }

    public void setDashscopeApiKey(String dashscopeApiKey) {
        this.dashscopeApiKey = dashscopeApiKey;
    }

    public String getDashscopeEndpoint() {
        return dashscopeEndpoint;
    }

    public void setDashscopeEndpoint(String dashscopeEndpoint) {
        this.dashscopeEndpoint = dashscopeEndpoint;
    }

    public String getDashscopeModel() {
        return dashscopeModel;
    }

    public void setDashscopeModel(String dashscopeModel) {
        this.dashscopeModel = dashscopeModel;
    }
}

