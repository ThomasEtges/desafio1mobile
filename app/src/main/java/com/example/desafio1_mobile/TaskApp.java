package com.example.desafio1_mobile;

import com.google.firebase.Timestamp;

public class TaskApp {
    private String title;
    private String description;

    private String uid;

    private Timestamp conclusion_date;

    private Boolean completed;


    public TaskApp(){}
    public TaskApp(String title, String description, Timestamp conclusion_date, String uid) {
        this.title = title;
        this.description = description;
        this.uid = uid;
        this.conclusion_date = conclusion_date;
        this.completed = false;
    }

    public String getTitle() {
        return title;
    }

    public Timestamp getConclusion_date() {
        return conclusion_date;
    }

    public void setConclusion_date(Timestamp conclusion_date) {
        this.conclusion_date = conclusion_date;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public String getUid() {
        return uid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
