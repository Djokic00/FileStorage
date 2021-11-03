import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class LocalImplementation extends SpecificationClass implements SpecificationInterface {
    HashMap<String, Long> mapOfStorageSizes = new HashMap<>();

    static {
        SpecificationManager.registerExporter(new LocalImplementation());
    }

    @Override
    public void createFile(String filename, String path) {
        File newFile = new File(path + filename);
        try {
            newFile.createNewFile();
            System.out.println(newFile.length());
            //System.out.println(Files.size(Path.of(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createDirectory(String directoryName, String path) {
        File newDir = new File(path + directoryName);
        newDir.mkdir();
    }

    @Override
    public void createStorage(String name, String path, Long storageSize, String... restrictions) {
        mapOfStorageSizes.put(path,storageSize);
        File newDir = new File(path + name);
        newDir.mkdir();
    }

    @Override
    public void createListOfDirectories() {

    }

    @Override
    public void createListOfFiles() {

    }

    @Override
    public void createUser(String s, String s1, Integer integer, String s2) {

    }

    @Override
    public void moveFile(String s, String s1, String s2) {

    }

    @Override
    public void saveFile(String s, String s1) {

    }


    @Override
    public void deleteFile(String filename, String path) {
        File file = new File(path + "/" + filename);
        file.delete();
    }

    @Override
    public void deleteDirectory(String s, String s1) {

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
    public void directoryNumberOfFiles(Integer integer, String s) {

    }

}
