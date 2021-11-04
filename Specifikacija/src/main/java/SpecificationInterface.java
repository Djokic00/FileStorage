public interface SpecificationInterface {

    void createFile(String filename, String path);
    void createDirectory(String name, String path,Integer... restriction);
    void createStorage(String name, String path, Long storageSize, String ... restriction);
    void createListOfDirectories(String filename, Integer numberOfDirectories, String path,Integer... restriction);
    ////ls komanda u specifikaciji
    void createListOfFiles(String filename, Integer numberOfFiles, String path);
    void createUser(String username, String password, Integer level, String path);

    void moveFile(String filename, String path, String currentPath);

    void downloadFile(String filename, String path);
    void deleteFile(String filename, String path);


    void listFilesFromDirectory(String path, String... extension);
    void sort(String path, String option, String ... name);
    //acs-desc; ime,datum....


    void editFile(String filename);

    void logIn(String username, String password);
    void logOut();







}
