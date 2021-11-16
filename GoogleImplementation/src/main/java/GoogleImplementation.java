import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

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


// download puca u jednoj liniji
// list puca u jednoj liniji
// copy i upload ne rade sa direktorijumima
// copy fora je da ne moze da kopira u skladiste ako se nalazi u folderu, popraviti to!
// move: kad se metodom mkdir kreira vise foldera: fol0 fol1 fol2, ako zeli da pomeri fol0 u fol1 vraca currentpath null i ne moze da se
// krece po putanji
// upload proveriti prvo tip fajla da bi ga uploadovao sa sadrzajem

// config, login, isStorage, createUser ; sort

public class GoogleImplementation extends SpecificationClass implements SpecificationInterface {
    Drive service;
    String osSeparator = java.io.File.separator;
    FileStorage fileStorage = new FileStorage();
    User connectedUser;
    StringBuilder jsonForUser=new StringBuilder();
    String usersId;
    String configId;
    int num = 0;

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
//        service = getDriveService();
//        File fileMetadata = new File();
//        fileMetadata.setName(filename);
//        fileMetadata.setMimeType("application/vnd.google-apps.folder");
//
//        File folder = null;
//        try {
//            folder = service.files().create(fileMetadata).setFields("id").execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Folder ID: " + folder.getId());
        if (compareNames(filename)==false) {
            System.out.println(fileStorage.getCurrentPath());

            String folderId = fileStorage.getCurrentPath();
            File fileMetadata2 = new File();
            fileMetadata2.setName(filename);

            //fileMetadata2.setFileExtension("txt");

            fileMetadata2.setParents(Collections.singletonList(folderId));
            // java.io.File filePath = new java.io.File("files/photo.jpg");
            // FileContent mediaContent = new FileContent("image/jpeg", filePath);

            File file = service.files().create(fileMetadata2)
                    .setFields("id, parents")
                    .execute();

            System.out.println("File ID: " + file.getParents());
            file.setName(filename);
            //done
            if (filename.contains("users.json"))
                usersId = file.getId();
            if (filename.contains("config.json"))
                configId = file.getId();

            System.out.println("kraj metode");
        } else System.out.println("vec postoji");
    }

    @Override
    public void createDirectory(String directoryname, Integer... integers) {
        if (compareNames(directoryname)==false) {
            File fileMetadata = new File();
            fileMetadata.setName(directoryname);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            if (fileStorage.getCurrentPath() != null) {
                fileMetadata.setParents(Collections.singletonList(fileStorage.getCurrentPath()));
                System.out.println("setuje parenta " + fileStorage.getCurrentPath());

            }
            File file = null;
            try {
                file = service.files().create(fileMetadata)
                        .setFields("id")
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Folder ID: " + file.getId());
            if (fileStorage.getStoragePath() == null) {
                fileStorage.setStoragePath(file.getId());

            }
            fileStorage.setCurrentPath(file.getId());
            System.out.println("storage path " + fileStorage.getStoragePath());
            //System.out.println("current path " + fileStorage.getCurrentPath());
            System.out.println("kraj metode");

        }else System.out.println("vec postoji");
    }

    @Override
    public void createStorage(String storagename, Long aLong, String... strings) {
        try {
            service = getDriveService();
        } catch (IOException e) {
            e.printStackTrace();
        }
        createDirectory(storagename);
            //getStorage().setCurrentPath();
        createDirectory("rootDirectory");
        try {
            createFile("users.json");
            Path downloadDir = Paths.get(System.getProperty("user.home")+osSeparator+"StorageDownloads");

            if (!Files.exists(downloadDir)) {
                java.io.File newDir = new java.io.File(System.getProperty("user.home") + osSeparator + "StorageDownloads");
                System.out.println("napravio sam storageDownloads");
                newDir.mkdir();
            }
            java.io.File fileContent = new java.io.File(System.getProperty("user.home") + osSeparator + "StorageDownloads" + osSeparator + "users.json");
            fileContent.createNewFile();
            createFile("config.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileStorage.setCurrentPath(fileStorage.getStoragePath());
        System.out.println("storage path " + fileStorage.getCurrentPath());
        System.out.println("kraj metode");

    }

    @Override
    public void createListOfDirectories(String dirName, Integer numberOfDirectories) {
        String currentPath=fileStorage.getCurrentPath();

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
        String currentPath=fileStorage.getCurrentPath();
        if (connectedUser.getLevel()<4) {
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

 //       try {

//            Path downloadDir = Paths.get(System.getProperty("user.home")+osSeparator+"StorageDownloads");
//
//            if (!Files.exists(downloadDir)) {
//                java.io.File newDir = new java.io.File(System.getProperty("user.home") + osSeparator + "StorageDownloads");
//                System.out.println("napravio sam storageDownloads");
//                newDir.mkdir();
//            }

//        try {
//            File file = service.files().get(usersid).execute();
//            java.io.File fileContent = new java.io.File("/home/aleksa/Desktop/novifajl.json");
//            FileContent mediaContent = new FileContent(file.getMimeType(), fileContent);
//            File updated = new File();
//            service.files().update(file.getId(), updated, mediaContent).execute();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//            File file = service.files().get(usersid).execute();
////            if (file.size()>=8) downloadFile("users.json");
//
//                java.io.File fileContent = new java.io.File(System.getProperty("user.home") + osSeparator + "StorageDownloads" + osSeparator + "users.json");
//                FileContent mediaContent = new FileContent(file.getMimeType(), fileContent);
//                File updated = new File();
//                service.files().update(file.getId(), updated, mediaContent).execute();
//                System.out.println("updated");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        if ((connectedUser == null) || connectedUser.getLevel() == 1) {
            try {
                File file = service.files().get(usersId).execute();
                Gson gson = new Gson();
                User user = new User(username, password, level);
                String path = System.getProperty("user.home") + osSeparator + "users.json";
                //java.io.File newFile = new java.io.File(path);
                if (new java.io.File(path).length() == 0) {
                    System.out.println("Ulazi ovde");
                    FileWriter fileWriter = new FileWriter(path);
                    jsonForUser.append("[");
                    jsonForUser.append(gson.toJson(user));
                    jsonForUser.append("]");
                    fileWriter.write(String.valueOf(jsonForUser));
                    fileWriter.close();
                }
                else {
                    BufferedReader reader = new BufferedReader(new FileReader(path));
                    jsonForUser = new StringBuilder(reader.readLine());
                    jsonForUser.deleteCharAt(jsonForUser.length() - 1);
                    jsonForUser.append(",");
                    jsonForUser.append(gson.toJson(user));
                    jsonForUser.append("]");
                    FileWriter fileWriter = new FileWriter(path);
                    fileWriter.write(String.valueOf(jsonForUser));
                    fileWriter.close();
                }
                FileContent mediaContent = new FileContent(file.getMimeType(), new java.io.File(path));
                File updated = new File();
                service.files().update(file.getId(), updated, mediaContent).execute();
                //newFile.delete();

            } catch (IOException e) {
                e.printStackTrace();
            }
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
        else fileId = getIdByName(filename);

        try {
            service.files().get(fileId).execute();
            OutputStream outputStream = new FileOutputStream(System.getProperty("user.home") + osSeparator + filename);
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//
//
//        } catch (IOException e) {
//            //System.out.println("Fajl je 0, zato puca. Mora da ima barem 1 bajt");
//            e.printStackTrace();
//        }

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
//        try {
//            service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
//        } catch (IOException e) {
//            System.out.println("Fajl je 0, zato puca. Mora da ima barem 1 bajt");
//            e.printStackTrace();
//        }
//
//        System.out.println("skinuo sam fajlic " + fileId);
//        System.out.println("kraj metode");
        return true;
    }

    @Override
    public boolean uploadFile(String path) {
        if (connectedUser.getLevel()<3) {
            File fileMetadata = new File();
            String parameters[] = path.split(Pattern.quote(osSeparator));
            String filename = parameters[parameters.length - 1];
            fileMetadata.setName(filename);
            fileMetadata.setParents(Collections.singletonList(fileStorage.getStoragePath()));
            java.io.File filePath = new java.io.File(path);
            FileContent mediaContent = new FileContent("file/txt", filePath);

//            $mime_types= array(
//                    "xls" =>'application/vnd.ms-excel',
//                    "xlsx" =>'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
//                    "xml" =>'text/xml',
//                    "ods"=>'application/vnd.oasis.opendocument.spreadsheet',
//                    "csv"=>'text/plain',
//                    "tmpl"=>'text/plain',
//                    "pdf"=> 'application/pdf',
//                    "php"=>'application/x-httpd-php',
//                    "jpg"=>'image/jpeg',
//                    "png"=>'image/png',
//                    "gif"=>'image/gif',
//                    "bmp"=>'image/bmp',
//                    "txt"=>'text/plain',
//                    "doc"=>'application/msword',
//                    "js"=>'text/js',
//                    "swf"=>'application/x-shockwave-flash',
//                    "mp3"=>'audio/mpeg',
//                    "zip"=>'application/zip',
//                    "rar"=>'application/rar',
//                    "tar"=>'application/tar',
//                    "arj"=>'application/arj',
//                    "cab"=>'application/cab',
//                    "html"=>'text/html',
//                    "htm"=>'text/html',
//                    "default"=>'application/octet-stream',
//                    "folder"=>'application/vnd.google-apps.folder'
//              );

            File file = null;
            try {
                file = service.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            file.setName(filename);
            System.out.println("File ID: " + file.getId());
            return true;
        }return false;
    }

    @Override
    public boolean copyFile(String filename, String newPath) {
        File copiedFile = new File();
        copiedFile.setName(filename);
        String parentId=getIdByName(newPath);
        copiedFile.setParents(Collections.singletonList(parentId));
        String fileid = getIdByName(filename);
        try {
            service.files().copy(fileid, copiedFile).execute();
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
        getParentId();
        fileStorage.setCurrentPath(String.valueOf(getParentId()));
        System.out.println("Trenutna putanjica " + fileStorage.getCurrentPath());

    }

    @Override
    public List<String> listFilesFromDirectory(String... extension) {
        List<String> listOfFiles = new ArrayList<>();

        FileList result = null;
        try {
            result = service.files().list()
                    .setPageSize(30)
                    .setFields("nextPageToken, files(id, name,parent)")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            //System.out.println("Files:");
            for (File file : files) {
                if (file.getParents().contains(fileStorage.getStoragePath())) {
                    //System.out.printf("%s (%s)\n", file.getName(), file.getId());
                    if (extension.length == 0) listOfFiles.add(file.getName());
                    else {
                        for (String extensionName : extension) {
                            if (file.getFileExtension() == extensionName)
                                listOfFiles.add(file.getName());
                        }
                    }
                    listOfFiles.add(file.getName());
                }
            }
            return listOfFiles;
        }
        return listOfFiles;

    }

    @Override
    public List<String> sort(String s, String... strings) {
        return null;
    }

    // Treba raditi download svaki put kad se radi login
    // time i path pokrivamo - da sa drugog skladista prvo skine users.json ali problem je ako je
    // fajl prazan - mozda ubaciti globalnu i onda setovati na 1 posle prvog logina a vratiti na 0 posle
    // logout-a? To je jedino sto mi pada na pamet da ce raditi sigurno

    @Override
    public boolean logIn(String username, String password) {
        //if (connectedUser != null)
        downloadFile("users.json");
        String path = System.getProperty("user.home") + osSeparator + "users.json";
        if (new java.io.File(path).length() == 0) {
            createUser(username, password, 1);
            connectedUser = new User(username, password, 1);
        } else {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                BufferedReader reader = new BufferedReader(new FileReader(path));
                Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
                ArrayList<User> userArray = gson.fromJson(reader, userListType);
                for (User user : userArray) {
                    if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                        connectedUser = new User(username, password, user.getLevel());
                        //fileStorage.setCurrentPath(fileStorage.getStoragePath());
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
        return true; // treba false
    }

    @Override
    public void logOut() {
        connectedUser = null;

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
    public void readConfig(String s) {

    }

    @Override
    public boolean isStorage(String s) {
        return false;
    }

    public void setConnectedUser(User connectedUser) {
        this.connectedUser = connectedUser;
    }

    public String getIdByName(String filename){
        String fileId="";
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
                    if (qq.getParents().contains(fileStorage.getCurrentPath()) && qq.getName().equals(filename))
                        fileId=qq.getId();
                    System.out.println(fileId + " = "+ qq.getId());
//                    if (fileStorage.getCurrentPath().equals(fileStorage.getStoragePath())){
//
//                    }
                }catch (Exception e){
                    System.out.println("Nisam vratio ID ");
                }
            }
        }
        return fileId;
    }



    public String getParentId(){
        String parentId=null;
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
                    if (qq.getId().contains(fileStorage.getCurrentPath()) ){

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
                    if (qq.getParents().contains(fileStorage.getCurrentPath()) && qq.getName().equals(filename))
                        return true;

                }catch (Exception e){
                    System.out.println("Nisam vratio ID ");
                }
            }
        }
        return false;

    }

}

