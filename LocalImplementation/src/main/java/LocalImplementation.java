import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;

public class LocalImplementation extends SpecificationClass implements SpecificationInterface {
    HashMap<String, Long> mapOfStorageSizes = new HashMap<>();
    HashMap<String, Integer> mapOfDirRestrictions = new HashMap<>();
    String os = File.separator;


    static {
        SpecificationManager.registerExporter(new LocalImplementation());
    }

    @Override
    public void createFile(String filename, String path) {
        File newFile = new File(path +os+ filename);

        try {
            Integer numberOfFilesLeft = mapOfDirRestrictions.get(path);
            if (numberOfFilesLeft > 0) {
                newFile.createNewFile();
                numberOfFilesLeft--;
                mapOfDirRestrictions.put(path,numberOfFilesLeft);
            } else System.out.println("Nema mesta");
            System.out.println(newFile.length());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createDirectory(String directoryName, String path, Integer... restriction) {
        //Integer numberOfFilesLeft = 0;
        if (restriction.length > 0) {
           mapOfDirRestrictions.put(path + os + directoryName, restriction[0]);
        }
        File newDir = new File(path +os+ directoryName);
        //try {
            if (mapOfDirRestrictions.get(path) != null) {
                Integer numberOfFilesLeft = mapOfDirRestrictions.get(path);
                if (numberOfFilesLeft > 0) {
                    newDir.mkdir();
                    numberOfFilesLeft--;
                    mapOfDirRestrictions.put(path,numberOfFilesLeft);
                } else System.out.println("Nema mesta");
            }
        //} catch (Exception e) {
          //  System.out.println("Mozemo da napravimo folder");
            else newDir.mkdir();
        //}
    }

    @Override
    public void createStorage(String name, String path, Long storageSize, String... restrictions) {
        mapOfStorageSizes.put(path,storageSize);
        File newDir = new File(path + name);
        newDir.mkdir();
    }

    @Override
    public void createListOfDirectories(String directoryName, Integer numberOfDirectories, String path, Integer... restriction) {
        for (int i = 0; i < numberOfDirectories; i++){
            createDirectory(directoryName + i, path,restriction[0]);
        }
    }

    @Override
    public void createListOfFiles(String filename, Integer numberOfFiles, String path) {
        for (int i = 0; i < numberOfFiles; i++){
            createFile(filename + i, path+os);
        }


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
    public void downloadFile(String filename, String path) {
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
        File file = new File(path + os + filename);
        file.delete();
        Integer numberOfFilesLeft = mapOfDirRestrictions.get(path);
        mapOfDirRestrictions.put(path, ++numberOfFilesLeft);
    }


    @Override
    public void listFilesFromDirectory(String path, String... extension) {
        File file = new File(path);
        File[] list = file.listFiles();
        if (list != null) {
            for (File f : list) {
                if (extension.length == 0) System.out.println(f.getName());
                else {
                    for (String extensionName : extension) {
                        if (f.getName().endsWith(extensionName))
                            System.out.println(f.getName());
                    }
                }

                BasicFileAttributes attrs = null;
                try {
                    attrs = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                    FileTime time = attrs.creationTime();
                    String pattern = "yyyy-MM-dd HH:mm:ss";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

                    String formatted = simpleDateFormat.format(new Date(time.toMillis()));

                    // System.out.println(  f.getName()+" je kreiran " + formatted );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //System.out.println(f); printuje celu putanju
            }
        }

    }

    @Override
    public void sort(String path, String option, String... name) {
        File file = new File(path);
        File[] list = file.listFiles();
        if (option.equals("desc"))
            Arrays.sort(list,Collections.reverseOrder());
        for (File files:list){
            System.out.println(files.getName());
        }
    }

    @Override
    public void editFile(String s) {

    }

    @Override
    public void logIn(String s, String s1) {

    }

    @Override
    public void logOut() {

    }

}
