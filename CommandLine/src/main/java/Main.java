import Exceptions.UnauthorizedActionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    // /home/aleksa/Desktop/aa
    // C:\Users\estoj


    // pitanje2: gde ce nam biti napisana lista metoda vezana za privilegije? (Koje sve moze metode da pozove
    //           korisnik sa odredjenom prvilegijom) Da li to stoji u dokumentaciji?
    // pitanje4: ako ime foldera/fajla ima vise reci, kako cemo to parsirati? da li da ime korisnik unosi pod
    //           navodnicima-> npr. cd "nov folder" ili da posle metodice stavi 2 tackice-> cd:novi folder
    // pitanje4*: kako cu pratiti koliko argumenata ima niz kad ga parsiramo ako se u imenu nalazi razmak?
    //            pitam zbog exceptiona? da ostane ovako???



    //    detalj: moze a ne mora (dodati na kraju projekta ako ostane visak vremena)
    //    dati primere komandi u komandnoj liniji -> treba da bude jasno osobi koja koristi program prvi put
    //    neka to bude ili lista primera
    //    zove se kad osoba ukuca help, dobije nazad primere kako se koriste komande


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Class localClass;
        SpecificationClass local = null;
        String currentPath = "";
        String osSeparator = File.separator;
        Scanner input = new Scanner(System.in);
        String commandLine;
        localClass = Class.forName("GoogleImplementation");
       // localClass = Class.forName("LocalImplementation");

        System.out.println("Enter path to the storage using path command or make a storage using ns command: ");


        while (true) {
            commandLine = input.nextLine();
            String parameters[] = commandLine.split(" ");
            local = SpecificationManager.getExporter("");

            if (parameters[0].equals("ns")) {
                try {
                    if (parameters.length == 1)
                        System.out.println("Error: You must enter a path.");
                    currentPath = "";
                    currentPath += parameters[1];
                    local = SpecificationManager.getExporter(currentPath);
                    if (local.isStorage(currentPath) == false) {
                        System.out.println("Set maximum size of the storage:");
                        Long storageSize = Long.parseLong(input.nextLine());
                        System.out.println("Set storage restriction or type n to abort:");
                        String restriction = input.nextLine();
                        if (restriction.equals("n")) local.createStorage(currentPath, storageSize);
                        else local.createStorage(currentPath, storageSize, restriction);
                       // System.out.println("Storage path is " + local.getStorage().getStoragePath());
                        String username;
                        String password;
                        if (local.getConnectedUser() == null) {
                            System.out.println("Username:");
                            username = input.nextLine();
                            System.out.println("Password:");
                            password = input.nextLine();
                        } else {
                            username = local.getConnectedUser().getUsername();
                            password = local.getConnectedUser().getPassword();
                        }
                        local.logIn(username, password);
                    } else System.out.println("Your path is already a storage.");

                    System.out.println("Successfully connected! Level: " + local.getConnectedUser().getLevel());
                } catch (Exception e) {
                    System.out.println("Too many or too few arguments.");
                }
            } else if (local.getConnectedUser() != null) {
                if (parameters[0].equals("newuser")) {
                    try {
                        if (parameters.length < 4)
                            System.out.println("Error: You must enter a username, password and privilege.");
                        local.createUser(parameters[1], parameters[2], Integer.parseInt(parameters[3]));
                        System.out.println("New user created: " + parameters[1]);
                        if (parameters.length > 5)
                            System.out.println("Too many arguments.");
                    } catch (Exception e) {
                        System.out.println("Too many or too few arguments.");
                    }
                } else if (parameters[0].equals("cd")) {
                    try {
                        if (parameters.length == 1) {
                            System.out.println("Error: You must enter a name for the folder.");
                            continue;
                        }
                        if (local.goForward(parameters[1])) {
                            System.out.println("Currentpath: " + local.getStorage().getCurrentPath());
                        } else {
                            System.out.println("Username:");
                            String username = input.nextLine();
                            System.out.println("Password:");
                            String password = input.nextLine();
                            boolean connected = local.logIn(username, password);
                            if (connected == true) System.out.println("Successfully connected! Level: " + local.getConnectedUser().getLevel());
                            else System.out.println("Not connected!");
                        }
                    } catch (Exception e) {
                        System.out.println("Too many or too few arguments.");
                    }

                } else if (parameters[0].equals("touch")) {
                    System.out.println("Ucitao sam touch");
                    try {
                        if (parameters.length == 1) {
                            System.out.println("Error: You must enter a name for the file.");
                            continue;
                        }

                        if (parameters.length == 3)
                            local.createListOfFiles(parameters[1], Integer.parseInt(parameters[2]));
                        else {
                            local.createFile(parameters[1]);
                            //System.out.println("Currentpath: " + local.getStorage().getCurrentPath());
                        }
                    } catch (UnauthorizedActionException e){
                        System.out.println(e.getMessage());
                    }
//                    catch (Exception e) {
//                        System.out.println("Too many or too few arguments.");
//                    }
                } else if (parameters[0].equals("mkdir")) {
                    try {
                        if (parameters.length == 1) {
                            System.out.println("Error: You must enter a name for the folder.");
                            continue;
                        } else if (parameters[1].equals("-res")) {
                            if (parameters.length == 4) {
                                local.createDirectory(parameters[2], Integer.parseInt(parameters[3]));
                            } else if (parameters.length == 5) {
                                local.createListOfDirRestriction(parameters[2], Integer.parseInt(parameters[3]),
                                        Integer.parseInt(parameters[4]));
                            } else if (parameters.length <= 3)
                                System.out.println("Too few arguments.");
                        } else {
                            if (parameters.length == 3) {
                                local.createListOfDirectories(parameters[1], Integer.parseInt(parameters[2]));
                            } else if (parameters.length == 2) {
                                local.createDirectory(parameters[1]);
                            } else if (parameters.length > 3)
                                System.out.println("Too many arguments.");
                        }
                    } catch (Exception e) {
                        System.out.println("Too many or too few arguments.");
                    }
                } else if (parameters[0].equals("rm")) {
                    try {
                        if (parameters.length == 1)
                            System.out.println("Error: You must enter a name for the folder or file.");
                        local.deleteFile(parameters[1]);
                    } catch (Exception e) {
                        System.out.println("Too many or too few arguments.");
                    }
                }
                else if (parameters[0].equals("..")) {
                        local.goBackwards();
                        System.out.println("Currentpath: " + local.getStorage().getCurrentPath());
                    } else if (parameters[0].equals("move")) {
                        try {
                            if (parameters.length == 1)
                                System.out.println("Error: You must enter a name for the file.");
                            String filename = parameters[1];
                            System.out.println("Enter the path to the new location:");
                            String newPath = input.nextLine();
                            local.moveFile(filename, newPath);
                        } catch (Exception e) {
                            System.out.println("Too many or too few arguments.");
                        }
                    } else if (parameters[0].equals("ls")) {
                        try {
                            List <String> list=new ArrayList<>();
                            if (parameters.length > 1) {
                                for (int i = 1; i < parameters.length; i++) {
                                    list=local.listFilesFromDirectory(parameters[i]);
                                }
                            } else list= local.listFilesFromDirectory();
                            for (String l: list){
                                System.out.println(l);
                            }
                        } catch (Exception e) {
                            System.out.println("Too many or too few arguments.");
                        }
                    } else if (parameters[0].equals("sort")) {
                        try {
                            List<String> listOfFiles=new ArrayList<>();
                            if (parameters.length == 1)
                                System.out.println("Error: You must enter asc or desc.");
                            if (parameters.length == 2) {
                                listOfFiles=local.sort(parameters[1]);
                            } else {
                                listOfFiles=local.sort(parameters[1], parameters[2]);
                            }
                            for (String f: listOfFiles){
                                System.out.println(f);
                            }
                        } catch (Exception e) {
                            System.out.println("Too many or too few arguments.");
                        }
                    } else if (parameters[0].equals("edit")) {
                        try {
                            if (parameters.length == 1)
                                System.out.println("Error: You must enter a name for the file.");
                            local.editFile(parameters[1]);
                        } catch (Exception e) {
                            System.out.println("Too many or too few arguments.");
                        }
                    } else if (parameters[0].equals("copy")) {
                        try {
                            if (parameters.length == 1)
                                System.out.println("Error: You must enter a name for the file.");
                            String filename = parameters[1];
                            System.out.println("Enter the path to the new location:");
                            String newPath = input.nextLine();
                            if (local.copyFile(filename, newPath)) {
                                System.out.println("Copied: " + filename);
                            }

                        } catch (Exception e) {
                            System.out.println("Too many or too few arguments.");
                        }
                    } else if (parameters[0].equals("download")) {
                        try {
                            if (parameters.length == 1)
                                System.out.println("Error: You must enter a name for the file.");
                            //String filename = parameters[1];
                            if (local.downloadFile(parameters[1])) {
                                System.out.println("Download completed: " + parameters[1]);
                            }

                        } catch (Exception e) {
                            System.out.println("Too many or too few arguments.");
                        }
                    } else if (parameters[0].equals("upload")) {
                        try {
                            if (parameters.length == 1)
                                System.out.println("Error: You must enter a name for the file.");
                             String filename = parameters[1];
                            if (local.uploadFile(parameters[1])) {
                                System.out.println("Upload completed: " + parameters[1]);
                            }

                        } catch (Exception e) {
                            System.out.println("Too many or too few arguments.");
                        }
                    } else if (parameters[0].equals("pwd")) {
                        System.out.println("Currentpath: " + local.getStorage().getCurrentPath());
                    } else if (parameters[0].equals("logout")) {
                        local.logOut();
                    } else if (parameters[0].equals("list")) {
                        int privilege = local.getConnectedUser().getLevel();
                        if (privilege == 1) System.out.println("ns, mkdir, newuser, touch, ls, cd, .. , " +
                                "pwd, login, logout, move, download, edit, rm, sort, exit");
                        if (privilege == 2) System.out.println("ns, mkdir, touch, ls, cd, .. , " +
                                "pwd, login, logout, move, download, edit rm, sort, exit");
                        if (privilege == 3) System.out.println("ns, mkdir, touch, ls, cd, .. , " +
                                "pwd, login, logout, edit, rm, sort, exit");
                        if (privilege == 4) System.out.println("ns, ls, cd, .. , " +
                                "pwd, login, logout, sort, exit");
                    } else if (parameters[0].equals("login")) System.out.println("Already connected.");

                    else if (parameters[0].equals("exit")) System.out.println("First disconnect using logout command.");

                    else {
                        System.out.println(parameters[0] + " command not found");
                    }
                } else if (parameters[0].equals("list")) {
                    System.out.println("You can only use ns, path or login command.");

                } else if (parameters[0].equals("login")) {
                    //currentPath = local.getStorage().getPath();
                    System.out.println("Storage path is " + local.getStorage().getStoragePath());
                    System.out.println("Username:");
                    String username = input.nextLine();
                    System.out.println("Password:");
                    String password = input.nextLine();
                    if (local.logIn(username, password)) {
                        System.out.println("Connected user: " + local.getConnectedUser().getUsername()
                                + "; Privilege: " + local.getConnectedUser().getLevel());
                    }


                } else if (parameters[0].equals("exit")) {
                    System.exit(0);
                } else if (parameters[0].equals("path")) {
                    try {
                        if (parameters.length == 1) System.out.println("Error: You must enter the path.");
                        Path pathToStorage = Paths.get(parameters[1]);
                        local = SpecificationManager.getExporter(parameters[1]);

                        if (Files.exists(pathToStorage) && local.isStorage(parameters[1])) {
                            local.readConfig(parameters[1]);
                            //local.getStorage().setStoragePath(parameters[1]);
                            //local.getStorage().setCurrentPath(parameters[1]);

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

                            boolean connected = local.logIn(username, password);
                            if (connected == true) {
                                System.out.println("Successfully connected! Level: " + local.getConnectedUser().getLevel());
                            }
                            else System.out.println("Not connected!");

                        } else if (Files.exists(pathToStorage)) {
                            System.out.println("Error: not a storage");
                        } else if (Files.exists(pathToStorage) == false) {
                            System.out.println("Incorrect path or command!");
                        } else if (parameters[0].equals("exit")) System.exit(0);
                    } catch (Exception e) {
                        System.out.println("Too many or too few arguments.");
                    }
                }
            }
        }
    }