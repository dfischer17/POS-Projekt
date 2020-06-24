package com.example.cloudclient;

import com.google.api.client.util.DateTime;

import java.io.Serializable;

public class FileDetails implements Serializable {
    private String id;
    private String name;
    private Long size;
    private String mimeType;
    private DateTime createdDate;

    public FileDetails(String id, String name, Long size, String mimeType, DateTime createdDate) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.mimeType = mimeType;
        this.createdDate = createdDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }
}
