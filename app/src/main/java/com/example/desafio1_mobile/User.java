package com.example.desafio1_mobile;

import android.text.BoringLayout;

public class User {
    private String uid;
    private String name;
    private final Boolean first_login;

    public User(String uid, String name) {
        this.uid = uid;
        this.name = name;
        this.first_login = true;
    }

    public Boolean getFirst_login() {
        return first_login;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
