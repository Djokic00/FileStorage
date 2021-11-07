package model;

public class User {

    private String username;
    private String password;
    private Integer level;

    public User(String username, String password, Integer level) {
        this.username = username;
        this.password = password;
        this.level = level;
    }

    //nivo 1
    //nivo 2
    //nivo 3

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer nivo) {
        this.level = level;
    }
}
