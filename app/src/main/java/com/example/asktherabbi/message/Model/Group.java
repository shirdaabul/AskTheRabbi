package com.example.asktherabbi.message.Model;

public class Group {
//    private String id;
    private String name;
    private String affiliation;

    public Group() {
    }

    public Group(String name) {
        this.name = name;
    }

    public Group(String id, String name, String affiliation) {
//        this.id = id;
        this.name = name;
        this.affiliation = affiliation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
}
