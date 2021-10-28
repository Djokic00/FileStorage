public interface SpecificationInterface {

    void createFile(String filename, String directory);
    void createDirectory(String storage,String name);
    void createStorage(String path);
    void createListOfDirectories();
    ////ls komanda u specifikaciji
    void createListOfFiles();

    void moveFile(String filename);

    void saveFile(String filename);
    void deleteFile(String filename);
    void deleteDirectory(String directory);

    void listFilesFromDirectory(String directory);
    void sort(String directory, String option, String ... name);
    void filter(String directory, String extension);

    void downloadFile();

    void logIn();
    void logOut();


}
