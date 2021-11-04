import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class LocalImplementation extends SpecificationClass implements SpecificationInterface {
    HashMap<String, Long> mapOfStorageSizes = new HashMap<>();
    String os=File.separator;


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
    public void moveFile(String filename, String path, String currentpath) {
        try {
            Files.move(Paths.get(currentpath+filename) ,Paths.get(path+filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void saveFile(String filename, String path) {
        //moveFile(filename, System.getProperty("user.home")+os, path+os);
        try {
            Path newDir=Paths.get(System.getProperty("user.home"));
            Files.copy(Paths.get(path+os+filename), newDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Kopirao sam: "+filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public void listFilesFromDirectory(String path) {
        File file = new File(path);
        File[] list=file.listFiles();
        if (list != null) {
            for (File f : list) {

                System.out.println(f.getName());
                // System.out.println(f.length());

                BasicFileAttributes attrs = null;
                try {
                    attrs = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                    FileTime time = attrs.creationTime();
                    String pattern = "yyyy-MM-dd HH:mm:ss";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

                    String formatted = simpleDateFormat.format( new Date( time.toMillis() ) );

                    // System.out.println(  f.getName()+" je kreiran " + formatted );
                } catch (IOException e) {
                    e.printStackTrace();
                }


                //System.out.println(f); printuje celu putanju

            }
        }

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
