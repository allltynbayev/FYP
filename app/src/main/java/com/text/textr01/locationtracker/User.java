package com.text.textr01.locationtracker;

public class User {
    String name;
    String empid;
    String address;
    String phone;
    String email;
    String password;
    String status;

    public User()
    {

    }

    public User(String name, String empid, String address, String phone, String email, String password, String status) {
        this.name = name;
        this.empid = empid;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmpid() {
        return empid;
    }

    public void setEmpid(String empid) {
        this.empid = empid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
