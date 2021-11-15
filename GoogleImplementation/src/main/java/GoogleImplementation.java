import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.FileStorage;
import model.User;

public class GoogleImplementation extends SpecificationClass implements SpecificationInterface {
    Drive service;
    String osSeparator = java.io.File.separator;
    FileStorage fileStorage = new FileStorage();
    User connectedUser;
    StringBuilder jsonForUser=new StringBuilder();
    String usersid;
    String configid;

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

        String folderId= fileStorage.getCurrentPath();
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
        //file.setName(filename);
        //done
        if (filename.contains("users.json"))
            usersid=file.getId();
        if (filename.contains("config.json"))
            configid=file.getId();
    }

    @Override
    public void createDirectory(String directoryname, Integer... integers) {
        File fileMetadata = new File();
        fileMetadata.setName(directoryname);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        if (fileStorage.getCurrentPath()!=null){
            fileMetadata.setParents(Collections.singletonList(fileStorage.getCurrentPath()));
            System.out.println("setuje parenta");

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
        if (fileStorage.getCurrentPath()==null){
        fileStorage.setStoragePath(file.getId());
        }
        fileStorage.setCurrentPath(file.getId());


    }

    @Override
    public void createStorage(String s, String storagename, Long aLong, String... strings) {
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
            createFile("config.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileStorage.setCurrentPath(fileStorage.getCurrentPath());


    }

    @Override
    public void createListOfDirectories(String s, Integer integer) {

    }

    @Override
    public void createListOfDirRestriction(String s, Integer integer, Integer integer1) {

    }

    @Override
    public void createListOfFiles(String s, Integer integer) {

    }


    @Override
    public void createUser(String username, String password, Integer level) {

//        try {
//            File file = service.files().get(usersid).execute();
//            java.io.File fileContent = new java.io.File("/home/aleksa/Desktop/novifajl.json");
//            FileContent mediaContent = new FileContent(file.getMimeType(), fileContent);
//            File updated = new File();
//            service.files().update(file.getId(), updated, mediaContent).execute();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


//        try {
//            Gson gson = new Gson();
//            User user = new User(username, password, level);
//            if (new File().setId(usersid).size() == 0) {
//                FileWriter file = new FileWriter(usersid);
//                jsonForUser.append("[");
//                jsonForUser.append(gson.toJson(user));
//                jsonForUser.append("]");
//                file.write(String.valueOf(jsonForUser));
//                file.close();
//            }
//            else {
//                BufferedReader reader = new BufferedReader(new FileReader(usersid));
//                jsonForUser = new StringBuilder(reader.readLine());
//                jsonForUser.deleteCharAt(jsonForUser.length() - 1);
//                jsonForUser.append(",");
//                jsonForUser.append(gson.toJson(user));
//                jsonForUser.append("]");
//                //System.out.println(jsonString);
//                FileWriter file = new FileWriter(usersid);
//                file.write(String.valueOf(jsonForUser));
//                file.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    public void moveFile(String s, String s1) {
        //done

    }

    @Override
    public void editFile(String s) {

    }

    @Override
    public boolean downloadFile(String s) {
        File fileMetadata4 = new File();
        fileMetadata4.setName("glupost.txt");
        java.io.File filePath4 = new java.io.File("C:\\Users\\estoj\\glupost.txt");
        FileContent mediaContent4 = new FileContent("file/txt", filePath4);
        File txt = null;
        try {
            txt = service.files().create(fileMetadata4, mediaContent4)
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        txt.setName("glupost.txt");
        System.out.println("File ID: " + txt.getId());
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(System.getProperty("user.home") + osSeparator + "Downloads" +
                    osSeparator + txt.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            service.files().get(txt.getId()).executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("skinuo sam fajlic " + txt.getId());
        return true;
    }

    @Override
    public boolean uploadFile(String s) {
        return false;
    }

    @Override
    public boolean copyFile(String s, String s1) {
        return false;
    }

    @Override
    public void deleteFile(String s) {
        //done

    }

    @Override
    public boolean goForward(String s) {
        return false;
    }

    @Override
    public void goBackwards() {

    }

    @Override
    public List<String> listFilesFromDirectory(String... extension) {
//        List<String> listOfFiles = new ArrayList<>();
//
//
//        FileList result = null;
//        try {
//            result = service.files().list()
//                    .setPageSize(10)
//                    .setFields("nextPageToken, files(id, name)")
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
//                //System.out.printf("%s (%s)\n", file.getName(), file.getId());
//                if (extension.length == 0) listOfFiles.add(file.getName());
//                else {
//                    for (String extensionName : extension) {
//                        if (file.getFileExtension()== extensionName)
//                            listOfFiles.add(file.getName());
//                    }
//                }
//                listOfFiles.add(file.getName());
//            }
//            return listOfFiles;
//        }
//        return listOfFiles;
        return null;
    }

    @Override
    public List<String> sort(String s, String... strings) {
        return null;
    }

    @Override
    public boolean logIn(String username, String password) {
        //if (new File().get(usersid).isEmpty()) {
        try {
            File file = service.files().get(usersid).execute();
            System.out.println(file.size());
            System.out.println("Title: " + file.getName());
            System.out.println("Description: " + file.getDescription());
            System.out.println("MIME type: " + file.getMimeType());
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }
        createUser(username, password, 1);
            connectedUser = new User(username,password,1);
           // fileStorage.setCurrentPath(fileStorage.getStoragePath());
            return true;

 //       }
//        else {
//            try {
//                Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                BufferedReader reader = new BufferedReader(new FileReader(usersid));
//                Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
//                ArrayList<User> userArray = gson.fromJson(reader,userListType);
//                for (User user: userArray) {
//                    if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
//                        connectedUser = new User(username, password, user.getLevel());
//                        //fileStorage.setCurrentPath(fileStorage.getStoragePath());
//                        return true;
//
//                    }
//                }
//                reader.close();
//            } catch (Exception e){
//                e.printStackTrace();
//            }
//        }
      //  return false;
    }

    @Override
    public void logOut() {

    }

    @Override
    public User getConnectedUser() {
        return null;
    }

    @Override
    public FileStorage getStorage() {
        return null;
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

}

