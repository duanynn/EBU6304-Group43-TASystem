package bupt.is.ta.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    public enum Role {
        TA, MO, ADMIN
    }

    private String id;          // student ID or staff ID
    private String password;    // simplified: plain text or simple hash
    private Role role;
    private String name;
    private Double gpa;         // TA only
    private List<String> skillTags = new ArrayList<>();
    private String availableTime = ""; // TA available time description
    private String cvPath;      // absolute CV storage path
    private String employeeId;  // MO staff ID (if different from id)
    private UserProfile profile = new UserProfile();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getGpa() {
        return gpa;
    }

    public void setGpa(Double gpa) {
        this.gpa = gpa;
    }

    public List<String> getSkillTags() {
        return skillTags;
    }

    public void setSkillTags(List<String> skillTags) {
        this.skillTags = skillTags;
    }

    public String getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime == null ? "" : availableTime.trim();
    }

    public String getCvPath() {
        return cvPath;
    }

    public void setCvPath(String cvPath) {
        this.cvPath = cvPath;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public UserProfile getProfile() {
        if (profile == null) {
            profile = new UserProfile();
        }
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }
}

