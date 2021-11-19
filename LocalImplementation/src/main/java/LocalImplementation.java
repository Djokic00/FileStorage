import Exceptions.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.FileStorage;
import model.User;
import org.apache.commons.io.FileUtils;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class LocalImplementation extends SpecificationClass {
    private HashMap<String, Integer> mapOfDirRestrictions = new HashMap<>();
    private StringBuilder jsonForUser = new StringBuilder();
    private StringBuilder jsonForStorage = new StringBuilder();
    private File users, config;
    private String osSeparator = File.separator;
    private User connectedUser;
    private FileStorage fileStorage = new FileStorage();


    static {
        SpecificationManager.registerExporter(new LocalImplementation());
    }

    public LocalImplementation(){}

    @Override
    public void createFile(String filename) throws UnauthorizedException, IOException, FolderException {
        if (connectedUser.getLevel() < 4) {
            File newFile = new File(fileStorage.getCurrentPath() + osSeparator + filename);
            if (fileStorage.getRestriction() != null) {
                if (filename.endsWith(fileStorage.getRestriction())) {
                    //System.out.println("You cannot make file with " + fileStorage.getRestriction() + " extension");
                    return;
                }
            }
            if (mapOfDirRestrictions.containsKey(fileStorage.getCurrentPath()) == true) {
                Integer numberOfFilesLeft = mapOfDirRestrictions.get(fileStorage.getCurrentPath());
                if (numberOfFilesLeft > 0) {
                    newFile.createNewFile();
                    mapOfDirRestrictions.put(fileStorage.getCurrentPath(), --numberOfFilesLeft);
                }
                else throw new FolderException();
            } else {
                newFile.createNewFile();
            }
        } else throw new UnauthorizedException();
    }

    @Override
    public void createDirectory(String directoryName, Integer... restriction)
            throws StorageException, FolderException, UnauthorizedException {
        if (connectedUser.getLevel() < 4) {
            if (restriction.length > 0) {
                mapOfDirRestrictions.put(getStorage().getCurrentPath() + osSeparator + directoryName, restriction[0]);
                fileStorage.setFolderRestrictions(mapOfDirRestrictions);
            }
            File newDir = new File(fileStorage.getCurrentPath() + osSeparator + directoryName);

            if (mapOfDirRestrictions.containsKey(fileStorage.getCurrentPath()) == true) {
                Integer numberOfFilesLeft = mapOfDirRestrictions.get(fileStorage.getCurrentPath());
                if (numberOfFilesLeft > 0) {
                    if (fileStorage.getSize() - 4096 > 0) {
                        newDir.mkdir();
                        mapOfDirRestrictions.put(fileStorage.getCurrentPath(), --numberOfFilesLeft);
                        fileStorage.setSize(fileStorage.getSize() - 4096);
                    }
                    else throw new StorageException();
                }
                else throw new FolderException();
            } else {
                if (fileStorage.getSize() - 4096 > 0) {
                    newDir.mkdir();
                    fileStorage.setSize(fileStorage.getSize() - 4096);
                }
                else throw new StorageException();
            }
        } else throw new UnauthorizedException();
    }

    @Override
    public void createStorage(String path, Long storageSize, String... restrictions) {
        if (restrictions.length > 0) {
            fileStorage = new FileStorage(path, storageSize, restrictions[0]);
        }
        else fileStorage = new FileStorage(path, storageSize);

        jsonForUser = new StringBuilder();
        fileStorage.setStoragePath(path);

        File storageFile = new File(path);
        storageFile.mkdir();
        String rootDirPath = fileStorage.getStoragePath() + osSeparator + "rootDirectory";
        File rootDirectory = new File(rootDirPath);
        rootDirectory.mkdir();
        users = new File(rootDirectory + osSeparator + "users.json");
        config = new File(rootDirectory + osSeparator + "config.json");
        try {
            users.createNewFile();
            config.createNewFile();
        } catch (IOException e) {

        }
        fileStorage.setCurrentPath(getStorage().getStoragePath());
        writeToConfig(fileStorage);
    }

//    @Override
//    public void createListOfDirectories(String dirName, Integer numberOfDirectories) throws
//            UnauthorizedException, FolderException, StorageException {
//        if (connectedUser.getLevel() < 4) {
//            for (int i = 0; i < numberOfDirectories; i++) {
//                createDirectory(dirName + i);
//            }
//            if (numberOfDirectories == 0) createDirectory(dirName + '0');
//
//        } else throw new UnauthorizedException();
//    }
//    @Override
//    public void createListOfDirRestriction(String dirName, Integer restriction, Integer numberOfDirectories) throws
//            UnauthorizedException, FolderException, StorageException {
//        if (connectedUser.getLevel()<4) {
//            for (int i = 0; i < numberOfDirectories; i++) {
//                createDirectory(dirName + i, restriction);
//            }
//        }
//        else throw new UnauthorizedException();
//    }
//
//    @Override
//    public void createListOfFiles(String filename, Integer numberOfFiles) throws
//            UnauthorizedException, FolderException, IOException {
//        if (connectedUser.getLevel() < 4) {
//            for (int i = 0; i < numberOfFiles; i++) {
//                createFile(filename + i);
//            }
//        } else throw new UnauthorizedException();
//    }

    @Override
    public void createUser(String username, String password, Integer level) throws UnauthorizedException {
        if ((connectedUser == null) || connectedUser.getLevel() == 1) {
            try {
                Gson gson = new Gson();
                User user = new User(username, password, level);
                String path = fileStorage.getStoragePath() + osSeparator + "rootDirectory" + osSeparator + "users.json";
                if (new File(path).length() == 0) {
                    FileWriter file = new FileWriter(path);
                    jsonForUser.append("[");
                    jsonForUser.append(gson.toJson(user));
                    jsonForUser.append("]");
                    file.write(String.valueOf(jsonForUser));
                    file.close();
                }
                else {
                    BufferedReader reader = new BufferedReader(new FileReader(path));
                    jsonForUser = new StringBuilder(reader.readLine());
                    jsonForUser.deleteCharAt(jsonForUser.length() - 1);
                    jsonForUser.append(",");
                    jsonForUser.append(gson.toJson(user));
                    jsonForUser.append("]");
                    FileWriter file = new FileWriter(path);
                    file.write(String.valueOf(jsonForUser));
                    file.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }  else throw new UnauthorizedException();
    }

    // Treba dodati u Specifikaciju ?

    void writeToConfig(Object object) {
        if (object instanceof FileStorage) fileStorage = (FileStorage) object;
        Gson gson = new Gson();
        String path = fileStorage.getStoragePath() + osSeparator + "rootDirectory" + osSeparator + "config.json";
        try {
            FileWriter file = new FileWriter(path, false);
            jsonForStorage = new StringBuilder();
            jsonForStorage.append("[");
            jsonForStorage.append(gson.toJson(fileStorage));
            jsonForStorage.append("]");
            file.write(String.valueOf(jsonForStorage));
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readConfig(String path) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            BufferedReader reader = new BufferedReader(new FileReader(path + osSeparator +
                    "rootDirectory" + osSeparator + "config.json"));
            Type storageListType = new TypeToken<ArrayList<FileStorage>>() {}.getType();
            ArrayList<FileStorage> storageArray = gson.fromJson(reader, storageListType);
            for (FileStorage file: storageArray) {
                fileStorage = file;
                if (fileStorage.getFolderRestrictions() != null)
                    mapOfDirRestrictions = fileStorage.getFolderRestrictions();
            }
            reader.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean goForward(String filename){
        if (!isStorage(fileStorage.getCurrentPath()  + osSeparator + filename)) {
            List<String> list;
            list = listFilesFromDirectory();
            for (String f: list){
                if (f.equals(filename))
                    fileStorage.setCurrentPath(fileStorage.getCurrentPath() + osSeparator + filename);
            }
            return true;
        } else {
            fileStorage.setStoragePath(fileStorage.getCurrentPath()+osSeparator+filename);
            return false;
        }
    }
    @Override
    public void goBackwards(){
        String currentPath = fileStorage.getCurrentPath();
        String separator[] = currentPath.split(Pattern.quote(osSeparator));
            currentPath = "";
            for (int i = 0 ; i < separator.length-1 ; i++) {
                currentPath += separator[i];
                if (i != separator.length - 2) currentPath += osSeparator;
            }
        fileStorage.setCurrentPath(currentPath);
    }

    @Override
    public void moveFile(String filename, String newpath) throws CantChangeRootException, UnauthorizedException {
        String newPath = fileStorage.getStoragePath() + osSeparator + newpath;
        if (connectedUser.getLevel() < 3) {
//            if (newPath.contains(getStorage().getStoragePath()) && !newPath.contains("rootDirectory")) {
            if (!newPath.contains("rootDirectory")) {
                try {
                    Files.move(Paths.get(getStorage().getCurrentPath() + osSeparator+filename),
                            Paths.get(newPath + osSeparator+filename), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {

                }

            } else if (newPath.contains("rootDirectory")) {
                throw new CantChangeRootException();
            }
        } else throw new UnauthorizedException();
    }


    @Override
    public boolean copyFile(String filename, String newpath) throws
            CantChangeRootException, UnauthorizedException, StorageException {
        String newPath = fileStorage.getStoragePath() + osSeparator + newpath;
        if (connectedUser.getLevel()<3) {
            File file = new File(fileStorage.getCurrentPath() + osSeparator + filename);
            File s = new File(fileStorage.getStoragePath());
            if (fileStorage.getSize() - file.length() > 0) {
                Path newDir = Paths.get(newPath);
//                if (newPath.contains(fileStorage.getStoragePath()) && !newPath.contains("rootDirectory")) {
                if (!newPath.contains("rootDirectory")) {
                    if (!file.isDirectory()) {
                        try {
                            Files.copy(Paths.get(getStorage().getCurrentPath() + osSeparator + filename), newDir.resolve(filename),
                                    StandardCopyOption.REPLACE_EXISTING);
                            fileStorage.setSize(fileStorage.getSize() - file.length());
                            return true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            FileUtils.copyDirectory(file, new File(newPath + osSeparator + filename));
                            fileStorage.setSize(fileStorage.getSize() - file.length());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else throw new CantChangeRootException();

            } else throw new StorageException();

        } else throw new UnauthorizedException();

        return false;
    }

    // Mozda ovde neki exception
    @Override
    public boolean uploadFile(String filename) throws CantChangeRootException, UnauthorizedException, StorageException {

        String newPath = fileStorage.getStoragePath();
        if (connectedUser.getLevel()<3) {
            File file = new File(fileStorage.getCurrentPath() + osSeparator + filename);
            File s = new File(fileStorage.getStoragePath());
            if (fileStorage.getSize() - file.length() > 0) {
                Path newDir = Paths.get(newPath);
                if (newPath.contains(fileStorage.getStoragePath()) && !newPath.contains("rootDirectory")) {
                    if (!file.isDirectory()) {
                        try {
                            Files.copy(Paths.get(getStorage().getCurrentPath() + osSeparator + filename), newDir.resolve(filename),
                                    StandardCopyOption.REPLACE_EXISTING);
                            fileStorage.setSize(fileStorage.getSize() - file.length());
                            fileStorage.setCurrentPath(fileStorage.getStoragePath());
                            return true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            FileUtils.copyDirectory(file, new File(newPath + osSeparator + filename));
                            fileStorage.setSize(fileStorage.getSize() - file.length());
                            fileStorage.setCurrentPath(fileStorage.getStoragePath());
                            return true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else throw new CantChangeRootException();

            } else throw new StorageException();

        } else throw new UnauthorizedException();


//        if (copyFile(filename, fileStorage.getStoragePath())) {
//            fileStorage.setCurrentPath(fileStorage.getStoragePath());
//            return true;
//        }
        return false;
    }

    @Override
    public boolean downloadFile(String filename) throws UnauthorizedException {
        if (connectedUser.getLevel() < 3) {
            File file = new File(fileStorage.getCurrentPath() + osSeparator + filename);
            Path downloadDir = Paths.get(System.getProperty("user.home")+osSeparator+"StorageDownloads");

            if (!Files.exists(downloadDir)) {
                File newDir = new File(System.getProperty("user.home") + osSeparator + "StorageDownloads");
                newDir.mkdir();
            }
            if (!file.isDirectory()) {
                try {
                    Files.copy(Paths.get(getStorage().getCurrentPath() + osSeparator + filename),
                            downloadDir.resolve(filename),
                            StandardCopyOption.REPLACE_EXISTING);
                    return true;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    FileUtils.copyDirectory(file, new File(System.getProperty("user.home") +
                            osSeparator + "StorageDownloads" + osSeparator + filename));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else throw new UnauthorizedException();

        return false;
    }

    @Override
    public void deleteFile(String filename) throws FileNotFoundException, UnauthorizedException {
        if (connectedUser.getLevel() < 4) {
            File file = new File(fileStorage.getCurrentPath() + osSeparator + filename);
            if (file.isDirectory()) {
                deleteDirectory(file);
            }
            long fileSize = file.length();
            if (file.delete()) {
                fileStorage.setSize(fileStorage.getSize() + fileSize);
            }
            else throw new FileNotFoundException();

            if (mapOfDirRestrictions.containsKey(fileStorage.getCurrentPath()) == true) {
                Integer numberOfFilesLeft = mapOfDirRestrictions.get(fileStorage.getCurrentPath());
                mapOfDirRestrictions.put(fileStorage.getCurrentPath(), ++numberOfFilesLeft);
            }
        } else throw new UnauthorizedException();

    }


    public void deleteDirectory(File file) throws UnauthorizedException {
        if (connectedUser.getLevel() < 4) {
            for (File subfile : file.listFiles()) {
                if (subfile.isDirectory()) {
                    deleteDirectory(subfile);
                }
                long fileSize = subfile.length();
                if (subfile.delete()) {
                    fileStorage.setSize(fileStorage.getSize() + fileSize);
                }
            }
        } else throw new UnauthorizedException();
    }

    @Override
    public List<String> listFilesFromDirectory(String... extension) {
        List<String> listOfNames = new ArrayList<>();
        File file = new File(fileStorage.getCurrentPath());
        File[] list = file.listFiles();
        if (list != null) {
            for (File f : list) {
                if (extension.length == 0) listOfNames.add(f.getName());
                else {
                    for (String extensionName : extension) {
                        if (f.getName().endsWith(extensionName))
                            listOfNames.add(f.getName());
                    }
                }
            }
        }
        return listOfNames;
    }

    @Override
    public List<String> sort(String order, String ... option) {
        List <String> listOfNames=new ArrayList<>();
        File file = new File(fileStorage.getCurrentPath());
        File[] list = file.listFiles();
        if (option.length == 0) {
            if (order.equals("asc")) {
                Arrays.sort(list);
                for (File f: list){
                    listOfNames.add(f.getName());
                }
            }
            else if (order.equals("desc")) {
                Arrays.sort(list, Collections.reverseOrder());
                for (File f: list){
                    listOfNames.add(f.getName());
                }
            }
        }
        else {
            if (option[0].equals("date")) {
                if (order.equals("asc")) {
                    Arrays.sort(list, Comparator.comparingLong(File::lastModified));
                    for (File f: list){
                        listOfNames.add(f.getName());
                    }
                }
                else if (order.equals("desc")) {
                    Arrays.sort(list, Comparator.comparingLong(File::lastModified).reversed());
                    for (File f: list){
                        listOfNames.add(f.getName());
                    }
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
                            .forEach(entry -> {
                                listOfNames.add(entry.getKey());
                            });
                }
                else if (order.equals("desc")) {
                    mapOfFiles.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                            .forEach((entry -> {
                                listOfNames.add(entry.getKey());
                            }));
                }
            }
        }
        return listOfNames;
    }

    @Override
    public void editFile(String filename) {
        File file = new File(fileStorage.getCurrentPath() + osSeparator + filename);
        if (connectedUser.getLevel() < 4) file.setWritable(true);
        else file.setWritable(false);

        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean logIn(String username, String password) throws UnauthorizedException {

        String path = fileStorage.getStoragePath() + osSeparator + "rootDirectory";

        if (new File(path + osSeparator + "users.json").length() == 0) {
            createUser(username, password, 1);
            connectedUser = new User(username,password,1);
            fileStorage.setCurrentPath(fileStorage.getStoragePath());

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
                        fileStorage.setCurrentPath(getStorage().getStoragePath());
                        return true;
                    }
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void logOut() {
        connectedUser = null;
        writeToConfig(fileStorage);
    }


    @Override
    public boolean isStorage(String path) {
        Path pathToStorage = Paths.get(path);

        if (Files.exists(pathToStorage)) {
            String users = path + osSeparator + "rootDirectory" + osSeparator + "users.json";
            String config = path + osSeparator + "rootDirectory" + osSeparator + "config.json";
            Path pathToUsers = Paths.get(users);
            Path pathToConfig = Paths.get(config);
            if (Files.exists(pathToUsers) && Files.exists(pathToConfig)) return true;

        }
        return false;
    }

    @Override
    public void writeToJsonFile(String s, Object o) {

    }

    @Override
    public User getConnectedUser() {
        return connectedUser;
    }

    @Override
    public FileStorage getStorage() {
        return fileStorage;
    }
}
