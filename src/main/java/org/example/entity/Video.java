package org.example.entity;

public class Video {
    private final String title;
    private final String channel;
    private final String href;
    private final String date;
    private final String views;

    // Private constructor to restrict direct instantiation
    private Video(Builder builder) {
        this.title = builder.title;
        this.href = builder.href;
        this.channel = builder.channel;
        this.date = builder.date;
        this.views = builder.views;
    }

    // Static inner Builder class
    public static class Builder {
        private String title;
        private String href;
        private String channel;
        private String date;
        private String views;

        public Builder() {

        }
        public Builder setViews(String views){
            this.views = views;
            return this;
        }
        public Builder setHref(String href){
            this.href = href;
            return this;
        }
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setChannel(String channel) {
            this.channel = channel;
            return this;
        }

        public Builder setDate(String date) {
            this.date = date;
            return this;
        }

        public Video build() {
            return new Video(this);
        }
    }

    // Getters for the Video class fields
    public String getTitle() {
        return title;
    }

    public String getHref() {
        return href;
    }

    public String getViews(){
        return views;
    }
    public String getChannel() {
        return channel;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Video{" +
                "title='" + title + '\'' +
                ", channel='" + channel + '\'' +
                ", href='" + href + '\'' +
                ", date='" + date + '\'' +
                ", views='" + views + '\'' +
                '}';
    }
}