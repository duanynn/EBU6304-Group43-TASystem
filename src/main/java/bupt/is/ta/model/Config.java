package bupt.is.ta.model;

public class Config {
    private int maxCoursesPerTA = 2;
    private String cvRelativePath = "/WEB-INF/data/cvs";
    private String storageMode = "WEBAPP"; // or USER_HOME

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
}

