public interface SpecificationInterface {

    void createFile(String filename, String directory);
    void createDirectory(String name,String storage);
    void createStorage(String path, Long storageSize);
    void createListOfDirectories();
    ////ls komanda u specifikaciji
    void createListOfFiles();
    void createUser(String username, String password, Integer level);

    void moveFile(String filename, String directory);

    void saveFile(String filename);
    void deleteFile(String filename, String path);
    void deleteDirectory(String directory);

    void listFilesFromDirectory(String directory);
    void sort(String directory, String option, String ... name);
    void filter(String directory, String extension);

    void downloadFile(String filename);

    void logIn(String username, String password);
    void logOut();

    void storageRestriction(String restriction, String ... rest);
    void directoryNumberOfFiles(Integer numberOfFiles);






}
