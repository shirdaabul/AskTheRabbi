package com.example.asktherabbi.message.Model;

public class GroupChat {
    private String name;
    private String date;
    private String time;
    private String message;
    private String userid;
    private String type;

    public GroupChat() {
    }

    public GroupChat(String name, String date, String time, String message, String userid, String type) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.message = message;
        this.userid = userid;
        this.type = type;
    }

    public GroupChat(String name, String date, String time, String message, String userid) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.message = message;
        this.userid = userid;
    }

    public GroupChat(String name, String date, String time, String message) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
