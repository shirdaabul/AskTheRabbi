package com.example.asktherabbi;

public class TorahLesson {
    private String lessonID;
    private String userID;
    private String topic;
    private int max_Number_participants;
    private int currentNumberPerticipants;
    private String location;
    private String date;
    private String time;
    private String rabbi;
    private boolean isparticipation;

    public TorahLesson(String lessonID,String userID, String topic, int max_Number_participants, int currentNumberPerticipants, String location, String date, String hour, String rabbi, boolean isparticipation) {
        this.lessonID=lessonID;
        this.userID = userID;
        this.topic = topic;
        this.max_Number_participants = max_Number_participants;
        this.currentNumberPerticipants = currentNumberPerticipants;
        this.location = location;
        this.date = date;
        this.time = hour;
        this.rabbi = rabbi;
        this.isparticipation = isparticipation;
    }

    public TorahLesson() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getMax_Number_participants() {
        return max_Number_participants;
    }

    public void setMax_Number_participants(int max_Number_participants) {
        this.max_Number_participants = max_Number_participants;
    }

    public int getCurrentNumberPerticipants() {
        return currentNumberPerticipants;
    }

    public void setCurrentNumberPerticipants(int currentNumberPerticipants) {
        this.currentNumberPerticipants = currentNumberPerticipants;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRabbi() {
        return rabbi;
    }

    public void setRabbi(String rabbi) {
        this.rabbi = rabbi;
    }

    public boolean isIsparticipation() {
        return isparticipation;
    }

    public void setIsparticipation(boolean isparticipation) {
        this.isparticipation = isparticipation;
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

    public String getLessonID() {
        return lessonID;
    }

    public void setLessonID(String lessonID) {
        this.lessonID = lessonID;
    }
}