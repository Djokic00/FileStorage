import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import model.FileStorage;
import model.User;

public class GoogleImplementation extends SpecificationClass implements SpecificationInterface{

    static {
        SpecificationManager.registerExporter(new GoogleImplementation());
    }

    private static Drive service;

    static {
        try {
            service = getDriveService();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public void createFile(String s) throws IOException {
        //done
    }

    @Override
    public void createDirectory(String directoryname, Integer... integers) {
        //done
        File fileMetadata = new File();
        fileMetadata.setName(directoryname);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        File file = null;
        try {
            file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Folder ID: " + file.getId());

    }

    @Override
    public void createStorage(String storagename, String s1, Long aLong, String... strings) {
            createDirectory(storagename);
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
    public void createUser(String s, String s1, Integer integer) {

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
        return false;
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
        List<String> listOfFiles = new ArrayList<>();


        FileList result = null;
        try {
            result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
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
                //System.out.printf("%s (%s)\n", file.getName(), file.getId());
                if (extension.length == 0) listOfFiles.add(file.getName());
                else {
                    for (String extensionName : extension) {
                        if (file.getFileExtension()== extensionName)
                            listOfFiles.add(file.getName());
                    }
                }
                listOfFiles.add(file.getName());
            }
            return listOfFiles;
        }
        return listOfFiles;
    }

    @Override
    public List<String> sort(String s, String... strings) {
        return null;
    }

    @Override
    public boolean logIn(String s, String s1) {
        return false;
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
}

