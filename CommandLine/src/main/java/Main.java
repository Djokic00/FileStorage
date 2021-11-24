import Exceptions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String currentPath = "";
        Scanner input = new Scanner(System.in);
        String commandLine;
        Class.forName("GoogleImplementation");
//        Class.forName("LocalImplementation");
        SpecificationClass local;

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
                        try {
                            if (restriction.equals("n")) local.createStorage(currentPath, storageSize);
                            else local.createStorage(currentPath, storageSize, restriction);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
                        if (local.logIn(username, password)) {
                            System.out.println("Successfully connected! Level: " + local.getConnectedUser().getLevel());
                        } else System.out.println("Not connected");
                    } else System.out.println("Your path is already a storage.");
                } catch (UnauthorizedException e) {
                    System.out.println(e.getMessage());
                }catch (NumberFormatException e){

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
                    } catch (UnauthorizedException e){
                        System.out.println(e.getMessage());
                    }
                    catch (Exception e) {
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
                            if (connected) System.out.println("Successfully connected! Level: " + local.getConnectedUser().getLevel());
                            else System.out.println("Not connected!");
                        }
                    } catch (Exception e) {
                        System.out.println("Too many or too few arguments.");
                    }

                } else if (parameters[0].equals("touch")) {
                    try {
                        if (parameters.length == 1) {
                            System.out.println("Error: You must enter a name for the file.");
                            continue;
                        }
                        if (parameters.length == 2)
                            local.createFile(parameters[1]);
                        else
                            local.createListOfFiles(parameters[1], Integer.parseInt(parameters[2]), local.getConnectedUser().getLevel());

                    } catch (UnauthorizedException | FolderException | ForbiddenExtensionException e) {
                        System.out.println(e.getMessage());
                    }
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
                                        Integer.parseInt(parameters[4]), local.getConnectedUser().getLevel());
                            } else if (parameters.length <= 3)
                                System.out.println("Too few arguments.");
                        } else {
                            if (parameters.length == 3) {
                                local.createListOfDirectories(parameters[1], Integer.parseInt(parameters[2]),
                                        local.getConnectedUser().getLevel());
                            } else if (parameters.length == 2) {
                                local.createDirectory(parameters[1]);
                            } else if (parameters.length > 3)
                                System.out.println("Too many arguments.");
                        }
                    } catch (StorageException | FolderException | UnauthorizedException | NumberFormatException e) {
                        System.out.println(e.getMessage());
                    }
                } else if (parameters[0].equals("rm")) {
                    try {
                        if (parameters.length == 1)
                            System.out.println("Error: You must enter a name for the folder or file.");
                        local.deleteFile(parameters[1]);
                    }catch (FileNotFoundException | UnauthorizedException e) {
                        System.out.println(e.getMessage());
                    }

                    catch (Exception e) {
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
                            if (parameters.length == 2) {
                                String filename = parameters[1];
                                System.out.println("Enter the path to the new location:");
                                String newPath = input.nextLine();
                                local.moveFile(filename, newPath);
                            } else if (parameters.length > 2) {
                                String firstfilename = parameters[1];
                                String[] lof = new String[parameters.length - 2];
                                for (int i = 2; i < (parameters.length); i++) {
                                    String p = parameters[i];
                                    lof[i - 2] = p;
                                }
                                System.out.println("Enter the path to the new location:");
                                String newPath = input.nextLine();
                                System.out.println(newPath);
                                local.moveFile(firstfilename, newPath, lof);
                            }
                        }catch (CantChangeRootException | UnauthorizedException e){
                            System.out.println(e.getMessage());
                        }
                        catch (Exception e) {
                            System.out.println("Too many or too few arguments.");
                        }
                        }else if (parameters[0].equals("ls")) {

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
                            List<String> listOfFiles;
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
                        } catch (UnauthorizedException | UnsupportedOperation e){
                            System.out.println(e.getMessage());
                        }
                        catch (Exception e) {
                            System.out.println("Too many or too few arguments.");
                        }
                    } else if (parameters[0].equals("copy")) {
                        try {
                            if (parameters.length == 1)
                                System.out.println("Error: You must enter a name for the file.");

                            if (parameters.length == 2) {
                                String filename = parameters[1];
                                System.out.println("Enter the path to the new location:");
                                String newPath = input.nextLine();
                                if (local.copyFile(filename, newPath)) {
                                    System.out.println("Copied: " + filename);
                                }
                            } else if (parameters.length > 2) {
                                String firstfilename = parameters[1];
                                String[] lof = new String[parameters.length - 2];
                                for (int i = 2; i < (parameters.length); i++) {
                                    String p = parameters[i];
                                    lof[i - 2] = p;
                                }
                                System.out.println("Enter the path to the new location:");
                                String newPath = input.nextLine();
                                System.out.println(newPath);
                                local.copyFile(firstfilename, newPath, lof);

                            }
                        }catch (CantChangeRootException | UnauthorizedException | StorageException e){
                                System.out.println(e.getMessage());
                        }
                        catch (Exception e) {
                            System.out.println("Too many or too few arguments.");
                        }
                    } else if (parameters[0].equals("download")) {
                        try {
                            if (parameters.length == 1)
                                System.out.println("Error: You must enter a name for the file.");
                            if (local.downloadFile(parameters[1])) {
                                System.out.println("Download completed: " + parameters[1]);
                            }

                        }catch (UnauthorizedException e){
                            System.out.println(e.getMessage());
                        }
                        catch (Exception e) {
                            System.out.println("Too many or too few arguments.");
                        }
                    } else if (parameters[0].equals("upload")) {
                        try {
                            if (parameters.length == 1)
                                System.out.println("Error: You must enter a name for the file.");

                            if (parameters.length == 2) {
                                if (local.uploadFile(parameters[1])) {
                                    System.out.println("Upload completed: " + parameters[1]);
                                }
                            }

                        } catch (CantChangeRootException | UnauthorizedException | StorageException e) {
                            System.out.println(e.getMessage());
                        }
                    } else if (parameters[0].equals("pwd")) {
                        System.out.println("Currentpath: " + local.getCurrentLocation());
                    } else if (parameters[0].equals("logout")) {
                        local.logOut();
                    } else if (parameters[0].equals("list")) {
                        int privilege = local.getConnectedUser().getLevel();
                        if (privilege == 1) System.out.println("ns, mkdir, newuser, touch, ls, cd, .. , " +
                                "pwd, logout, move, download, edit, rm, sort, exit");
                        if (privilege == 2) System.out.println("ns, mkdir, touch, ls, cd, .. , " +
                                "pwd, logout, move, download, edit rm, sort, exit");
                        if (privilege == 3) System.out.println("ns, mkdir, touch, ls, cd, .. , " +
                                "pwd, logout, edit, rm, sort, exit");
                        if (privilege == 4) System.out.println("ns, ls, cd, .. , " +
                                "pwd, logout, sort, exit");
                    }

                    else if (parameters[0].equals("exit")) System.out.println("First disconnect using logout command.");

                    else {
                        System.out.println(parameters[0] + " command not found");
                    }
                } else if (parameters[0].equals("list")) {
                    System.out.println("You can only use ns or path command.");

//                } else if (parameters[0].equals("login")) {
//                    //currentPath = local.getStorage().getPath();
//                    System.out.println("Storage path is " + local.getStorage().getStoragePath());
//                    System.out.println("Username:");
//                    String username = input.nextLine();
//                    System.out.println("Password:");
//                    String password = input.nextLine();
//                try {
//                    if (local.logIn(username, password)) {
//                        System.out.println("Connected user: " + local.getConnectedUser().getUsername()
//                                + "; Privilege: " + local.getConnectedUser().getLevel());
//                    }
//                } catch (UnauthorizedException e) {
//                    //e.printStackTrace();
//                    System.out.println(e.getMessage());
//                }

            } else if (parameters[0].equals("exit")) {
                    System.exit(0);
                } else if (parameters[0].equals("path")) {
                    try {
                        if (parameters.length == 1) System.out.println("Error: You must enter the path.");
                        //currentPath=parameters[1];

                        local = SpecificationManager.getExporter(parameters[1]);

                        if (local.isStorage(parameters[1]) == true){
                            local.readConfig(parameters[1]);
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
                            if (connected) {
                                System.out.println("Successfully connected! Level: " + local.getConnectedUser().getLevel());
                            }
                            else System.out.println("Not connected!");

                        }
                        else if (parameters[0].equals("exit")) System.exit(0);

                    } catch (Exception e) {
                        System.out.println("Too many or too few arguments.");
                       // e.printStackTrace();
                    }
                }
            }
        }
    }