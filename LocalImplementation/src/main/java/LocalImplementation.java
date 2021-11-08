import com.fasterxml.jackson.dataformat.xml.XmlAnnotationIntrospector;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.FileStorage;
import model.User;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class LocalImplementation extends SpecificationClass implements SpecificationInterface {
    HashMap<String, Long> mapOfStorageSizes = new HashMap<>();
    HashMap<String, Integer> mapOfDirRestrictions = new HashMap<>();
    StringBuilder jsonString = new StringBuilder();
    File users;
    File config;
    String osSeparator = File.separator;
    User connectedUser;
    FileStorage storage=new FileStorage();

    static {
        SpecificationManager.registerExporter(new LocalImplementation());
    }

    @Override
    public void createFile(String filename, String path) throws IOException {
        if (connectedUser.getLevel() < 4) {
            File newFile = new File(path + osSeparator + filename);

            // Ovo radi samo za jedno skladiste jer ne izvlaci podatke iz config.json
            if (storage.getRestriction() != null) {
                if (filename.endsWith(storage.getRestriction())) {
                    System.out.println("You cannot make file with " + storage.getRestriction() + " extension");
                    return;
                }
            }

            if (mapOfDirRestrictions.containsKey(path) == true) {
                Integer numberOfFilesLeft = mapOfDirRestrictions.get(path);
                if (numberOfFilesLeft > 0) {
                    newFile.createNewFile();
                    numberOfFilesLeft--;
                    mapOfDirRestrictions.put(path, numberOfFilesLeft);
                } else System.out.println("Folder is full!");
            } else {
                newFile.createNewFile();
            }
        } else unauthorizedAction();
    }

    @Override
    public void createDirectory(String directoryName, String path, Integer... restriction) {
        if (connectedUser.getLevel() < 4) {
            if (restriction.length > 0) {
                mapOfDirRestrictions.put(path + osSeparator + directoryName, restriction[0]);
            }
            File newDir = new File(path + osSeparator + directoryName);

            if (mapOfDirRestrictions.containsKey(path) == true) {
                Integer numberOfFilesLeft = mapOfDirRestrictions.get(path);
                if (numberOfFilesLeft > 0) {
                    newDir.mkdir();
                    mapOfDirRestrictions.put(path, --numberOfFilesLeft);
                } else System.out.println("Folder is full!");
            } else {
                newDir.mkdir();
            }
        } else unauthorizedAction();
    }

    @Override
    public void createStorage(String name, String path, Long storageSize, String... restrictions) {
        if (restrictions.length > 0) storage.setRestriction(restrictions[0]);

        jsonString = new StringBuilder();
        mapOfStorageSizes.put(path, storageSize);
        System.out.println("Storagepath: "+ path + osSeparator + name);

        File storageFile = new File(path +osSeparator+ name);
        storageFile.mkdir();
        String rootDirPath = path + osSeparator + name + osSeparator + "rootDirectory";
        File rootDirectory = new File(rootDirPath);
        rootDirectory.mkdir();
        users = new File(rootDirectory + osSeparator + "users.json");
        config = new File(rootDirectory + osSeparator + "config.json");
        try {
            users.createNewFile();
            config.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createListOfDirectories(String dirName, Integer numberOfDirectories, String path) {
        if (connectedUser.getLevel() < 4) {
            for (int i = 0; i < numberOfDirectories; i++) {
                createDirectory(dirName + i, path);
            }
            if (numberOfDirectories == 0) createDirectory(dirName + '0', path);

        }else unauthorizedAction();
    }
    @Override
    public void createListOfDirRestriction(String dirName, Integer restriction, Integer numberOfDirectories, String path) {
        if (connectedUser.getLevel()<4) {
        for (int i = 0; i < numberOfDirectories; i++) {
            createDirectory(dirName + i, path, restriction);
        }
        }else unauthorizedAction();
    }

    @Override
    public void createListOfFiles(String filename, Integer numberOfFiles, String path) {
        if (connectedUser.getLevel()<4) {
            for (int i = 0; i < numberOfFiles; i++) {
                try {
                    createFile(filename + i, path + osSeparator);
                } catch (IOException e) {
                    System.out.println("Error: File not created");
                }
            }
        } else unauthorizedAction();
    }

    @Override
    public void createUser(String username, String password, Integer level, String path) {
        if ((connectedUser == null) || connectedUser.getLevel() == 1) {
            try {
                Gson gson = new Gson();
                User user = new User(username, password, level);
                if (new File(path + osSeparator + "users.json").length() == 0) {
                    FileWriter file = new FileWriter(path + osSeparator + "users.json");
                    System.out.println("Creating users.json file on path: " + path + osSeparator + "users.json");
                    jsonString.append("[");
                    jsonString.append(gson.toJson(user));
                    jsonString.append("]");
                    file.write(String.valueOf(jsonString));
                    file.close();
                }
                else {
                    BufferedReader reader = new BufferedReader(new FileReader(path + osSeparator + "users.json"));
                    System.out.println("Creating users.json file on path: " + path + osSeparator + "users.json");
                    jsonString = new StringBuilder(reader.readLine());
                    jsonString.deleteCharAt(jsonString.length() - 1);
                    jsonString.append(",");
                    jsonString.append(gson.toJson(user));
                    jsonString.append("]");
                    System.out.println(jsonString);
                    FileWriter file = new FileWriter(path + osSeparator + "users.json");
                    file.write(String.valueOf(jsonString));
                    file.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }  else unauthorizedAction();
    }

    @Override
    public void moveFile(String filename, String path, String currentpath) {
        if (connectedUser.getLevel()<3) {
            try {
                Files.move(Paths.get(currentpath + osSeparator+filename), Paths.get(path + filename), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {

            }
        }else unauthorizedAction();
    }

    @Override
    public void downloadFile(String filename, String path) {
        if (connectedUser.getLevel()<3) {
            Path downloadDir=Paths.get(System.getProperty("user.home")+osSeparator+"StorageDownloads");
            if (!Files.exists(downloadDir)){
                createDirectory("StorageDownloads",System.getProperty("user.home"));
            }
            try {
                Files.copy(Paths.get(path + osSeparator + filename), downloadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Download completed: " + filename);

            } catch (IOException e) {
                    e.printStackTrace();
            }
        } else unauthorizedAction();
    }

    @Override
    public void deleteFile(String filename, String path) {
        if (connectedUser.getLevel()<4) {
            File file = new File(path + osSeparator + filename);
            if (file.isDirectory()) {
                deleteDirectory(file);
            }
            boolean deleted = file.delete();
            if (deleted == false) System.out.println("File is not in this folder.");

            if (mapOfDirRestrictions.containsKey(path) == true) {
                Integer numberOfFilesLeft = mapOfDirRestrictions.get(path);
                mapOfDirRestrictions.put(path, ++numberOfFilesLeft);
            }
        } else unauthorizedAction();
    }


    public void deleteDirectory(File file) {
        if (connectedUser.getLevel() < 4) {
            for (File subfile : file.listFiles()) {
                if (subfile.isDirectory()) {
                    deleteDirectory(subfile);
                }
                subfile.delete();
            }
        } else unauthorizedAction();
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
            }
        }
    }

    @Override
    public void sort(String path, String order, String ... option) {
        File file = new File(path);
        File[] list = file.listFiles();
        if (option.length == 0) {
            if (order.equals("asc")) {
                Arrays.sort(list);
            }
            else if (order.equals("desc")) {
                Arrays.sort(list, Collections.reverseOrder());
            }
        }
        else {
            if (option[0].equals("date")) {
                if (order.equals("asc")) {
                    Arrays.sort(list, Comparator.comparingLong(File::lastModified));
                }
                else if (order.equals("desc")) {
                    Arrays.sort(list, Comparator.comparingLong(File::lastModified).reversed());
                }
            }
            else if (option[0].equals("size")) {
                HashMap<String, Long> mapOfFiles = new HashMap<>();
                for (File f : list) {
                    if (f.isDirectory()) mapOfFiles.put(f.getName(), FileUtils.sizeOfDirectory(f));
                    else mapOfFiles.put(f.getName(), f.length());
                }
                if (order.equals("asc")) {
                    mapOfFiles.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByValue())
                            .forEach(System.out::println);
                }
                else if (order.equals("desc")) {
                    mapOfFiles.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                            .forEach(System.out::println);
                }
            }
        }
    }

    @Override
    public void editFile(String filePath) {
        File file = new File(filePath);
        if (connectedUser.getLevel() < 4) file.setWritable(true);
        else file.setWritable(false);

        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean logIn(String username, String password, String path) {

        if (new File(path + osSeparator + "users.json").length() == 0) {
            //System.out.println("Users.json is empty.");
            createUser(username, password, 1, path);
            connectedUser = new User(username,password,1);
            return true;
        }
        else {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                BufferedReader reader = new BufferedReader(new FileReader(
                        path + osSeparator + "users.json"));
                Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
                ArrayList<User> userArray = gson.fromJson(reader,userListType);
                for (User user: userArray) {
                    if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                        connectedUser = new User(username, password, user.getLevel());
                        System.out.println("Connected user: " + user.getUsername() + "; Privilege: " + user.getLevel());
                        return true;
                    }
                }
                reader.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void logOut() {
        connectedUser = null;
        System.out.println("Log out");
    }

    @Override
    public boolean isStorage(String currentPath) {
        String users = currentPath + osSeparator + "rootDirectory" + osSeparator + "users.json";
        String config = currentPath + osSeparator + "rootDirectory" + osSeparator + "config.json";
        Path pathToUsers = Paths.get(users);
        Path pathToConfig = Paths.get(config);
        if (Files.exists(pathToUsers) && Files.exists(pathToConfig)) return true;
        else return false;
    }

    @Override
    public User getConnectedUser() {
        return connectedUser;
    }

    @Override
    public FileStorage getStorage() {
        return storage;
    }

    public void unauthorizedAction(){
        System.out.println("Unauthorized action. Level: " + connectedUser.getLevel());
    }
}
