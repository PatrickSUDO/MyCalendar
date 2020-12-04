package com.paihan.entities;

public class WorkItem {

    private String id;
    private String name;
    private String event;
    private String eventDate;
    private String date;
    private String description;


    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return this.date;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setEvent(String guide) {
        this.event = guide;
    }

    public String getEvent() {
        return this.event;
    }
}
