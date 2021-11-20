package model;

public class User {

    private String username;
    private String password;
    private Integer level;

    /**
     * Parameterized constructor.
     *
     * @param username
     * @param password
     * @param level Level represents privilege of a specific user.
     *      * There are 4 hierarchical levels:
     *      * 1 - superuser (has access to all methods)
     *      * 2 - does everything except creating new users
     *      * 3 - cannot move, download, copy and upload files and directories, cannot create new users
     *      * 4 - can just watch
     *      * User with every privilege can create new storage, and they automatically become
     *      * superuser of that storage with privilege level 1.
     */
    public User(String username, String password, Integer level) {
        this.username = username;
        this.password = password;
        this.level = level;
    }

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
