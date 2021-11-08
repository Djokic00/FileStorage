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
        boolean firstRead = true;
        String currentPath = "";
        String osSeparator = File.separator;
        Scanner input = new Scanner(System.in);
        String commandLine;
        String storagePath = "";

        localClass = Class.forName("LocalImplementation");

        System.out.println("Enter path to the storage using path command or make storage using ns command: ");

        while (true) {
            commandLine = input.nextLine();
            String parameters[] = commandLine.split(" ");
            local = SpecificationManager.getExporter("");

            if (parameters[0].equals("ns")) {
                currentPath = "";
                currentPath += parameters[1];
                local = SpecificationManager.getExporter(currentPath);
                if (local.isStorage(currentPath) == false) {
                    System.out.println("Enter storage name:");
                    String storageName = input.nextLine();
                    System.out.println("Set maximum size of the storage:");
                    Long storageSize = Long.parseLong(input.nextLine());

                    local.createStorage(storageName, currentPath, storageSize);
                    currentPath += osSeparator;
                    currentPath += storageName;
                    storagePath = currentPath;
                    String username;
                    String password;
                    if (local.getConnectedUser() == null) {
                        System.out.println("Username:");
                        username = input.nextLine();
                        //nevidljiva sifra ne moze jer Intellij ne podrzava Console klasu
                        //java.io.Console console = System.console();
                        //password = new String(console.readPassword("Password: "));
                        System.out.println("Password:");
                        password = input.nextLine();
                    } else {
                        username = local.getConnectedUser().getUsername();
                        password = local.getConnectedUser().getPassword();
                    }
                    local.logIn(username, password, currentPath + osSeparator + "rootDirectory");
                }else System.out.println("Your path is already a storage.");
            }

            else if (local.getConnectedUser() != null) {
                 if (parameters[0].equals("newuser")) {
                    local.createUser(parameters[1],parameters[2], Integer.parseInt(parameters[3]), currentPath + osSeparator + "rootDirectory");
                    System.out.println("New user created: " + parameters[1]);
                }

                else if (parameters[0].equals("cd")) {
                    currentPath += osSeparator;
                    currentPath += parameters[1];
                    System.out.println("Currentpath: " + currentPath);
                }

                else if (parameters[0].equals("touch")) {
                    if (parameters.length == 3)
                        local.createListOfFiles(parameters[1], Integer.parseInt(parameters[2]), currentPath);
                    else {
                        local.createFile(parameters[1], currentPath);
                        System.out.println("Currentpath: " + currentPath);
                    }
                }

                else if (parameters[0].equals("mkdir")) {
                    if (parameters.length == 1) {
                        System.out.println("You must enter a name for the folder.");
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
                            System.out.println("Too few arguments.");
                    }
                    else {
                        if (parameters.length == 3) {
                            local.createListOfDirectories(parameters[1], Integer.parseInt(parameters[2]), currentPath);
                        } else if (parameters.length == 2) {
                            local.createDirectory(parameters[1], currentPath);
                        } else if (parameters.length > 3)
                            System.out.println("Too many arguments.");
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
                    System.out.println("Currentpath: " + currentPath);
                }

                else if (parameters[0].equals("move")) {
                    String filename = parameters[1];
                    System.out.println("Enter the path to the new location:");
                    String newPath = input.nextLine();
                    String path1 = newPath + osSeparator;
                    if (newPath.contains(storagePath) && !newPath.contentEquals(System.getProperty("user.home"))&&
                        !newPath.contains("rootDirectory")) {
                        local.moveFile(filename, path1, currentPath + osSeparator);
                    }else if(newPath.contentEquals(System.getProperty("user.home"))) {
                        System.out.println("Your path is outside of storage.");
                    } else if (newPath.contains("rootDirectory")){
                        System.out.println("You cannot move files to rootDirectory");
                    }
                    System.out.println("Currentpath: " + currentPath);
                }

                else if (parameters[0].equals("ls")) {
                    if (parameters.length > 1) {
                        for (int i = 1; i < parameters.length; i++) {
                            local.listFilesFromDirectory(currentPath, parameters[i]);
                        }
                    } else local.listFilesFromDirectory(currentPath);
                }

                else if (parameters[0].equals("sort")) {
                    if (parameters.length == 2){
                        local.sort(currentPath, parameters[1]);
                    }else{
                        local.sort(currentPath, parameters[1], parameters[2]);
                    }
                }

                else if (parameters[0].equals("edit")) {
                    local.editFile(currentPath + osSeparator + parameters[1]);
                }

                else if (parameters[0].equals("download")) {
                    String filename = parameters[1];
                    local.downloadFile(filename,currentPath);
                }

                else if (parameters[0].equals("pwd")) {
                    System.out.println("Currentpath: " + currentPath);
                }

                else if (parameters[0].equals("logout")) {
                    local.logOut();
                }

                else if (parameters[0].equals("list")) {
                    int privilege = local.getConnectedUser().getLevel();
                    if (privilege == 1)  System.out.println("ns, mkdir, newuser, touch, ls, cd, .. , " +
                            "pwd, login, logout, move, download, edit, rm, sort, exit");
                    if (privilege == 2) System.out.println("ns, mkdir, touch, ls, cd, .. , " +
                            "pwd, login, logout, move, download, edit rm, sort, exit");
                    if (privilege == 3) System.out.println("ns, mkdir, touch, ls, cd, .. , " +
                            "pwd, login, logout, edit, rm, sort, exit");
                    if (privilege == 4) System.out.println("ns, ls, cd, .. , " +
                            "pwd, login, logout, sort, exit");
                }

                else if (parameters[0].equals("login")) System.out.println("Already connected.");

                else if (parameters[0].equals("exit")) System.out.println("First disconnect using logout command.");

                else {
                    System.out.println(parameters[0] + " command not found");
                }
            }

            else if (parameters[0].equals("list")) {
                System.out.println("You can only use ns, path or login command.");
            }

            else if (parameters[0].equals("login")) {
                //currentPath=storagePath;
                storagePath=currentPath;
                //System.out.println("Storagepath: "+ storagePath);
                //storagePath je prazan
                System.out.println("Username:");
                String username = input.nextLine();
                System.out.println("Password:");
                String password = input.nextLine();

                //Ako je u nekom direktorijumu u skladistu onda ovde puca jer ne treba da mu se
                //prosledi trenutna putanja nego putanja skladista
                local.logIn(username, password, currentPath + osSeparator + "rootDirectory");
            }

            else if (parameters[0].equals("exit")) {
                System.exit(0);
            }

            else if (parameters[0].equals("path")) {
                Path pathToStorage = Paths.get(parameters[1]);
                local = SpecificationManager.getExporter(parameters[1]);

                if (Files.exists(pathToStorage) && local.isStorage(parameters[1]) ) {
                    currentPath = parameters[1];
                    String username;
                    String password;
                    if (local.getConnectedUser() == null) {
                        System.out.println("Enter username and password to connect.");
                        System.out.println("Username:");
                        username = input.nextLine();
                        System.out.println("Password:");
                        password = input.nextLine();
                    } else {
                        username = local.getConnectedUser().getUsername();
                        password = local.getConnectedUser().getPassword();
                        System.out.println(username + " " + password);
                    }

                    boolean connected = local.logIn(username, password, currentPath + osSeparator + "rootDirectory");
                    if (connected == true) System.out.println("Successfully connected!");
                    else System.out.println("Not connected!");
                        //dodaj if
                        //koji je nivo

                }
                else if (Files.exists(pathToStorage)) {
                    System.out.println("Error: not a storage");
                }

                else if (Files.exists(pathToStorage) == false) {
                    System.out.println("Incorrect path or command!");
                    currentPath = "";
                }

                else if (parameters[0].equals("exit")) System.exit(0);
            }
        }
    }


}
