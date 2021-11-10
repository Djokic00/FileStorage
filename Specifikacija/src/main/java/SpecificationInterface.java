import model.FileStorage;
import model.User;
import java.io.IOException;

public interface SpecificationInterface {

    void createFile(String filename) throws IOException; // touch
    void createDirectory(String name,Integer... restriction); // mkdir
    void createStorage(String name, String path, Long storageSize, String ... restriction); // ns
    // mkdir name numberOfDir, mkdir -res name restriction numberOfDir
    void createListOfDirectories(String filename, Integer numberOfDirectories);
    void createListOfDirRestriction(String dirName, Integer restriction, Integer numberOfDirectories);
    void createListOfFiles(String filename, Integer numberOfFiles); // touch name number
    void createUser(String username, String password, Integer level);

    void moveFile(String filename, String path); // move
    void editFile(String filename); //
    boolean downloadFile(String filename); // download
    boolean uploadFile(String filename);
    boolean copyFile(String name, String newPath);
    void deleteFile(String filename); // rm
    boolean goForward(String filename);
    void goBackwards();

    void listFilesFromDirectory(String... extension); // ls
    void sort(String option, String ... name); // sort
    //acs-desc; ime,datum....

    boolean logIn(String username, String password);
    void logOut();
    User getConnectedUser();
    FileStorage getStorage();

    boolean isStorage(String path);
}
