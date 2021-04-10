package com.github.bgabriel998.softwaredevproject;

/**
 * Item holding user attributes
 * used to fill friends list.
 */
public class FriendItem {
    private String uid;
    private String username;
    private int points;

    /**
     * Constructor
     * @param uid of user
     * @param username of user.
     * @param points user has.
     */
    public FriendItem(String uid, String username, int points) {
        this.uid = uid;
        this.username = username;
        this.points = points;
    }

    /**
     * Getter of the friend item username
     * @return username of the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter of the friend item user points
     * @return points of the user
     */
    public int getPoints() {
        return points;
    }

    /**
     * Check if a friend item has a specific username
     * @param username
     * @return true if the user has the target username, false otherwise
     */
    public boolean hasUsername(String username) {
        return this.username.equals(username);
    }
}