package com.example.demo.model;

public class PostSearchCriteria {
    private String topic;
    private String region;
    private String keyword;
    private int days;
    private int minLikes;
    private int minFavorites;
    private String orderBy;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getMinLikes() {
        return minLikes;
    }

    public void setMinLikes(int minLikes) {
        this.minLikes = minLikes;
    }

    public int getMinFavorites() {
        return minFavorites;
    }

    public void setMinFavorites(int minFavorites) {
        this.minFavorites = minFavorites;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public boolean isEmpty() {
        return isBlank(topic) && isBlank(region) && isBlank(keyword) && days <= 0 && minLikes <= 0 && minFavorites <= 0;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
