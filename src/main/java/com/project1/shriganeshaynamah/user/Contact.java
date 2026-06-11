package com.project1.shriganeshaynamah.user;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="contact1")
public class Contact {

      @Id
      @GeneratedValue(strategy=GenerationType.AUTO)
    private int cid;
    private String name;
    private String description;
    private String phone;
    private  String image;
    private String work;
    private String email;
    private String nickname;
    private String category = "Personal";
    private boolean favorite = false;
    @ManyToOne
    private User us;

  

    public Contact() {
    }

    public Contact(int cid, String description, String email, String image, String name, String nickname, String phone, User us, String work) {
        this.cid = cid;
        this.description = description;
        this.email = email;
        this.image = image;
        this.name = name;
        this.nickname = nickname;
        this.phone = phone;
        this.us = us;
        this.work = work;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public User getUs() {
        return us;
    }

    public void setUs(User us) {
        this.us = us;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    
    }
    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Contact{");
        sb.append("cid=").append(cid);
        sb.append(", name=").append(name);
        sb.append(", description=").append(description);
        sb.append(", phone=").append(phone);
        sb.append(", image=").append(image);
        sb.append(", work=").append(work);
        sb.append(", email=").append(email);
        sb.append(", nickname=").append(nickname);
       
        sb.append('}');
        return sb.toString();
    }
      @Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Contact contact = (Contact) obj;
    return cid == contact.getCid();
}

@Override
public int hashCode() {
    return Objects.hash(cid);
}

    
      
}
