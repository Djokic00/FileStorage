import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import Exceptions.UnauthorizedActionException;
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
import errorHandler.ErrorType;
import model.FileStorage;
import model.User;
import org.mortbay.jetty.MimeTypes;

// Glavna stvar: napraviti storage.json fajl (to resava: copy, isStorage, move) ima ime i id skladista
// isStorage proverava da li je u json fajlu
// dodati isStorage u goForward metodu


// download puca u jednoj liniji---->ne puca?
// list puca u jednoj liniji---> ne puca?

// copy, upload, download ne rade sa direktorijumima
// download ne radi dobro, skine prazan fajl, skrinsotovana greska

// upload proveriti prvo tip fajla da bi ga uploadovao sa sadrzajem

//PATH LOGOVANJE
// copy fora je da ne moze da kopira u skladiste ako se nalazi u folderu, popraviti to! popravlja se sa storage.json fajlov
// move: kad se metodom mkdir kreira vise foldera: fol0 fol1 fol2, ako zeli da pomeri fol0 u fol1 vraca currentpath null i ne moze da se
// krece po putanji, to ce se popraviti proverom da li je skladiste u storage.json

// sort, filtriranje, restrikcije, download, copy, move


public class GoogleImplementation extends SpecificationClass implements SpecificationInterface {
    Drive service;
    String osSeparator = java.io.File.separator;
    FileStorage fileStorage = new FileStorage();
    User connectedUser;
    StringBuilder jsonBuilder, jsonForStorage = new StringBuilder();
    String usersId;
    String configId;
    String storageJsonId = "";
    boolean usersEmpty = true;
    boolean storageJsonDownloaded = false;
    //String currentPath; - realno fileStorage ne treba da ima currentPath
    HashMap<String, Integer> mapOfDirRestrictions = new HashMap<>();

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
    public void createFile(String filename) throws IOException {
        if (!compareNames(filename)) {
            System.out.println(fileStorage.getCurrentPath());
            String folderId = fileStorage.getCurrentPath();
            File fileMetadata = new File();
            fileMetadata.setName(filename);
            fileMetadata.setParents(Collections.singletonList(folderId));
            File file = service.files().create(fileMetadata).setFields("id, parents").execute();
            System.out.println("File ID: " + file.getParents());
            file.setName(filename);

            //fileMetadata2.setFileExtension("txt");
            // java.io.File filePath = new java.io.File("files/photo.jpg");
            // FileContent mediaContent = new FileContent("image/jpeg", filePath);

            if (filename.contains("users.json")) usersId = file.getId();
            if (filename.contains("config.json")) configId = file.getId();
            if (filename.contains("storage.json")) storageJsonId = file.getId();

        } else System.out.println("File with such name already exists");
    }

    @Override
    public void createDirectory(String directoryName, Integer... restriction) {
        if (!compareNames(directoryName)) {

//            if (restriction.length > 0) {
//                mapOfDirRestrictions.put(fileStorage.getCurrentPath(), restriction[0]);
//                fileStorage.setFolderRestrictions(mapOfDirRestrictions);
//            }

            File fileMetadata = new File();
            fileMetadata.setName(directoryName);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            if (fileStorage.getCurrentPath() != null) {
                fileMetadata.setParents(Collections.singletonList(fileStorage.getCurrentPath()));
                System.out.println("setuje parenta " + fileStorage.getCurrentPath());
            }
            File file = null;
//            if (mapOfDirRestrictions.containsKey(fileStorage.getCurrentPath()) == true) {
//                Integer numberOfFilesLeft = mapOfDirRestrictions.get(fileStorage.getCurrentPath());
//                if (numberOfFilesLeft > 0) {
//                    if (fileStorage.getSize() - 4096 > 0) {
//                        try {
//                            file = service.files().create(fileMetadata).setFields("id").execute();
//                            mapOfDirRestrictions.put(fileStorage.getCurrentPath(), --numberOfFilesLeft);
//                            fileStorage.setSize(fileStorage.getSize() - 4096);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//            else {
                //if (fileStorage.getSize() - 4096 > 0) {
                    try {
                        file = service.files().create(fileMetadata).setFields("id").execute();
                        //fileStorage.setSize(fileStorage.getSize() - 4096);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
               // }
//            }

            System.out.println("Folder ID: " + file.getId());
            if (fileStorage.getStoragePath() == null) {
                fileStorage.setStoragePath(file.getId());
            }

            fileStorage.setCurrentPath(file.getId());
            //System.out.println("storage path " + fileStorage.getStoragePath());

        } else System.out.println("Folder with such name already exists");
    }

    @Override
    public void createStorage(String storageName, Long size, String... restrictions) {
//        if (restrictions.length > 0) {
//            fileStorage = new FileStorage(storageName, size, restrictions[0]);
//        }
//        else fileStorage = new FileStorage(storageName, size); // StorageName nam realno ne treba kao polje u konstruktoru

        try {
            if (storageJsonId == "") createFile("storage.json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        createDirectory(storageName);
        createDirectory("rootDirectory");
        try {
            createFile("users.json");
            createFile("config.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

        StorageJson storageJson = new StorageJson(storageName, usersId, configId, fileStorage.getStoragePath(), storageJsonId);
        writeToJsonFile("storage.json", storageJson); // ovde ga ujedno i skida

        writeToConfig(fileStorage);
        usersEmpty = true;
    }

    @Override
    public void createListOfDirectories(String dirName, Integer numberOfDirectories) {
        String currentPath = fileStorage.getCurrentPath();

        if (connectedUser.getLevel() < 4) {
            for (int i = 0; i < numberOfDirectories; i++) {
                fileStorage.setCurrentPath(currentPath);
                createDirectory(dirName + i);
            }
            if (numberOfDirectories == 0) createDirectory(dirName + '0');
            fileStorage.setCurrentPath(currentPath);
        }
    }

    @Override
    public void createListOfDirRestriction(String dirName, Integer restriction, Integer numberOfDirectories) {
        String currentPath = fileStorage.getCurrentPath();
        if (connectedUser.getLevel() < 4) {
            for (int i = 0; i < numberOfDirectories; i++) {
                fileStorage.setCurrentPath(currentPath);
                createDirectory(dirName + i, restriction);
            }
            fileStorage.setCurrentPath(currentPath);
        }
    }

    @Override
    public void createListOfFiles(String filename, Integer numberOfFiles) {
        for (int i = 0; i < numberOfFiles; i++) {
            try {
                createFile(filename + i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void createUser(String username, String password, Integer level) {
        if ((connectedUser == null) || connectedUser.getLevel() == 1) {
            User user = new User(username, password, level);
            writeToJsonFile("users.json", user);
        }
    }

    @Override
    public void moveFile(String filename, String newPath) {
        String fileId = getIdByName(filename);
        String folderId =getIdByName(newPath);
// Retrieve the existing parents to remove
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
        System.out.println("Moved " + file.getName());
    }

    @Override
    public void editFile(String s) {

    }

    @Override
    public boolean downloadFile(String filename) {
        String fileId = "";
        if (filename.equals("users.json")) fileId = usersId;
        else if (filename.equals("config.json")) fileId = configId;
        else if (filename.equals("storage.json")) fileId = compareNamesToJson("storage.json");
        else fileId = getIdByName(filename);

        try {
            System.out.println(fileId + " " + usersId);
            OutputStream outputStream = new ByteArrayOutputStream();

            File file=service.files().get(fileId)
                    .setFields("id, name, mimeType")
                    .execute();
           // service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            try {
                System.out.println("exportujem");
                System.out.println("mime " + file.getMimeType());
                service.files().export(fileId, file.getMimeType()).executeMediaAndDownloadTo(outputStream);
            } catch (Exception e){
                System.out.println("getujem");
                service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            }

            FileWriter fileWriter = new FileWriter(System.getProperty("user.home") + osSeparator + filename);
            fileWriter.write(String.valueOf(outputStream));
            fileWriter.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//
//        Path downloadDir = Paths.get(System.getProperty("user.home")+osSeparator+"StorageDownloads");
//
//        if (!Files.exists(downloadDir)) {
//            java.io.File newDir = new java.io.File(System.getProperty("user.home") + osSeparator + "StorageDownloads");
//            System.out.println("napravio sam storageDownloads");
//            newDir.mkdir();
//        }
//        OutputStream outputStream = null;
//        try {
//            outputStream = new FileOutputStream(System.getProperty("user.home") + osSeparator + "StorageDownloads" +
//                    osSeparator + filename);
//        } catch (FileNotFoundException e) {
//            System.out.println("ovde pucam");
//        }
//
//        System.out.println("skinuo sam fajlic " + fileId);
//        System.out.println("kraj metode");
        return true;
    }

    @Override
    public boolean uploadFile(String path) {
//        File fileMetadata = new File();
//        fileMetadata.setTitle("My Report");
//        fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");
//
//        java.io.File filePath = new java.io.File("files/report.csv");
//        FileContent mediaContent = new FileContent("text/csv", filePath);
//        File file = driveService.files().insert(fileMetadata, mediaContent)
//                .setFields("id")
//                .execute();
//        System.out.println("File ID: " + file.getId());

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
                System.out.println("setovao roditelja " + fileStorage.getStoragePath());


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
                System.out.println("File ID: " + file.getId());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean copyFile(String filename, String newPath) {
        File copiedFile = new File();
        copiedFile.setName(filename);
        String parentId=getIdByName(newPath);
        copiedFile.setParents(Collections.singletonList(parentId));
        String fileId = getIdByName(filename);
        try {
            service.files().copy(fileId, copiedFile).execute();
            System.out.println("copied bby");
            return true;
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
            return false;
        }

    }

    @Override
    public void deleteFile(String filename) {
        String fileId = getIdByName(filename);
        try {
            service.files().delete(fileId).execute();
            System.out.println("obrisao sam " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        System.out.println("Trenutna putanjica " + fileStorage.getCurrentPath());


    }

    @Override
    public List<String> listFilesFromDirectory(String... extension) {
        List<String> listOfFiles = new ArrayList<>();

        FileList result = null;

        try{
            result = service.files().list().setCorpora("user").setQ("'" + fileStorage.getCurrentPath()+"' in parents")
                    .setPageSize(50)
                    .setFields("nextPageToken, files(id, name, parents, mimeType)")
                    .execute();
        } catch (IOException e){
            e.printStackTrace();
        }

        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            for (File file : files) {

                if (extension.length == 0) listOfFiles.add(file.getName());
                else {
                    for (String extensionName : extension) {
                        System.out.println("mimeType " + file.getMimeType());

                        if (file.getMimeType().contains(extensionName)) {
                            System.out.println("fajl extension " + file.getFileExtension());
                            listOfFiles.add(file.getName());
                        }
                    }
                }
            }
        }

//        try {
//            result = service.files().list()
//                    .setPageSize(50)
//                    .setFields("nextPageToken, files(id, name, parents, mimeType)")
//                    .execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        List<File> files = result.getFiles();
//        if (files == null || files.isEmpty()) {
//            System.out.println("No files found.");
//        } else {
//            //System.out.println("Files:");
//            for (File file : files) {
//                //System.out.println(file.getParents());
//                if (file.getParents()!=null && file.getParents().contains(fileStorage.getCurrentPath())) {
//                    System.out.println("extension length = " + extension.length);
//                    //System.out.printf("%s (%s)\n", file.getName(), file.getId());
//                    if (extension.length == 0) listOfFiles.add(file.getName());
//                    else {
//                        for (String extensionName : extension) {
//                             System.out.println("mimeType " + file.getMimeType());
//                           // System.out.println("file ext " + file.getFileExtension());
//                            if (file.getMimeType().contains(extensionName)) {
//                                System.out.println("fajl extension " + file.getFileExtension());
//                                listOfFiles.add(file.getName());
//                            }
//                        }
//                    }
//
//                }
//            }
//
//            return listOfFiles;
//        }
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
//            "gif"=>'image/gif',
//            "bmp"=>'image/bmp',
//            "txt"=>'text/plain',
//            "doc"=>'application/msword',
//            "js"=>'text/js',
//            "swf"=>'application/x-shockwave-flash',
//            "mp3"=>'audio/mpeg',
//            "zip"=>'application/zip',
//            "rar"=>'application/rar',
//            "tar"=>'application/tar',
//            "arj"=>'application/arj',
//            "cab"=>'application/cab',
//            "html"=>'text/html',
//            "htm"=>'text/html',
//            "default"=>'application/octet-stream',
//            "folder"=>'application/vnd.google-apps.folder'



    @Override
    public List<String> sort(String order, String... option) {
        List<File> listOfFiles = new ArrayList<>();
        List <String> listOfNames = new ArrayList<>();

        FileList result = null;

        if (order.equals("asc")){
            if (option.equals("date")){

                try {
                    result = service.files().list().setCorpora("user").setQ("'" + fileStorage.getCurrentPath()+"' in parents").setOrderBy("createdTime")
                            .setPageSize(50)
                            .setFields("nextPageToken, files(id, name, parents)")
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            try {
                result = service.files().list().setCorpora("user").setQ("'" + fileStorage.getCurrentPath()+"' in parents").setOrderBy("name")
                        .setPageSize(50)
                        .setFields("nextPageToken, files(id, name, parents)")
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (order.equals("desc")) {
            if (option.equals("date")){

                try {
                    result = service.files().list().setQ("'" + fileStorage.getCurrentPath()+"' in parents").setOrderBy("createdTime desc")
                            .setPageSize(50)
                            .setFields("nextPageToken, files(id, name, parents)")
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            try {
                result = service.files().list().setQ("'" + fileStorage.getCurrentPath() + "' in parents").setOrderBy("name desc")
                        .setPageSize(50)
                        .setFields("nextPageToken, files(id, name, parents)")
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            //System.out.println("Files:");
            for (File file : files) {

                listOfNames.add(file.getName());
                //System.out.println(file.getParents());
//                if (file.getParents()!=null && file.getParents().contains(fileStorage.getCurrentPath())) {
//
//                    listOfFiles.add(file);
//
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
                        System.out.println("storage " + fileStorage.getStoragePath());
                        return true;
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
           // fileStorage.setCurrentPath(fileStorage.getStoragePath());
            System.out.println("kraj metode login");
        }
        return false;
    }

    @Override
    public void logOut() {
        connectedUser = null;
        usersEmpty = true;
        fileStorage.setCurrentPath(null);
        java.io.File users = new java.io.File( System.getProperty("user.home") + osSeparator + "users.json");
        java.io.File config = new java.io.File( System.getProperty("user.home") + osSeparator + "config.json");
        users.delete();
        config.delete();
    }

    @Override
    public User getConnectedUser() {
        return connectedUser;
    }

    @Override
    public FileStorage getStorage() {
        return fileStorage;
    }

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
//                if (fileStorage.getFolderRestrictions() != null)
//                    mapOfDirRestrictions = fileStorage.getFolderRestrictions();
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

        if (storageJsonDownloaded==false){
        downloadFile("storage.json");
        }else storageJsonDownloaded=true;


        Path storageJsonFile = Paths.get(System.getProperty("user.home") + osSeparator + "storage.json");
       if (Files.exists(storageJsonFile)) {
            System.out.println("ulazi");
           try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                BufferedReader reader = new BufferedReader(new FileReader(
                        System.getProperty("user.home") + osSeparator + "storage.json"));
                Type storageListType = new TypeToken<ArrayList<StorageJson>>() {}.getType();
                ArrayList<StorageJson> userArray = gson.fromJson(reader, storageListType);
                for (StorageJson fs: userArray) {
                    if (fs.getStorageName().equals(storageName)) {
                        usersId = fs.getUserId();
                        configId = fs.getConfigId();
                        // znaci kad imamo storage lokalno i radimo ns necemo praviti novi storage.json jer storageJsonId nije null
                        // i takodje necemo prolaziti kroz sve foldere na drajvu da vidimo njegov id
                        usersEmpty = false;
                       // fileStorage.setCurrentPath(fs.getStorageId());
                        return true;
                    }
                    storageJsonId = fs.getStorageJsonId();
                    //System.out.println("storagepath: " + fileStorage.getStoragePath());
                }
            reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("ne postoji storage.json");
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
                System.out.println("Ulazi ovde");
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
            System.out.println("fileid " + file.getId());
            service.files().update(file.getId(), updated, mediaContent).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getIdByName(String filename){
        String fileId = "";
        FileList lista = null;

        try {
            lista = service.files().list().setCorpora("user")
                    .setPageSize(50)
                    .setFields("nextPageToken, files(id, name, parents)")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<File> fajlovi = lista.getFiles();
        if (fajlovi == null || fajlovi.isEmpty()) {
            System.out.println("No files found.");
        } else {
            for (File qq : fajlovi) {
                try {
                    System.out.println("parent " + qq.getParents());
                    if (qq.getParents().contains(fileStorage.getCurrentPath()) && qq.getName().equals(filename)){
                        fileId=qq.getId();
                        System.out.println(fileId + " = "+ qq.getId());
                    }
//                    ne radi get id by name van current patha pa onda ne moze da kopira nesto (iz foldera koji je u skladistu )
//                    ->u skladiste
//                    ne moze ni da kopira iz foldera u folderu -> unazad u njegovog roditelja
//                    else if (qq.getParents().contains(fileStorage.getStoragePath()) && qq.getName().equals(filename)){
//                        fileId=qq.getId();
//                        System.out.println(fileId + " = "+ qq.getId());
//                    }


//                      if (qq.getName().equals(filename)){
//                          fileId=qq.getId();
//                      }

//                    else if (isStorage(filename)){
//                        fileId=fileStorage.getStoragePath();
//                    }

                  //  System.out.println(fileId + " = "+ qq.getId());
//                    if (fileStorage.getCurrentPath().equals(fileStorage.getStoragePath())){
//
//                    }
                }catch (Exception e){
                    System.out.println("Nisam vratio ID ");
                }
            }
            if (fileId == "" && compareStorageNames(filename) != ""){
                //prodje kroz json
                fileId = compareStorageNames(filename);
                System.out.println(fileId);
            }
        }
        return fileId;
    }



    public String getParentId(){
        String parentId=null;
        FileList lista = null;

        try {
            lista = service.files().list().setCorpora("user")
                    .setPageSize(50)
                    .setFields("nextPageToken, files(id, name, parents)")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<File> fajlovi = lista.getFiles();
        if (fajlovi == null || fajlovi.isEmpty()) {
            System.out.println("No files found.");
        } else {
            for (File qq : fajlovi) {
                try {
                    if (qq.getId().contains(fileStorage.getCurrentPath()) ){

                    List<String> parents = qq.getParents();
                    parentId=parents.get(0);
                    System.out.println("size niza ParentId " + parentId);
                    } else if (qq.getId().contains(fileStorage.getStoragePath()))
                        parentId=fileStorage.getStoragePath();
                    System.out.println("u skladistu sam");
                }catch (Exception e){
                    System.out.println("Nisam vratio ID ");
                }
            }
        }
        return parentId;
    }
    public String getParentIdByName(String filename){
        String parentId="";
        FileList lista = null;

        try {
            lista = service.files().list()
                    .setPageSize(50)
                    .setFields("nextPageToken, files(id, name, parents)")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<File> fajlovi = lista.getFiles();
        if (fajlovi == null || fajlovi.isEmpty()) {
            System.out.println("No files found.");
        } else {
            for (File qq : fajlovi) {
                try {
                    if (qq.getParents().contains(fileStorage.getCurrentPath()) && qq.getName().equals(filename)){
                        List<String> parents = qq.getParents();
                        parentId=parents.get(0);
                        System.out.println("size niza ParentId " + parentId);
                    }
                }catch (Exception e){
                    System.out.println("Nisam vratio ID ");
                }
            }
        }
        return parentId;

    }
    public boolean compareNames(String filename){

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
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            for (File file : files) {
                try {
                    if (file.getName().equals(filename))
                        return true;
                } catch (Exception e) {
                    System.out.println("Nisam vratio ID ");
                }
            }


//        FileList list = null;
//
//        try {
//            list = service.files().list()
//                    .setPageSize(50)
//                    .setFields("nextPageToken, files(id, name, parents)")
//                    .execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        List<File> files = list.getFiles();
//        if (files == null || files.isEmpty()) {
//            System.out.println("No files found.");
//        } else {
//            for (File file : files) {
//                try {
//                    if (file.getParents().contains(fileStorage.getCurrentPath()) && file.getName().equals(filename))
//                        return true;
//                } catch (Exception e) {
//                    System.out.println("Nisam vratio ID ");
//                }
//            }
//        }
        }return false;

    }

    public String compareNamesToJson(String filename){
        FileList lista = null;
        String jsonId="";

        try {
            lista = service.files().list()
                    .setPageSize(100)
                    .setFields("nextPageToken, files(id, name, parents)")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<File> fajlovi = lista.getFiles();
        if (fajlovi == null || fajlovi.isEmpty()) {
            System.out.println("No files found.");
        } else {
            for (File qq : fajlovi) {
                try {
                    if (qq.getName().equals(filename)) {
                        System.out.println(qq.getName());
                        jsonId=qq.getId();
                        System.out.println("storageJsonId " + jsonId);
                        return jsonId;
                    }
                }catch (Exception e){
                    System.out.println("Nisam vratio ID ");
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
                    System.out.println(storagename);
                    return fs.getStoragePath();

                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




}

