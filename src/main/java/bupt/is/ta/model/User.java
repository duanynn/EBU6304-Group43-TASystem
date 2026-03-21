package bupt.is.ta.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    public enum Role {
        TA, MO, ADMIN
    }

    private String id;          // 学号或工号
    private String password;    // 简化起见，先用明文或简单 hash
    private Role role;
    private String name;
    private Double gpa;         // 仅 TA 使用
    private List<String> skillTags = new ArrayList<>();
    private String cvPath;      // 简历真实存储路径
    private String employeeId;  // MO 工号（如需要与 id 区分）

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
}

