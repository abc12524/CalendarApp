package com.example.calendarapp;

/**
 * 笔记数据模型
 */
public class Note {
    private long id;
    private String date;       // yyyy-MM-dd
    private String title;
    private String content;
    private String updatedAt;

    public Note() {}

    public Note(String date, String title, String content) {
        this.date = date;
        this.title = title;
        this.content = content;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    /** 是否有实际内容（标题或正文非空） */
    public boolean hasContent() {
        return (title != null && !title.trim().isEmpty())
            || (content != null && !content.trim().isEmpty());
    }
}
