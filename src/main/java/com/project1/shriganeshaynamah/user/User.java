package com.project1.shriganeshaynamah.user;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="user1")
public class User {

    @Id

    @GeneratedValue(strategy=GenerationType.AUTO)
     private int id;

      private String name;
   
      private String email;
      private String enable;
      private String imageurl;
      private String about;
      private String password;
      private String role;
    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", email=" + email + ", role=" + role + "]";
    }

      @OneToMany(mappedBy="us", cascade = CascadeType.ALL, orphanRemoval = true)
      List<Contact> cn=new ArrayList<>();


    public User() {
    }

    public User(String about, String email, String enable, int id, String imageurl, String name, String password, String role) {
        this.about = about;
        this.email = email;
        this.enable = enable;
        this.id = id;
        this.imageurl = imageurl;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Contact> getCn() {
        return cn;
    }

    public void setCn(List<Contact> cn) {
        this.cn = cn;
    }


    
}
