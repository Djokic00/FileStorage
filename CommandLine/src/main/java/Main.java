import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    //static LocalImplementation local = new LocalImplementation();

    public static void main(String[] args) throws ClassNotFoundException {

        String currentPath = "";
        String osSeparator = "";

        Scanner input = new Scanner(System.in);
        System.out.println("Unesite putanju do skladista: ");
        String commandLine = input.nextLine();


        Path pathToStorage = Paths.get(commandLine);
        osSeparator = File.separator;


        if (Files.exists(pathToStorage) == false) {
//            System.out.println("Username:");
//            String username=input.nextLine();
//            System.out.println("Password:");
//            String password=input.nextLine();
//            local.logIn(username,password);
            System.out.println("Ako zelite da napravite novo skladiste ukucajte ns i putanju do skladista");
        } else currentPath = commandLine;

        Class localClass = Class.forName("LocalImplementation");
        SpecificationClass local= SpecificationManager.getExporter(currentPath);

        while (true) {
            commandLine = input.nextLine();
            String parameters[] = commandLine.split(" ");

            if (parameters[0].equals("ns")) {
                currentPath += parameters[1];
                currentPath += osSeparator;
                System.out.println("Unesite ime");
                String ime = input.nextLine();
                System.out.println("Unesite velicinu skladista");
                Long storageSize = Long.parseLong(input.nextLine());

                local.createStorage(ime, currentPath, storageSize);
                currentPath+=ime;
                System.out.println("Username:");
                String username=input.nextLine();

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

                    // = new LocalImplementation();
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
                        System.out.println("Too few arguments. Try 'mkdir --help' for more information");
                }
                else {
                    if (parameters.length == 3) {
                        local.createListOfDirectories(parameters[1], Integer.parseInt(parameters[2]), currentPath);
                    } else if (parameters.length == 2) {
                        //Class local = Class.forName("LocalImplementation");
                        local.createDirectory(parameters[1], currentPath);
                        System.out.println(currentPath);
                    } else if (parameters.length > 3)
                        System.out.println("Too many arguments.Try 'mkdir --help' for more information");
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
                String currentpath = currentPath+osSeparator;

                //move filename
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
                try {
                    local.sort(currentPath, parameters[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
