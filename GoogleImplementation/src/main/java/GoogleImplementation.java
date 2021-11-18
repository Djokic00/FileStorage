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

///////////////////////Glavna stvar: napraviti storage.json fajl (to resava: copy, existStorage, move) ima ime i id skladista
//existsStorage proverava da li je u json fajlu
//dodati existStorage u goForward metodu

//da li nam je potrebna metoda isStorage()? to je ovde isto, a u lokalnoj razlika u jednoj liniji KAKO GOD


// download puca u jednoj liniji---->ne puca?
// list puca u jednoj liniji---> ne puca?

// copy, upload, download ne rade sa direktorijumima
// download ne radi dobro, skine prazan fajl, skrinsotovana greska


// upload proveriti prvo tip fajla da bi ga uploadovao sa sadrzajem


//PATH LOGOVANJE
// copy fora je da ne moze da kopira u skladiste ako se nalazi u folderu, popraviti to! popravlja se sa storage.json fajlov
// move: kad se metodom mkdir kreira vise foldera: fol0 fol1 fol2, ako zeli da pomeri fol0 u fol1 vraca currentpath null i ne moze da se
// krece po putanji, to ce se popraviti proverom da li je skladiste u storage.json

// config ; sort

//isStorage bice isti kod kao existsStorage

public class GoogleImplementation extends SpecificationClass implements SpecificationInterface {
    Drive service;
    String osSeparator = java.io.File.separator;
    FileStorage fileStorage = new FileStorage();
    User connectedUser;
    StringBuilder jsonForUser=new StringBuilder();
    String usersId;
    String configId;
    String storagejsonId;
    int num = 0;
    String storagename="";
    boolean usersEmpty = true;

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
        //Path storageJsonFile = Paths.get(System.getProperty("user.home")+osSeparator+"storage.json");

//        if (Files.exists(storageJsonFile)) {
//            System.out.println("ulazim");
//            if(compareStorageNames(storagename) == null){
//                System.out.println("izbacujem null");
//            }

            try {
                service = getDriveService();
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {
                storagejsonId = compareNamesToJson("storage.json");
                if (storagejsonId == "") {

                    createFile("storage.json");

                    storagejsonId = compareNamesToJson("storage.json");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            createDirectory(storagename);


            //getStorage().setCurrentPath();
            createDirectory("rootDirectory");
            try {
                createFile("users.json");
                Path downloadDir = Paths.get(System.getProperty("user.home") + osSeparator + "StorageDownloads");

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
            this.storagename = storagename;
            fileStorage.setCurrentPath(fileStorage.getStoragePath());
            System.out.println("storage path " + fileStorage.getCurrentPath());
            System.out.println("kraj metode");
            FileStorage fs = new FileStorage(fileStorage.getStoragePath(), storagename);
            writeToJsonFile("storage.json", fs);
//        }else
//            // ako uhvati exception break
//            System.out.println("already exists");
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

        if ((connectedUser == null) || connectedUser.getLevel() == 1) {

            try {
                File file = service.files().get(usersId).execute();
                //Gson gson = new Gson();
                User user = new User(username, password, level);
                writeToJsonFile("users.json", user);
//                String path = System.getProperty("user.home") + osSeparator + "users.json";
//                //java.io.File newFile = new java.io.File(path);
//                if (new java.io.File(path).length() == 0) {
//                    System.out.println("Ulazi ovde");
//                    FileWriter fileWriter = new FileWriter(path);
//                    jsonForUser.append("[");
//                    jsonForUser.append(gson.toJson(user));
//                    jsonForUser.append("]");
//                    fileWriter.write(String.valueOf(jsonForUser));
//                    fileWriter.close();
//                }
//                else {
//                    BufferedReader reader = new BufferedReader(new FileReader(path));
//                    jsonForUser = new StringBuilder(reader.readLine());
//                    jsonForUser.deleteCharAt(jsonForUser.length() - 1);
//                    jsonForUser.append(",");
//                    jsonForUser.append(gson.toJson(user));
//                    jsonForUser.append("]");
//                    FileWriter fileWriter = new FileWriter(path);
//                    fileWriter.write(String.valueOf(jsonForUser));
//                    fileWriter.close();
//                }
//                FileContent mediaContent = new FileContent(file.getMimeType(), new java.io.File(path));
//                File updated = new File();
//                service.files().update(file.getId(), updated, mediaContent).execute();
//                //newFile.delete();

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
            File file=service.files().get(fileId).execute();
            OutputStream outputStream = new FileOutputStream(System.getProperty("user.home") + osSeparator + filename);
            service.files().export(fileId,file.getMimeType()).executeMediaAndDownloadTo(outputStream);
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

        if (connectedUser.getLevel()<3) {
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
            }else {


                //String sourceFile = "zipTest";
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream("dirCompressed.zip");

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                ZipOutputStream zipOut = new ZipOutputStream(fos);
                java.io.File fileToZip = new java.io.File(path);

                try {
                    System.out.println("zovem metodu zipFile");
                    zipFile(fileToZip, fileToZip.getName(), zipOut);
                    zipOut.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }return false;
    }

    private static void zipFile(java.io.File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            java.io.File[] children = fileToZip.listFiles();
            for (java.io.File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
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
        String parentId= getParentId();
        fileStorage.setCurrentPath(parentId);
        System.out.println("Trenutna putanjica " + fileStorage.getCurrentPath());

    }

    @Override
    public List<String> listFilesFromDirectory(String... extension) {
        List<String> listOfFiles = new ArrayList<>();

        FileList result = null;

        try {
            result = service.files().list()
                    .setPageSize(50)
                    .setFields("nextPageToken, files(id, name, parents)")
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
                //System.out.println(file.getParents());
                if (file.getParents()!=null && file.getParents().contains(fileStorage.getCurrentPath())) {
                    //System.out.printf("%s (%s)\n", file.getName(), file.getId());
                    if (extension.length == 0) listOfFiles.add(file.getName());
                    else {
                        for (String extensionName : extension) {
                            if (file.getFileExtension() == extensionName)
                                listOfFiles.add(file.getName());
                        }
                    }

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
        if (usersEmpty == false) downloadFile("users.json");
        String path = System.getProperty("user.home") + osSeparator + "users.json";
        if (new java.io.File(path).length() == 0) {
            createUser(username, password, 1);
            connectedUser = new User(username, password, 1);
            usersEmpty = false;
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
        usersEmpty = true;
        java.io.File file = new java.io.File( System.getProperty("user.home") + osSeparator + "users.json");
        System.out.println(System.getProperty("user.home") + osSeparator + "users.json");
        file.delete();
        System.out.println("obrisao sam");
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
    public boolean isStorage(String storagename) {

//        Path storageJsonFile = Paths.get(System.getProperty("user.home")+osSeparator+"storage.json");
//        if (Files.exists(storageJsonFile)) {
//            System.out.println("ulazi");
//        String storageID = null;
//        try {
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            BufferedReader reader = new BufferedReader(new FileReader(
//                    System.getProperty("user.home") + osSeparator + "storage.json"));
//            Type storageListType = new TypeToken<ArrayList<FileStorage>>() {}.getType();
//            ArrayList<FileStorage> userArray = gson.fromJson(reader,storageListType);
//            for (FileStorage fs: userArray) {
//                if (fs.getStoragename().equals(storagename)) {
//                    System.out.println(storagename);
//                    return true;
//
//                }
//            }
//            reader.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        }
//        System.out.println("ne postoji storage.json");
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
            else if (obj instanceof FileStorage){
                file = service.files().get(storagejsonId).execute();
            }

            if (new java.io.File(path).length() == 0) jsonForUser = new StringBuilder();

            if (new java.io.File(path).length() == 0) {
                System.out.println("Ulazi ovde");
                FileWriter fileWriter = null;
                fileWriter = new FileWriter(path);
                jsonForUser.append("[");
                if (obj instanceof User) {
                    User user = (User) obj;
                    jsonForUser.append(gson.toJson(user));
                }
                else {
                    FileStorage fileStorage = (FileStorage) obj;
                    jsonForUser.append(gson.toJson(fileStorage));
                }
                jsonForUser.append("]");
                fileWriter.write(String.valueOf(jsonForUser));
                fileWriter.close();
            }
            else {
                BufferedReader reader = new BufferedReader(new FileReader(path));
                jsonForUser = new StringBuilder(reader.readLine());
                jsonForUser.deleteCharAt(jsonForUser.length() - 1);
                jsonForUser.append(",");
                if (obj instanceof User) {
                    User user = (User) obj;
                    jsonForUser.append(gson.toJson(user));
                }
                else {
                    FileStorage fileStorage = (FileStorage) obj;
                    jsonForUser.append(gson.toJson(fileStorage));
                }
                jsonForUser.append("]");
                FileWriter fileWriter = new FileWriter(path);
                fileWriter.write(String.valueOf(jsonForUser));
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
                    if (qq.getParents().contains(fileStorage.getCurrentPath()) && qq.getName().equals(filename)){
                        fileId=qq.getId();
                        System.out.println(fileId + " = "+ qq.getId());
                    }

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
            if (fileId=="" && compareStorageNames(filename)!=""){
                //prodje kroz json
                fileId= compareStorageNames(filename);
                System.out.println(fileId);
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
        String storageID = null;
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            BufferedReader reader = new BufferedReader(new FileReader(
                    System.getProperty("user.home") + osSeparator + "storage.json"));
            Type storageListType = new TypeToken<ArrayList<FileStorage>>() {}.getType();
            ArrayList<FileStorage> userArray = gson.fromJson(reader,storageListType);
            for (FileStorage fs: userArray) {
                if (fs.getStoragename().equals(storagename)) {
                    System.out.println(storagename);
                    return storageID=fs.getStoragePath();

                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return storageID;
    }




}

