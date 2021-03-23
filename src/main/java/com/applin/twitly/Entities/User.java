package com.applin.twitly.Entities;

public class User {

    private String id;
    private String username;
    private String name;
    private String image;
    private String bio;

    public User(String id, String username, String name, String image, String bio) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.image = image;
        this.bio = bio;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}