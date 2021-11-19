import Exceptions.UnauthorizedActionException;
import model.FileStorage;
import model.User;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface SpecificationInterface {


    /**
     * This method creates a file with name filename.
     */
    void createFile(String filename) throws UnauthorizedActionException,IOException; // touch
    /**
     * Creates directory with a name and possible restrictions.
     * Restriction represents a summed number of files and folders that can be saved in created directory.
     */
    void createDirectory(String name,Integer... restriction); // mkdir
    /**
     * Creates Storage on forwarded absolute path, with given size limit and possible restrictions.
     * Restrictions of storage represent forbidden extensions of files in created storage.
     * This method also creates users.json and config.json files.
     */
    void createStorage(String path, Long storageSize, String ... restriction); // ns
    // mkdir name numberOfDir, mkdir -res name restriction numberOfDir
    /**
     * Creates more directories calling method createDirectory() for each one.
     * Name of directories are formatted like: dirname + i
     * where i is Integer variable going from 0 to numbewOfDirectories - 1
     */
    void createListOfDirectories(String dirname, Integer numberOfDirectories);
    /**
     * Creates more directories with restriction calling method createDirectory() for each one.
     * Restriction represents a summed number of files and folders that can be saved in created directory.
     * Name of directories are formatted like: dirname + i
     * where i is Integer variable going from 0 to numbewOfDirectories - 1
     */
    void createListOfDirRestriction(String dirName, Integer restriction, Integer numberOfDirectories);
    /**
     * Creates more files calling method createFile() for each one.
     * Name of directories are formatted like: filename + i
     * where i is Integer variable going from 0 to numbewOfFiles - 1
     */
    void createListOfFiles(String filename, Integer numberOfFiles); // touch name number
    /**
     * Creates new User which contains username, password and privilege.
     */
    void createUser(String username, String password, Integer level);

    /**
     * Moves selected file to selected place with forwarded relative path.
     */
    void moveFile(String filename, String path); // move

    /**
     * With this method user can edit selected selected file.
     */
    void editFile(String filename); //

    /**
     * Downloads selected file to special folder StorageDownloads on "users.home" path.
     */
    boolean downloadFile(String filename); // download
    /**
     * Uploads selected file from local System to a storage where user is logged-in.
     */
    boolean uploadFile(String filename);
    /**
     * Copy files within storage. Forwarded are name and relative path to new locatiion.
     */
    boolean copyFile(String name, String newPath);
    /**
     * Deletes selected file from a storage.
     */
    void deleteFile(String filename); // rm
    /**
     * Da li ove dve metode treba da stoje u specifikaciji? mi smo ih napravili specijalno za komandnu
     * liniju, da li onda uopste treba da budu u implementaciji??????
     */
    boolean goForward(String filename);
    void goBackwards();
    /**
     * Returns list (of names) of files and directories saved on currentpath.
     * If extension exists, then method returns files with wanted extension.
     * Instead of extension any string can be forwarded.
     */
    List<String> listFilesFromDirectory(String... extension); // ls
    /**
     * Returns list (of names) of files and directories saved on currentpath
     * in ascending or descending order.
     * Option of sorting can be date or size.
     */
    List<String> sort(String order, String ... option); // sort
    //acs-desc; ime,datum....

    /**
     * Returns true if user is logged-in. Thus checks by connectedUser field.
     */
    boolean logIn(String username, String password);
    /**
     * Sets connectedUser to null. Thus user is logged-out.
     */
    void logOut();
    /**
     * Returns connectedUser object.
     */
    User getConnectedUser();
    /**
     * Return fileStorage object.
     */
    FileStorage getStorage();
    /**
     * Reads information about restriction ........
     */
    void readConfig(String path);
    boolean isStorage(String path);
    void writeToJsonFile(String filename, Object object);
}
