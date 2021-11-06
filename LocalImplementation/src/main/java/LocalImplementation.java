import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
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
    JsonObject jsonObject = new JsonObject();
    JsonObject user = new JsonObject();
    File users;
    File config;
    String osSeparator = File.separator;


    static {
        SpecificationManager.registerExporter(new LocalImplementation());
    }

    @Override
    public void createFile(String filename, String path) {
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
                try {
                    newFile.createNewFile();
                    numberOfFilesLeft--;
                    mapOfDirRestrictions.put(path, numberOfFilesLeft);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else System.out.println("Folder is full!");
        }
        else {
            try {
                newFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //System.out.println(newFile.length());
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
        mapOfStorageSizes.put(path,storageSize);
        System.out.println(path+name);
        File storage = new File(path + name);
        storage.mkdir();
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

        //Inserting key-value pairs into the json object
//        jsonObject.addProperty("ID", "1");
//        jsonObject.addProperty("First_Name", "Shikhar");
//        try {
//            FileWriter file = new FileWriter(rootDirPath + osSeparator + "users.json");
//            file.write(jsonObject.toString());
//            file.close();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
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
            createFile(filename + i, path + osSeparator);
        }
    }

    @Override
    public void createUser(String username, String password, Integer level, String path) {
        try {


//            user.addProperty("username", username);
//            user.addProperty("password", password);
//            user.addProperty("level", String.valueOf(level));
//
//            JsonArray userList = new JsonArray();
//            userList.add(user);

            FileWriter file = new FileWriter(path + osSeparator + "users.json", true);

            //jsonObject.addProperty("user", String.valueOf(user));
            //JsonArray jsonArray=new JsonArray();
            //jsonArray.add(jsonObject);
            //file.write(jsonArray.toString());
           // System.out.println(path + osSeparator + "users.json");
           // file.write(userList.toString());
           // file.write(String.valueOf(jsonObject));
            //file.close();


            Gson gson = new Gson();
            User user = new User(username, password, level);
            String a = gson.toJson(user);
            System.out.println(a);
            file.write(a);
            file.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
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
            Path newDir=Paths.get(System.getProperty("user.home"));
            Files.copy(Paths.get(path + osSeparator + filename), newDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Kopirao sam: "+filename);

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
        file.delete();

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
        for (File files:list){
            System.out.println(files.getName());
        }
    }

    @Override
    public void editFile(String s) {

    }

    @Override
    public void logIn(String username, String password, String path) {
        //jsonObject.toString().isEmpty() == true

        if (new File(path + osSeparator + "users.json").length() == 0) {
            System.out.println("Null sam");
            createUser(username, password, 1, path);
        } else {
            //System.out.println("Ulogovao sam se " + username);
//
////            JsonArray jsonArray= user.getAsJsonArray(String.valueOf(user));
////            for (JsonElement u:jsonArray){
////                System.out.println(u.toString());
////            }
//



//            try {
//                // create Gson instance
//                Gson gson = new GsonBuilder().setLenient().create();
//
//                // create a reader
//                Reader reader = Files.newBufferedReader(Paths.get(path + osSeparator + "users.json"));
//
//                User user = gson.fromJson(reader,User.class);
//
//                // print user object
//                System.out.println(user);
//
//                // close reader
//                reader.close();
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//            Gson g = new Gson();
//
//            User person = g.fromJson("{\"username\":\"ema\",\"password\":\"123\",\"level\":1}", User.class);
//           // System.out.println(person.getUsername()); //John
//
//            System.out.println(g.toJson(person));
            System.out.println("jebem ti mater");

            try{
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                BufferedReader reader=new BufferedReader(new FileReader(path + osSeparator + "users.json"));
                //System.out.println(reader.readLine());
                Type userListType= new TypeToken<ArrayList<User>>() {}.getType();
                System.out.println("tokeni");
                ArrayList<User> userArray= gson.fromJson(reader,userListType);
                System.out.println("size:"+userArray.size());
                for (User u: userArray){
                    System.out.println(u.toString());
                }
                reader.close();
                System.out.println();
            } catch (Exception e){

            }
        }


        // ako je prazan json onda create user
        // else trazimo iz json-a da li je dobro
    }



    @Override
    public void logOut() {

    }

}
