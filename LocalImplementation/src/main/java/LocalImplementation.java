import java.io.File;
import java.io.IOException;

public class LocalImplementation implements SpecificationInterface {

    @Override
    public void createFile(String filename, String path) {
        File newFile = new File(path + "/" + filename);
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createDirectory(String directoryName, String path) {
        File newDir = new File(path + "/" + directoryName);
        newDir.mkdir();
    }

    @Override
    public void createStorage(String s, Long aLong) {

    }

    @Override
    public void createListOfDirectories() {

    }

    @Override
    public void createListOfFiles() {

    }

    @Override
    public void createUser(String s, String s1, Integer integer) {

    }

    @Override
    public void moveFile(String s, String s1) {

    }

    @Override
    public void saveFile(String s) {

    }

    @Override
    public void deleteFile(String filename, String path) {
        File file = new File(path + "/" + filename);
        file.delete();
    }

    @Override
    public void deleteDirectory(String s) {

    }

    @Override
    public void listFilesFromDirectory(String s) {

    }

    @Override
    public void sort(String s, String s1, String... strings) {

    }

    @Override
    public void filter(String s, String s1) {

    }

    @Override
    public void downloadFile(String s) {

    }

    @Override
    public void logIn(String s, String s1) {

    }

    @Override
    public void logOut() {

    }

    @Override
    public void storageRestriction(String s, String... strings) {

    }

    @Override
    public void directoryNumberOfFiles(Integer integer) {

    }
}
