import model.User;

import java.io.IOException;

public interface SpecificationInterface {

    void createFile(String filename, String path) throws IOException; // touch
    void createDirectory(String name, String path,Integer... restriction); // mkdir
    void createStorage(String name, String path, Long storageSize, String ... restriction); // ns
    // mkdir name numberOfDir, mkdir -res name restriction numberOfDir
    void createListOfDirectories(String filename, Integer numberOfDirectories, String path);
    void createListOfDirRestriction(String dirName, Integer restriction, Integer numberOfDirectories, String path);
    void createListOfFiles(String filename, Integer numberOfFiles, String path); // touch name number
    void createUser(String username, String password, Integer level, String path);

    void moveFile(String filename, String path, String currentPath); // move
    void editFile(String filename); //
    void downloadFile(String filename, String path); // download
    void deleteFile(String filename, String path); // rm

    void listFilesFromDirectory(String path, String... extension); // ls
    void sort(String path, String option, String ... name); // sort
    //acs-desc; ime,datum....

    boolean logIn(String username, String password, String path);
    void logOut();
    User getConnectedUser();

    boolean isStorage(String path);
}
