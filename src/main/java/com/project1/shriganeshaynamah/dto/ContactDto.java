package com.project1.shriganeshaynamah.dto;

import com.project1.shriganeshaynamah.user.Contact;

public class ContactDto {

    private int id;
    private String name;
    private String email;
    private String phone;
    private String work;
    private String nickname;
    private String description;
    private String category;
    private boolean favorite;
    private String imageUrl;

    public static ContactDto from(Contact contact) {
        ContactDto dto = new ContactDto();
        dto.id = contact.getCid();
        dto.name = contact.getName();
        dto.email = contact.getEmail();
        dto.phone = contact.getPhone();
        dto.work = contact.getWork();
        dto.nickname = contact.getNickname();
        dto.description = contact.getDescription();
        dto.category = contact.getCategory();
        dto.favorite = contact.isFavorite();
        dto.imageUrl = contact.getImage();
        return dto;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
