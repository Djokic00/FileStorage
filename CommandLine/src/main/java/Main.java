import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {

    // /home/aleksa/Desktop/aa

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Class localClass;
        SpecificationClass local = null;
        boolean firstRead = false;
        String currentPath = "";
        String osSeparator = "";
        Scanner input = new Scanner(System.in);

//        if (Files.exists(pathToStorage) == false) {
//             localClass = Class.forName("LocalImplementation");
//            local = SpecificationManager.getExporter(currentPath);
//            System.out.println("Ako zelite da napravite novo skladiste ukucajte ns i putanju do skladista");
//        } else {
//            currentPath = commandLine;
//            //localClass = Class.forName("LocalImplementation");
//            local = SpecificationManager.getExporter(currentPath);
//
//
//
//        }

        System.out.println("Enter path to the storage or make storage using ns command: ");
        String commandLine = input.nextLine();

        if (commandLine.startsWith("ns")) firstRead = true;
        else {
            Path pathToStorage = Paths.get(commandLine);
            osSeparator = File.separator;
            if (Files.exists(pathToStorage)) {
                currentPath = commandLine;
                localClass = Class.forName("LocalImplementation");
                local = SpecificationManager.getExporter(currentPath);
            }

            if (local.isStorage(commandLine)) {
                currentPath = commandLine;
                local = SpecificationManager.getExporter(currentPath);
                System.out.println("Enter username and password to connect");
                System.out.println("Username:");
                String username = input.nextLine();
                System.out.println("Password");
                String password = input.nextLine();
                local.logIn(username, password, currentPath + osSeparator + "rootDirectory");
            }
            else {
                System.out.println("Error: not a storage");
                currentPath = "";
            }
        }

        while (true) {
            if (firstRead == true) firstRead = false;
            else commandLine = input.nextLine();
            String parameters[] = commandLine.split(" ");

            if (parameters[0].equals("ns")) {
                currentPath += parameters[1];
                currentPath += osSeparator;
                System.out.println("Enter storage name");
                String storageName = input.nextLine();
                System.out.println("Set maximum size for the storage");
                Long storageSize = Long.parseLong(input.nextLine());

                local.createStorage(storageName, currentPath, storageSize);
                currentPath += storageName;
                System.out.println("Username:");
                String username = input.nextLine();
                System.out.println("Password");
                String password = input.nextLine();
                local.logIn(username, password, currentPath + osSeparator + "rootDirectory");
            }

            else if (parameters[0].equals("newuser")){
                local.createUser(parameters[1],parameters[2], Integer.parseInt(parameters[3]), currentPath + osSeparator + "rootDirectory");
                System.out.println("new user created " + parameters[1]);
            }


            else if (parameters[0].equals("cd")) {
                currentPath += osSeparator;
                currentPath += parameters[1];
                System.out.println(currentPath);
            }

            else if (parameters[0].equals("touch")) {
                if (parameters.length == 3)
                    local.createListOfFiles(parameters[1], Integer.parseInt(parameters[2]), currentPath);
                else {
                    local.createFile(parameters[1], currentPath);
                    System.out.println(currentPath);
                }
            }

            else if (parameters[0].equals("mkdir")) {
                if (parameters.length == 1) {
                    System.out.println("You must enter a name for the folder");
                }
                else if (parameters[1].equals("-res")) {
                    if (parameters.length == 4) {
                        local.createDirectory(parameters[2], currentPath, Integer.parseInt(parameters[3]));
                    }
                    else if (parameters.length == 5) {
                        local.createListOfDirRestriction(parameters[2], Integer.parseInt(parameters[3]),
                        Integer.parseInt(parameters[4]), currentPath);
                    }
                    else if (parameters.length <= 3)
                        System.out.println("Too few arguments");
                }
                else {
                    if (parameters.length == 3) {
                        local.createListOfDirectories(parameters[1], Integer.parseInt(parameters[2]), currentPath);
                    } else if (parameters.length == 2) {
                        local.createDirectory(parameters[1], currentPath);
                        System.out.println(currentPath);
                    } else if (parameters.length > 3)
                        System.out.println("Too many arguments");
                }
            }
            else if (parameters[0].equals("rm")) {
                local.deleteFile(parameters[1], currentPath);
            }

            else if (parameters[0].equals("..")) {
                String separator[] = currentPath.split(Pattern.quote(osSeparator));
                currentPath = "";
                for (int i = 0 ; i < separator.length-1 ; i++)
                {
                    currentPath += separator[i];
                    if (i != separator.length - 2) currentPath += osSeparator;
                }
                System.out.println(currentPath);
            }

            else if (parameters[0].equals("move")) {
                String currentpath = currentPath + osSeparator;
                String filename = parameters[1];
                System.out.println("Unesite putanju nove lokacije:");
                currentPath = input.nextLine();
                String path1 = currentPath + osSeparator;

                local.moveFile(filename,path1,currentpath);
                System.out.println("u metodi move sam "+ currentPath);
            }

            else if (parameters[0].equals("ls")) {
                if (parameters.length > 1) {
                    for (int i = 1; i < parameters.length; i++) {
                        local.listFilesFromDirectory(currentPath, parameters[i]);
                    }
                } else local.listFilesFromDirectory(currentPath);
            }

            else if (parameters[0].equals("sort")) {
                local.sort(currentPath, parameters[1]);
            }

            else if (parameters[0].equals("download")) {
                String filename = parameters[1];
                System.out.println("u download metodi sam " + currentPath);
                local.downloadFile(filename,currentPath);
            }

            else if (parameters[0].equals("pwd")) {
                System.out.println(currentPath);
            }

            else if (parameters[0].equals("exit")) {
                System.exit(0);
            }

            else {
                System.out.println(parameters[0] + " command not found");
            }
        }
    }
}
