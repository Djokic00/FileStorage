import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.FileStorage;
import model.User;

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

public class LocalImplementation extends SpecificationClass implements SpecificationInterface {
    HashMap<String, Long> mapOfStorageSizes = new HashMap<>();
    HashMap<String, Integer> mapOfDirRestrictions = new HashMap<>();
    StringBuilder jsonString = new StringBuilder();
    File users;
    File config;
    String osSeparator = File.separator;
    User connectedUser;

    static {
        SpecificationManager.registerExporter(new LocalImplementation());
    }

    @Override
    public void createFile(String filename, String path) throws IOException {
        File newFile = new File(path + osSeparator + filename);
        /*
            Restrikcije za skladiste => filename.endsWith("restrikcija") (npr .exe) onda
            ispisujemo ne mozete dodati fajl u skladiste zbog exe ekstenzije. U suprotnom nastavljamo
            sa kodom tj funkcijom
            A posle moramo da proverimo kad napravimo falj da li moze da se skladisti zbog velicine
        */

        if (mapOfDirRestrictions.containsKey(path) == true) {
            Integer numberOfFilesLeft = mapOfDirRestrictions.get(path);
            if (numberOfFilesLeft > 0) {
                newFile.createNewFile();
                numberOfFilesLeft--;
                mapOfDirRestrictions.put(path, numberOfFilesLeft);
            }
            else System.out.println("Folder is full!");
        }
        else {
            newFile.createNewFile();
        }
    }

    @Override
    public void createDirectory(String directoryName, String path, Integer... restriction) {
        if (restriction.length > 0) {
           mapOfDirRestrictions.put(path + osSeparator + directoryName, restriction[0]);
        }
        File newDir = new File(path + osSeparator + directoryName);

        if (mapOfDirRestrictions.containsKey(path) == true) {
            Integer numberOfFilesLeft = mapOfDirRestrictions.get(path);
            if (numberOfFilesLeft > 0) {
                newDir.mkdir();
                mapOfDirRestrictions.put(path, --numberOfFilesLeft);
            } else System.out.println("Nema mesta");
        }
        else {
            newDir.mkdir();
            System.out.println("Ulazi ovde");
        }
    }

    @Override
    public void createStorage(String name, String path, Long storageSize, String... restrictions) {
        jsonString = new StringBuilder();
        mapOfStorageSizes.put(path,storageSize);
        System.out.println(path+name);
        File storageFile = new File(path + name);
        storageFile.mkdir();
        String rootDirPath = path + name + osSeparator + "rootDirectory";
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
        for (int i = 0; i < numberOfDirectories; i++) {
            createDirectory(dirName + i, path);
        }
        if (numberOfDirectories == 0) createDirectory(dirName + '0', path);
    }
    @Override
    public void createListOfDirRestriction(String dirName, Integer restriction, Integer numberOfDirectories, String path) {
        for (int i = 0; i < numberOfDirectories; i++) {
            createDirectory(dirName + i, path, restriction);
        }
    }

    @Override
    public void createListOfFiles(String filename, Integer numberOfFiles, String path) {
        for (int i = 0; i < numberOfFiles; i++) {
            try {
                createFile(filename + i, path + osSeparator);
            } catch (IOException e) {
                System.out.println("Error: File not created");
            }
        }
    }

    @Override
    public void createUser(String username, String password, Integer level, String path) {
        try {
            Gson gson = new Gson();
            User user = new User(username, password, level);
            if (new File(path + osSeparator + "users.json").length() == 0) {
                FileWriter file = new FileWriter(path + osSeparator + "users.json");
                System.out.println(path + osSeparator + "users.json");
                System.out.println("Ulazi");
                jsonString.append("[");
                jsonString.append(gson.toJson(user));
                jsonString.append("]");
                file.write(String.valueOf(jsonString));
                file.close();
            }
            else {
                BufferedReader reader = new BufferedReader(new FileReader(path + osSeparator + "users.json"));
                System.out.println(path + osSeparator + "users.json");
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
            Path newDir = Paths.get(System.getProperty("user.home"));
            Files.copy(Paths.get(path + osSeparator + filename), newDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Kopirao sam: " + filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void deleteFile(String filename, String path) {
        File file = new File(path + osSeparator + filename);
        if (file.isDirectory()) {
            deleteDirectory(file);
        }
        boolean deleted = file.delete();
        if (deleted == false) System.out.println("File is not in this folder");

        if (mapOfDirRestrictions.containsKey(path) == true) {
            Integer numberOfFilesLeft = mapOfDirRestrictions.get(path);
            mapOfDirRestrictions.put(path, ++numberOfFilesLeft);
        }
    }

    public void deleteDirectory(File file) {
        for (File subfile : file.listFiles()) {
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }
            subfile.delete();
        }
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
        if (option.equals("asc")) {
            Arrays.sort(list);
        }
        else if (option.equals("desc"))
            Arrays.sort(list,Collections.reverseOrder());
        for (File files : list){
            System.out.println(files.getName());
        }
    }

    @Override
    public void editFile(String s) {

    }

    @Override
    public boolean logIn(String username, String password, String path) {

        if (new File(path + osSeparator + "users.json").length() == 0) {
            System.out.println("Null sam");
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
                System.out.println("Number of users: " + userArray.size());
                for (User user: userArray) {
                    if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                        connectedUser = new User(username, password, user.getNivo());
                        System.out.println("Connected: " + user.getNivo());
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
}
