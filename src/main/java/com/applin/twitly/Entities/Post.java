package com.applin.twitly.Entities;

public class Post {

    private String postid;
    private String postcontent;
    private String postimage;
    private String publisher;
    private String timestamp;
    private String postdate;
    private String postquery;

    public Post(String postid, String postcontent, String postimage, String publisher, String timestamp, String postdate, String postquery) {
        this.postid = postid;
        this.postcontent = postcontent;
        this.postimage = postimage;
        this.publisher = publisher;
        this.timestamp = timestamp;
        this.postdate = postdate;
        this.postquery = postquery;
    }

    public Post() {
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPostcontent() {
        return postcontent;
    }

    public void setPostcontent(String postcontent) {
        this.postcontent = postcontent;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
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

    public String getPostdate() {
        return postdate;
    }

    public void setPostdate(String postdate) {
        this.postdate = postdate;
    }

    public String getPostquery() {
        return postquery;
    }

    public void setPostquery(String postquery) {
        this.postquery = postquery;
    }
}
