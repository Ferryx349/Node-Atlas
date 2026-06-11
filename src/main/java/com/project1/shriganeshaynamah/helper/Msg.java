package com.project1.shriganeshaynamah.helper;

public class Msg {
    private String content;
    private String type;

    public Msg() {
    }

    public Msg(String content, String type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("msg{");
        sb.append("content=").append(content);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
    

}
