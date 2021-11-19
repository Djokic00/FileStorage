import Exceptions.*;
import model.FileStorage;
import model.User;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public abstract class SpecificationClass {
    protected String fileName;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * This method creates a file with name filename.
     * @param filename
     * @throws UnauthorizedException
     * @throws IOException
     * @throws FolderException
     */
    abstract void createFile(String filename) throws UnauthorizedException, IOException, FolderException; // touch
    /**
     * Creates directory with a name and possible restrictions.
     * Restriction represents a summed number of files and folders that can be saved in created directory.
     * @param name
     * @param restriction
     * @throws StorageException
     * @throws FolderException
     * @throws UnauthorizedException
     */
    abstract void createDirectory(String name, Integer... restriction) throws StorageException, FolderException, UnauthorizedException; // mkdir
    /**
     * Creates Storage on forwarded absolute path, with given size limit and possible restrictions.
     * Restrictions of storage represent forbidden extensions of files in created storage.
     * This method also creates users.json and config.json files.
     * @param path
     * @param storageSize
     * @param restriction
     */
    abstract void createStorage(String path, Long storageSize, String ... restriction); // ns
    // mkdir name numberOfDir, mkdir -res name restriction numberOfDir
    /**
     * Creates more directories calling method createDirectory() for each one.
     * Name of directories are formatted like: dirname + i
     * where i is Integer variable going from 0 to numbewOfDirectories - 1
     */
    void createListOfDirectories(String dirName, Integer numOfDir, Integer level) throws
            UnauthorizedException, FolderException, StorageException {
        if (level < 4) {
            for (int i = 0; i < numOfDir; i++) {
                createDirectory(dirName + i);
            }
            if (numOfDir == 0) createDirectory(dirName + '0');

        } else throw new UnauthorizedException();
    };
    /**
     * Creates more directories with restriction calling method createDirectory() for each one.
     * Restriction represents a summed number of files and folders that can be saved in created directory.
     * Name of directories are formatted like: dirname + i
     * where i is Integer variable going from 0 to numbewOfDirectories - 1
     */
    void createListOfDirRestriction(String dirName, Integer restriction, Integer numOfDir, Integer level) throws
            UnauthorizedException, FolderException, StorageException {
        if (level < 4) {
            for (int i = 0; i < numOfDir; i++) {
                createDirectory(dirName + i, restriction);
            }
        }
        else throw new UnauthorizedException();
    }
    /**
     * Creates more files calling method createFile() for each one.
     * Name of directories are formatted like: filename + i
     * where i is Integer variable going from 0 to numberOfFiles - 1
     */
    void createListOfFiles(String filename, Integer numberOfFiles, Integer level) throws UnauthorizedException, FolderException, IOException {
        if (level < 4) {
            for (int i = 0; i < numberOfFiles; i++) {
                createFile(filename + i);
            }
        }
        else throw new UnauthorizedException();
    }
    /**
     * Creates new User which contains username, password and privilege.
     */
    abstract void createUser(String username, String password, Integer level) throws UnauthorizedException;
    /**
     * * Moves selected file to selected place with forwarded relative path.
     * @param filename
     * @param path
     * @throws CantChangeRootException
     * @throws UnauthorizedException
     */
    abstract void moveFile(String filename, String path) throws CantChangeRootException, UnauthorizedException; // move

    /**
     * With this method user can edit selected file.
     */
    abstract void editFile(String filename);
    /**
     * Downloads selected file to special folder StorageDownloads on "users.home" path.
     * @param filename
     * @return returns true if file is successfully downloaded, false otherwise
     * @throws UnauthorizedException
     */
    abstract boolean downloadFile(String filename) throws UnauthorizedException; // download
    /**
     * Uploads selected file from local System to a storage where user is logged-in.
     * @param filename
     * @return returns true if file is successfully uploaded, false otherwise
     * @throws CantChangeRootException
     * @throws UnauthorizedException
     * @throws StorageException
     */
    abstract boolean uploadFile(String filename) throws CantChangeRootException, UnauthorizedException, StorageException;
    /**
     * Copy files within storage. Forwarded are name and relative path to new locatiion.
     * @param name
     * @param newPath
     * @return returns true if file is successfully copied, false otherwise
     * @throws CantChangeRootException
     * @throws UnauthorizedException
     * @throws StorageException
     */
    abstract boolean copyFile(String name, String newPath) throws CantChangeRootException, UnauthorizedException, StorageException;
    /**
     * Deletes selected file from a storage.
     * @param filename
     * @throws FileNotFoundException
     * @throws UnauthorizedException
     */
    abstract void deleteFile(String filename) throws FileNotFoundException, UnauthorizedException; // rm
    /**
     * Da li ove dve metode treba da stoje u specifikaciji? mi smo ih napravili specijalno za komandnu
     * liniju, da li onda uopste treba da budu u implementaciji????
     */
    abstract boolean goForward(String filename);
    abstract void goBackwards();
    /**
     * Returns list (of names) of files and directories saved on current path.
     * If extension exists, then method returns files with wanted extension.
     * Instead of extension any string can be forwarded.
     * @param extension
     * @return
     */
    abstract List<String> listFilesFromDirectory(String... extension); // ls
    /**
     * Returns list (of names) of files and directories saved on currentpath
     * in ascending or descending order.
     * Option of sorting can be date or size.
     */
    abstract List<String> sort(String order, String ... option); // sort

    /**
     * Returns true if user is logged-in. Thus checks by connectedUser field.
     */
    abstract boolean logIn(String username, String password) throws UnauthorizedException;
    /**
     * Sets connectedUser to null. Thus, user is logged-out.
     */
    abstract void logOut();
    /**
     * Returns connectedUser object.
     * @return
     */
    abstract User getConnectedUser();
    /**
     * Return fileStorage object.
     */
    abstract FileStorage getStorage();
    /**
     * Reads information about restriction ........
     */
    abstract void readConfig(String path);
    abstract boolean isStorage(String path);
    abstract void writeToJsonFile(String filename, Object object);
}
