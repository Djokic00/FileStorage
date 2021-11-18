package model;

import java.io.File;
import java.util.HashMap;

public class FileStorage {
   // Boolean connection = false;
    String restriction;
    String storagePath;
    String currentPath;
    Long size;
    String storagename;
    HashMap<String, Integer> folderRestrictions;

    public FileStorage(Long size) {
        this.size = size;
    }

    public FileStorage(String storagePath, String storagename) {
        this.storagePath = storagePath;
        this.storagename = storagename;
    }

    public FileStorage(Long size, String restriction) {
        this.size = size;
        this.restriction = restriction;
    }

    public FileStorage(Long size, String restriction, HashMap<String, Integer> folderRestrictions) {
        this.size = size;
        this.restriction = restriction;
        this.folderRestrictions = folderRestrictions;
    }

    public FileStorage() {}

    public void setFolderRestrictions(HashMap<String, Integer> folderRestrictions) {
        this.folderRestrictions = folderRestrictions;
    }

    public HashMap<String, Integer> getFolderRestrictions() {
        return folderRestrictions;
    }

    public String getRestriction() {
        return restriction;
    }

    public void setRestriction(String restriction) {
        this.restriction = restriction;
    }

//    public Boolean getConnection() {
//        return connection;
//    }
//
//    public void setConnection(Boolean connection) {
//        this.connection = connection;
//    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getStoragename() {
        return storagename;
    }

    public void setStoragename(String storagename) {
        this.storagename = storagename;
    }
}
