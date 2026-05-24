package bupt.is.ta.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    public enum Role {
        TA, MO, ADMIN
    }

    public enum AvatarType {
        DEFAULT, PRESET, UPLOAD
    }

    private String id;          // student ID or staff ID
    private String password;    // simplified: plain text or simple hash
    private Role role;
    private String name;
    private Double gpa;         // TA only
    private List<String> skillTags = new ArrayList<>();
    private String availableTime = ""; // human-readable summary of availableSlots
    private List<JobScheduleSlot> availableSlots = new ArrayList<>();
    private String cvPath;      // absolute CV storage path
    private String employeeId;  // MO staff ID (if different from id)
    private UserProfile profile = new UserProfile();
    private AvatarType avatarType = AvatarType.DEFAULT;
    private String avatarKey = "";
    private String idCardSuffix = ""; // TA only, for password recovery, never shown in UI
    private String college = "";      // MO only, teaching college/faculty

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

    public List<JobScheduleSlot> getAvailableSlots() {
        return availableSlots == null ? List.of() : availableSlots;
    }

    public void setAvailableSlots(List<JobScheduleSlot> availableSlots) {
        this.availableSlots = availableSlots == null ? new ArrayList<>() : new ArrayList<>(availableSlots);
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

    public AvatarType getAvatarType() {
        return avatarType == null ? AvatarType.DEFAULT : avatarType;
    }

    public void setAvatarType(AvatarType avatarType) {
        this.avatarType = avatarType == null ? AvatarType.DEFAULT : avatarType;
    }

    public String getAvatarKey() {
        return avatarKey == null ? "" : avatarKey;
    }

    public void setAvatarKey(String avatarKey) {
        this.avatarKey = avatarKey == null ? "" : avatarKey.trim();
    }

    public String getIdCardSuffix() {
        return idCardSuffix == null ? "" : idCardSuffix;
    }

    public void setIdCardSuffix(String idCardSuffix) {
        this.idCardSuffix = idCardSuffix == null ? "" : idCardSuffix.trim();
    }

    public String getCollege() {
        return college == null ? "" : college;
    }

    public void setCollege(String college) {
        this.college = college == null ? "" : college.trim();
    }
}

