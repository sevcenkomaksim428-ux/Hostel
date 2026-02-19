package org.example;

public class Employee {
    private int id;
    private String fullName;
    private String role;
    private int salary;
    private String login;
    private String password;

    public Employee(
            int id,
            String fullName,
            String role,
            int salary,
            String login,
            String password
    ) {
        this.id = id;
        this.fullName = fullName;
        this.role = role;
        this.salary = salary;
        this.login = login;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}