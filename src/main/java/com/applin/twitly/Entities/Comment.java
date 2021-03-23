package com.applin.twitly.Entities;

public class Comment {

    private String commentid;
    private String comment;
    private String publisher;
    private String timestamp;

    public Comment(String commentid, String comment, String publisher, String timestamp) {
        this.commentid = commentid;
        this.comment = comment;
        this.publisher = publisher;
        this.timestamp = timestamp;
    }

    public Comment() {
    }

    public String getCommentid() {
        return commentid;
    }

    public void setCommentid(String commentid) {
        this.commentid = commentid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
