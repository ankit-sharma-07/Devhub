package com.example.android.devhub.db;

public class Userinformation {
    public String id;
    public String name, email;
    public String phoneno;
    public String desc, skills, linkedIn, github;

    public Userinformation() {
    }


    public Userinformation(String id, String name, String email, String phoneno, String desc, String skills, String linkedIn, String github) {
        this.id = id;
        this.name = name;
        this.phoneno = phoneno;
        this.desc = desc;
        this.skills = skills;
        this.linkedIn = linkedIn;
        this.github = github;
        this.email = email;
    }

    public String getUserName() {
        return name;
    }

    public String getUserPhoneno() {
        return phoneno;
    }

    public String getDesc() {
        return desc;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getSkills() {
        return skills;
    }

    public String getLinkedIn() {
        return linkedIn;
    }

    public String getGithub() {
        return github;
    }

}