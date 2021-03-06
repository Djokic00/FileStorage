import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import Exceptions.ForbiddenExtensionException;
import Exceptions.StorageException;
import Exceptions.UnauthorizedException;
import Exceptions.UnsupportedOperation;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import com.google.api.services.drive.model.FileList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.FileStorage;
import model.User;


// copy - samo sa fajlovima - to radi, a direktorijum je unsupported,
// upload, download ne rade sa direktorijumima
// upload proveriti prvo tip fajla da bi ga uploadovao sa sadrzajem

// copy fora je da ne moze da kopira u skladiste ako se nalazi u folderu, popraviti to!
// move: kad se metodom mkdir kreira vise foldera: fol0 fol1 fol2, ako zeli da pomeri fol0 u fol1 vraca currentpath null i ne moze da se
// krece po putanji, to ce se popraviti proverom da li je skladiste u storage.json

// sort - gotov (po velicini jos fali ali to je unsupported)
// filtriranje - radi po jpeg, json i tako dalje
// restrikcije
// download - gotov (ne radi folder i prazan fajl - ali to je okej)
// copy - radi (ne radi copy unazad)
// move - treba vise fajlova da moze da move
// rm - gotov (gde ce deleteDirectory - ide u spec)
// upload - sad cemo da sredimo - i fajl i direktorijum
// pwd da vrati trenutni naziv gde smo

public class GoogleImplementation extends SpecificationClass {
    private Drive service;
    private String osSeparator = java.io.File.separator;
    private FileStorage fileStorage = new FileStorage();
    private User connectedUser;
    private StringBuilder jsonBuilder, jsonForStorage = new StringBuilder();
    private String usersId;
    private String configId;
    private String storageJsonId = "";
    private boolean usersEmpty = true;
    private HashMap<String, Integer> mapOfDirRestrictions = new HashMap<>();

    static {
        SpecificationManager.registerExporter(new GoogleImplementation());
    }

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "GoogleImplementation";

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials at
     * ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in = GoogleImplementation.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES).setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Override
    public void createFile(String filename) throws IOException, ForbiddenExtensionException {
        if (fileStorage.getRestriction() != null && filename.contains(fileStorage.getRestriction())) {
            throw new ForbiddenExtensionException();
        }
        File file;
        if (!compareNames(filename)) {
            String folderId = fileStorage.getCurrentPath();
            File fileMetadata = new File();
            fileMetadata.setName(filename);
            fileMetadata.setParents(Collections.singletonList(folderId));
            if (mapOfDirRestrictions.containsKey(fileStorage.getCurrentPath()) == true) {
                Integer numberOfFilesLeft = mapOfDirRestrictions.get(fileStorage.getCurrentPath());
                if (numberOfFilesLeft > 0) {
                    try {
                        service.files().create(fileMetadata).setFields("id").execute();
                        mapOfDirRestrictions.put(fileStorage.getCurrentPath(), --numberOfFilesLeft);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                file = service.files().create(fileMetadata).setFields("id, parents").execute();
                file.setName(filename);
                if (filename.contains("users.json")) usersId = file.getId();
                if (filename.contains("config.json")) configId = file.getId();
                if (filename.contains("storage.json")) storageJsonId = file.getId();
            }
        } else System.out.println("File with such name already exists");
    }

    @Override
    public void createDirectory(String directoryName, Integer... restriction) throws StorageException {
        if (!compareNames(directoryName)) {
            File fileMetadata = new File();
            fileMetadata.setName(directoryName);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            if (fileStorage.getCurrentPath() != null) {
                fileMetadata.setParents(Collections.singletonList(fileStorage.getCurrentPath()));
            }
            File file = null;
            if (restriction.length > 0) {
                try {
                    file = service.files().create(fileMetadata).setFields("id").execute();
                    mapOfDirRestrictions.put(file.getId(), restriction[0]);
                    fileStorage.setFolderRestrictions(mapOfDirRestrictions);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (fileStorage.getStoragePath() != null && mapOfDirRestrictions.containsKey(fileStorage.getCurrentPath()) == true) {
                Integer numberOfFilesLeft = mapOfDirRestrictions.get(fileStorage.getCurrentPath());
                if (numberOfFilesLeft > 0) {
                    if (fileStorage.getSize() - 4096 > 0) {
                        try {
                            file = service.files().create(fileMetadata).setFields("id").execute();
                            mapOfDirRestrictions.put(fileStorage.getCurrentPath(), --numberOfFilesLeft);
                            fileStorage.setSize(fileStorage.getSize() - 4096);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            else {
                if (fileStorage.getStoragePath() == null || directoryName.equals("rootDirectory")) {
                    try {
                        file = service.files().create(fileMetadata).setFields("id").execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (fileStorage.getSize() - 4096 > 0) {
                    try {
                        file = service.files().create(fileMetadata).setFields("id").execute();
                        fileStorage.setSize(fileStorage.getSize() - 4096);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
               } else {
                    throw new StorageException();
                }
            }

            if (fileStorage.getStoragePath() == null) {
                fileStorage.setStoragePath(file.getId());
                fileStorage.setCurrentPath(file.getId());
            } else if (directoryName.equals("rootDirectory")) fileStorage.setCurrentPath(file.getId());

        } else System.out.println("Folder with such name already exists");
    }

    @Override
    public void createStorage(String storageName, Long size, String... restrictions) {
        if (restrictions.length > 0) {
            fileStorage = new FileStorage(storageName, size, restrictions[0]);
        }
        else fileStorage = new FileStorage(storageName, size);
        try {
            createDirectory(storageName);
            createDirectory("rootDirectory");
        } catch (StorageException e) {

        }
        try {
            createFile("users.json");
            createFile("config.json");
        } catch (IOException e) {
        } catch (ForbiddenExtensionException e) {

        }

        StorageJson storageJson = new StorageJson(storageName, usersId, configId, fileStorage.getStoragePath(), storageJsonId);
        writeToJsonFile("storage.json", storageJson); // ovde ga ujedno i skida
        writeToConfig(fileStorage);
        fileStorage.setCurrentPath(fileStorage.getStoragePath());
        usersEmpty = true;
    }

    @Override
    public void createUser(String username, String password, Integer level) {
        if ((connectedUser == null) || connectedUser.getLevel() == 1) {
            User user = new User(username, password, level);
            writeToJsonFile("users.json", user);
        }
    }

    @Override
    public void moveFile(String filename, String newPath, String... files) throws UnauthorizedException {
        if(connectedUser.getLevel()<3) {
            if (files.length == 0) {
                String fileId = getIdByName(filename);
                String folderId = getIdByName(newPath);
                File file = null;
                try {
                    file = service.files().get(fileId)
                            .setFields("parents")
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                StringBuilder previousParents = new StringBuilder();
                for (String parent : file.getParents()) {
                    previousParents.append(parent);
                    previousParents.append(',');
                }

                // Move the file to the new folder
                try {
                    file = service.files().update(fileId, null)
                            .setAddParents(folderId)
                            .setRemoveParents(previousParents.toString())
                            .setFields("id, parents")
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                file.setName(filename);
            } else if (files.length > 0) {

                moveFile(filename, newPath);
               // System.out.println("pomerio " + filename);
                for (String f : files) {
                    moveFile(f, newPath);
                }
            }
        }else throw new UnauthorizedException();
    }

    @Override
    public void editFile(String s) throws UnsupportedOperation {
            throw new UnsupportedOperation();
    }

    @Override
    public boolean downloadFile(String filename) {
        String fileId = "";
        if (filename.equals("users.json")) fileId = usersId;
        else if (filename.equals("config.json")) fileId = configId;
        else if (filename.equals("storage.json")) fileId = compareNamesToJson("storage.json");
        else fileId = getIdByName(filename);

        try {
            OutputStream outputStream = new ByteArrayOutputStream();

//            File file = service.files().get(fileId)
//                    .setFields("id, name, mimeType")
//                    .execute();
           service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
//            try {
//                System.out.println("exportujem");
//                System.out.println("mime " + file.getMimeType());
//                service.files().export(fileId, file.getMimeType()).executeMediaAndDownloadTo(outputStream);
//            } catch (Exception e){
//                System.out.println("getujem");
//                service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
//            }

            FileWriter fileWriter = new FileWriter(System.getProperty("user.home") + osSeparator + filename);
            fileWriter.write(String.valueOf(outputStream));
            fileWriter.close();
            outputStream.close();
        } catch (IOException e) {
           // e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean uploadFile(String path) {
        if (connectedUser.getLevel() < 3) {
            java.io.File filePath = new java.io.File(path);
            if (!filePath.isDirectory()) {
                File fileMetadata = new File();
                String parameters[] = path.split(Pattern.quote(osSeparator));
                String filename = parameters[parameters.length - 1];
                fileMetadata.setName(filename);
                try {
                    fileMetadata.setMimeType(Files.probeContentType(Path.of(path)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileMetadata.setParents(Collections.singletonList(fileStorage.getStoragePath()));
               // System.out.println("setovao roditelja " + fileStorage.getStoragePath());


                //FileContent mediaContent = new FileContent("file/txt", filePath);
                FileContent mediaContent = new FileContent(fileMetadata.getMimeType(), filePath);
//            $mime_types= array(
//                    "xls" =>'application/vnd.ms-excel',
//                    "xlsx" =>'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
//                    "xml" =>'text/xml',
//                    "ods"=>'application/vnd.oasis.opendocument.spreadsheet',
//                    "csv"=>'text/plain',
//                    "tmpl"=>'text/plain',
//                    "pdf"=> 'application/pdf',
//                    "jpg"=>'image/jpeg',
//                    "png"=>'image/png',
//                    "txt"=>'text/plain',
//                    "doc"=>'application/msword',
//                    "js"=>'text/js',
//                    "swf"=>'application/x-shockwave-flash',
//                    "zip"=>'application/zip',
//                    "default"=>'application/octet-stream',
//                    "folder"=>'application/vnd.google-apps.folder'
//              );

                File file = null;
                try {
                    file = service.files().create(fileMetadata, mediaContent)
                            .setFields("id, parents")
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                file.setName(filename);
               // System.out.println("File ID: " + file.getId());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean copyFile(String filename, String newPath, String...files) throws UnauthorizedException {
        if (connectedUser.getLevel()<3) {

            if (files.length == 0) {
                File copiedFile = new File();
                copiedFile.setName(filename);
                String parentId = getIdByName(newPath);
                copiedFile.setParents(Collections.singletonList(parentId));
                String fileId = getIdByName(filename);
                try {
                    service.files().copy(fileId, copiedFile).execute();
                    //System.out.println("copied bby");
                    return true;
                } catch (IOException e) {
                    System.out.println("An error occurred: " + e);
                    return false;
                }
            } else if (files.length > 0) {
                copyFile(filename, newPath);
                for (String f : files) {
                    if (copyFile(f, newPath) == true) {
                        return true;
                    }
                }
            }
            return false;
        }else throw new UnauthorizedException();

    }

    @Override
    public void deleteFile(String filename) throws UnauthorizedException {
        if (connectedUser.getLevel()<4) {
            String fileId = getIdByName(filename);
            File file = null;
            try {
                file = service.files().get(fileId).setFields("id, name, mimeType").execute();
                if (file.getMimeType().contains("folder")) {
                    fileStorage.setSize(fileStorage.getSize() + 4096);
                }
                service.files().delete(fileId).execute();
                if (mapOfDirRestrictions.containsKey(fileStorage.getCurrentPath()) == true) {
                    Integer numberOfFilesLeft = mapOfDirRestrictions.get(fileStorage.getCurrentPath());
                    mapOfDirRestrictions.put(fileStorage.getCurrentPath(), ++numberOfFilesLeft);
                }
                //  System.out.println("File " + filename + " deleted");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else throw new UnauthorizedException();
    }

    @Override
    public boolean goForward(String filename) {
        String fileId = getIdByName(filename);
        fileStorage.setCurrentPath(fileId);
        return true;
    }

    @Override
    public void goBackwards() {
        if (!fileStorage.getCurrentPath().contentEquals(fileStorage.getStoragePath())){
        String parentId= getParentId();
        fileStorage.setCurrentPath(parentId);
        }
        //System.out.println("Trenutna putanjica " + fileStorage.getCurrentPath());

    }

    @Override
    public List<String> listFilesFromDirectory(String... extension) {
        List<String> listOfFiles = new ArrayList<>();

        FileList result = listFilesFromDriveSort("name");

//        try{
//            result = service.files().list().setCorpora("user").setQ("'" + fileStorage.getCurrentPath()+"' in parents")
//                    .setPageSize(50)
//                    .setFields("nextPageToken, files(id, name, parents, mimeType)")
//                    .execute();
//        } catch (IOException e){
//            e.printStackTrace();
//        }

        List<File> files = result.getFiles();
        if (files != null || !files.isEmpty()){
            for (File file : files) {

                if (extension.length == 0) listOfFiles.add(file.getName());
                else {
                    for (String extensionName : extension) {
                       // System.out.println("mimeType " + file.getMimeType());

                        if (file.getMimeType().contains(extensionName)) {
                            listOfFiles.add(file.getName());
                        }
                    }
                }
            }
        }
        return listOfFiles;
    }

//            "xls" =>'application/vnd.ms-excel',
//            "xlsx" =>'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
//            "xml" =>'text/xml',
//            "ods"=>'application/vnd.oasis.opendocument.spreadsheet',
//            "csv"=>'text/plain',
//            "tmpl"=>'text/plain',
//            "pdf"=> 'application/pdf',
//            "php"=>'application/x-httpd-php',
//            "jpg"=>'image/jpeg',
//            "png"=>'image/png',
//            "txt"=>'text/plain',
//            "doc"=>'application/msword',
//            "swf"=>'application/x-shockwave-flash',
//            "mp3"=>'audio/mpeg',
//            "zip"=>'application/zip',
//            "rar"=>'application/rar',
//            "tar"=>'application/tar',
//            "arj"=>'application/arj',
//            "cab"=>'application/cab',
//            "html"=>'text/html',
//            "htm"=>'text/html',
//            "folder"=>'application/vnd.google-apps.folder'


    public FileList listFilesFromDriveSort(String order){
        FileList lista = null;

        try {
            lista = service.files().list().setCorpora("user").setQ("'" + fileStorage.getCurrentPath()+"' in parents").setOrderBy(order)
                    .setPageSize(50)
                    .setFields("nextPageToken, files(id, name, parents, mimeType)")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<String> sort(String order, String... option) {
        List<File> listOfFiles = new ArrayList<>();
        List <String> listOfNames = new ArrayList<>();

        FileList result = null;

        if (order.equals("asc")){
            if (option.equals("date")){
                    result = listFilesFromDriveSort("createdTime");
            } else result = listFilesFromDriveSort("name");


        } else if (order.equals("desc")) {
            result = listFilesFromDriveSort("createdTime desc");
        } else result = listFilesFromDriveSort("name desc");


        List<File> files = result.getFiles();
        if (files != null || !files.isEmpty()) {

            //System.out.println("Files:");
            for (File file : files) {

                listOfNames.add(file.getName());
                //System.out.println(file.getParents());
//                if (file.getParents()!=null && file.getParents().contains(fileStorage.getCurrentPath())) {
//                    listOfFiles.add(file);
//                }
            }
        }
            return listOfNames;
    }



    @Override
    public boolean logIn(String username, String password) {
        if (usersEmpty == false) downloadFile("users.json");

        String path = System.getProperty("user.home") + osSeparator + "users.json";
        if (new java.io.File(path).length() == 0) {
            createUser(username, password, 1);
            connectedUser = new User(username, password, 1);
            usersEmpty = false;
            return true;
        } else {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                BufferedReader reader = new BufferedReader(new FileReader(path));
                Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
                ArrayList<User> userArray = gson.fromJson(reader, userListType);
                for (User user : userArray) {
                    if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                        connectedUser = new User(username, password, user.getLevel());
                        fileStorage.setCurrentPath(fileStorage.getStoragePath());
                       // System.out.println("storage " + fileStorage.getStoragePath());
                        return true;
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
           // fileStorage.setCurrentPath(fileStorage.getStoragePath());
           // System.out.println("kraj metode login");
        }
        return false;
    }

    @Override
    public void logOut() {
        writeToConfig(fileStorage);
        connectedUser = null;
        usersEmpty = true;
        fileStorage.setCurrentPath(null);
        java.io.File users = new java.io.File( System.getProperty("user.home") + osSeparator + "users.json");
        java.io.File config = new java.io.File( System.getProperty("user.home") + osSeparator + "config.json");
        java.io.File storage = new java.io.File( System.getProperty("user.home") + osSeparator + "storage.json");
        users.delete();
        config.delete();
        storage.delete();
    }

    @Override
    public User getConnectedUser() {
        return connectedUser;
    }

    @Override
    public FileStorage getStorage() {
        return fileStorage;
    }
    @Override
    public void writeToConfig(FileStorage fileStorage) {
        String path = System.getProperty("user.home") + osSeparator + "config.json";
        Gson gson = new Gson();
        File file;
        try {
            FileWriter fileWriter = new FileWriter(path, false);
            jsonForStorage = new StringBuilder();
            jsonForStorage.append("[");
            jsonForStorage.append(gson.toJson(fileStorage));
            jsonForStorage.append("]");
            fileWriter.write(String.valueOf(jsonForStorage));
            fileWriter.close();
            file = service.files().get(configId).execute();
            FileContent mediaContent = new FileContent(file.getMimeType(), new java.io.File(path));
            File updated = new File();
            System.out.println("fileid " + file.getId());
            service.files().update(file.getId(), updated, mediaContent).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readConfig(String path) {
        try {
            path = System.getProperty("user.home") + osSeparator + "config.json";
            Path configFile = Paths.get(path);
            if (!Files.exists(configFile)) {
                // lepo radi download jer ima njegov ID koji izvlaci iz isStorage iz storage.json
                downloadFile("config.json");
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            BufferedReader reader = new BufferedReader(new FileReader(path));
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
    public boolean isStorage(String storageName) {
        try {
            service = getDriveService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!checkStorageJson()) return false;
           try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                BufferedReader reader = new BufferedReader(new FileReader(
                        System.getProperty("user.home") + osSeparator + "storage.json"));
                Type storageListType = new TypeToken<ArrayList<StorageJson>>() {}.getType();
                ArrayList<StorageJson> userArray = gson.fromJson(reader, storageListType);
                if (!userArray.isEmpty()) {
                    for (StorageJson fs: userArray) {
                        if (fs.getStorageName().equals(storageName)) {
                            usersId = fs.getUserId();
                            configId = fs.getConfigId();
                            // znaci kad imamo storage lokalno i radimo ns necemo praviti novi storage.json jer storageJsonId nije null
                            // i takodje necemo prolaziti kroz sve foldere na drajvu da vidimo njegov id
                            usersEmpty = false;
                            fileStorage.setCurrentPath(fs.getStorageId());
                            return true;
                        }
                        storageJsonId = fs.getStorageJsonId();
                    }
                }

            reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        return false;
    }

    @Override
    public void writeToJsonFile(String filename, Object obj) {
        try {
            String path = System.getProperty("user.home") + osSeparator + filename;
            Gson gson = new Gson();
            File file = new File();
            if (obj instanceof User) {
                file = service.files().get(usersId).execute();
            }
            else if (obj instanceof StorageJson){
                file = service.files().get(storageJsonId).execute();
            }

            if (new java.io.File(path).length() == 0) jsonBuilder = new StringBuilder();

            if (new java.io.File(path).length() == 0) {
                FileWriter fileWriter = new FileWriter(path);
                jsonBuilder.append("[");
                if (obj instanceof User) {
                    User user = (User) obj;
                    jsonBuilder.append(gson.toJson(user));
                }
                else {
                    StorageJson fileStorage = (StorageJson) obj;
                    jsonBuilder.append(gson.toJson(fileStorage));
                }
                jsonBuilder.append("]");
                fileWriter.write(String.valueOf(jsonBuilder));
                fileWriter.close();
            }
            else {
                BufferedReader reader = new BufferedReader(new FileReader(path));
                jsonBuilder = new StringBuilder(reader.readLine());
                jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
                jsonBuilder.append(",");
                if (obj instanceof User) {
                    User user = (User) obj;
                    jsonBuilder.append(gson.toJson(user));
                }
                else {
                    StorageJson fileStorage = (StorageJson) obj;
                    jsonBuilder.append(gson.toJson(fileStorage));
                }
                jsonBuilder.append("]");
                FileWriter fileWriter = new FileWriter(path);
                fileWriter.write(String.valueOf(jsonBuilder));
                fileWriter.close();
            }
            FileContent mediaContent = new FileContent(file.getMimeType(), new java.io.File(path));
            File updated = new File();
            service.files().update(file.getId(), updated, mediaContent).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getCurrentLocation(){
        String currentLocation = getNameById(fileStorage.getCurrentPath());
        return currentLocation;
    }

    public String getNameById(String id){
        String filename = "";
        try {
            File file=service.files().get(id)
                    .setFields("id, name")
                    .execute();

            filename = file.getName();
            return filename;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filename;
    }

    public FileList listFilesFromDrive(){
        FileList lista = null;

        try {
            lista = service.files().list().setCorpora("user")
                    .setPageSize(50)
                    .setFields("nextPageToken, files(id, name, parents)")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public String getIdByName(String filename){
        String fileId = "";
        FileList lista = listFilesFromDrive();

        List<File> fajlovi = lista.getFiles();
        if (fajlovi != null || !fajlovi.isEmpty()) {

            for (File qq : fajlovi) {
                try {
                    if (qq.getParents().contains(fileStorage.getCurrentPath()) && qq.getName().equals(filename)){
                        fileId=qq.getId();
                        System.out.println(fileId + " = "+ qq.getId());
                    }
                }catch (Exception e) {
                }
            }
            if (fileId == "" && compareStorageNames(filename) != ""){
                //prodje kroz json
                fileId = compareStorageNames(filename);
            }
        }
        return fileId;
    }


    public String getParentId(){
        String parentId=null;
        FileList lista = listFilesFromDrive();

        List<File> fajlovi = lista.getFiles();
        if (fajlovi != null || !fajlovi.isEmpty()) {
            for (File qq : fajlovi) {
                try {
                    if (qq.getId().contains(fileStorage.getCurrentPath()) ){

                    List<String> parents = qq.getParents();
                    parentId=parents.get(0);
                    } else if (qq.getId().contains(fileStorage.getStoragePath()))
                        parentId=fileStorage.getStoragePath();
                }catch (Exception e){
                }
            }
        }
        return parentId;
    }

    public boolean compareNames(String filename) {
        if (fileStorage.getCurrentPath() == null) return false;
        FileList list = null;
        try{
            list = service.files().list().setCorpora("user").setQ("'" + fileStorage.getCurrentPath()+"' in parents")
                    .setPageSize(50)
                    .setFields("nextPageToken, files(id, name, parents, mimeType)")
                    .execute();
        } catch (IOException e){
            e.printStackTrace();
        }
                List<File> files = list.getFiles();
        if (files != null || !files.isEmpty()) {

            for (File file : files) {
                try {
                    if (file.getName().equals(filename))
                        return true;
                } catch (Exception e) {
                    //System.out.println("Nisam vratio ID ");
                }
            }
       }
        return false;
    }

    public String compareNamesToJson(String filename) {
        FileList lista = null;
        String jsonId = "";
        try {
            lista = service.files().list().setPageSize(30)
                    .setFields("nextPageToken, files(id, name, parents)").execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<File> fajlovi = lista.getFiles();
        if (fajlovi != null || !fajlovi.isEmpty()) {

            for (File qq : fajlovi) {
                try {
                    if (qq.getName().equals(filename)) {
                        System.out.println(qq.getName());
                        jsonId=qq.getId();
                        return jsonId;
                    }
                }catch (Exception e){
                }
            }
        }
        return jsonId;

    }

    public String compareStorageNames(String storagename){
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            BufferedReader reader = new BufferedReader(new FileReader(
                    System.getProperty("user.home") + osSeparator + "storage.json"));
            Type storageListType = new TypeToken<ArrayList<FileStorage>>() {}.getType();
            ArrayList<FileStorage> userArray = gson.fromJson(reader,storageListType);
            for (FileStorage fs: userArray) {
                if (fs.getStorageName().equals(storagename)) {
                    return fs.getStoragePath();
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    boolean checkStorageJson() {
        String fileID = compareNamesToJson("storage.json");
        if (fileID != "") downloadFile("storage.json");
        else {
            try {
                createFile("storage.json");
                return false;
            } catch (IOException | ForbiddenExtensionException e) {
            }
        }
        return true;
    }
}

