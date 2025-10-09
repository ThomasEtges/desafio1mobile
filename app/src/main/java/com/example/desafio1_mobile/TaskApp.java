package com.example.desafio1_mobile;

public class TaskApp {
    private String title;
    private String description;

    private final String uid;

    private final Boolean completed;


    public TaskApp(String title, String description, String uid) {
        this.title = title;
        this.description = description;
        this.uid = uid;
        this.completed = false;
    }

    public String getTitle() {
        return title;
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
