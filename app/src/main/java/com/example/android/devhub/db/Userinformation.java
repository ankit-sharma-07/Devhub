package com.example.android.devhub.db;

public class Userinformation {

    public String name;
    public String phoneno;
    public String desc,skills,linkedIn,github;

    public Userinformation(){
    }


    public Userinformation(String name, String phoneno, String desc, String skills, String linkedIn, String github){
        this.name = name;
        this.phoneno = phoneno;
        this.desc=desc;
        this.skills=skills;
        this.linkedIn=linkedIn;
        this.github=github;
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