public interface SpecificationInterface {

    void createFile(String filename, String path);
    void createDirectory(String name, String path);
    void createStorage(String name, String path, Long storageSize, String ... restriction);
    void createListOfDirectories();
    ////ls komanda u specifikaciji
    void createListOfFiles();
    void createUser(String username, String password, Integer level, String path);

    void moveFile(String filename, String path, String currentPath);

    void saveFile(String filename, String path);
    void deleteFile(String filename, String path);
    void deleteDirectory(String directory, String path);

    void listFilesFromDirectory(String path);
    void sort(String path, String option, String ... name);
    //acs-desc; ime,datum....
    void filter(String path, String extension);
    //.txt .json

    void downloadFile(String filename);

    void logIn(String username, String password);
    void logOut();
    void directoryNumberOfFiles(Integer numberOfFiles, String path);






}
