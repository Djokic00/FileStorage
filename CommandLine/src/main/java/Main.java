import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    static LocalImplementation local = new LocalImplementation();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String path = "";
        String os = "";

        Scanner in = new Scanner(System.in);
        System.out.println("Unesite putanju do skladista: ");
        String s = in.nextLine();


        Path putanja = Paths.get(s);
        System.out.println("separator: "+ File.separator);
        os=File.separator;


        if (Files.exists(putanja) == false) {
            System.out.println("Ako zelite da napravite novo skladiste ukucajte ns i putanju do skladista");
        } else path = s;

        while (true) {
            s = in.nextLine();
            String niz[] = s.split(" ");

            if (niz[0].equals("ns")) {
                path += niz[1];
                path += os;
                System.out.println("Unesite ime");
                String ime = in.nextLine();
                System.out.println("Unesite velicinu skladista");
                Long storageSize = Long.parseLong(in.nextLine());
                local.createStorage(ime, path, storageSize);
                //path+=os;
                path+=ime;
            }

            if (niz[0].equals("cd")) {
                path += os;
                path += niz[1];
                System.out.println(path);
            }
            else if (niz[0].equals("touch")) {
               // String path1=path+os;
                if (niz.length == 3) {
                    local.createListOfFiles(niz[1], Integer.parseInt(niz[2]), path);
                    //System.out.println(path1);
                }
                else {
                    //path += os;
                    //Class local = Class.forName("LocalImplementation");// = new LocalImplementation();
                    local.createFile(niz[1], path);
                    System.out.println(path);
                }
            }

            else if (niz[0].equals("mkdir")) {
                if(niz[1].equals("-res")){
                   // String path1=path+os;
                    if (niz.length == 4) {
                        local.createListOfDirectories(niz[2], Integer.parseInt(niz[3]), path);
                        //System.out.println(path1);
                    }else if(niz.length==5){
                        local.createListOfDirectories(niz[2], Integer.parseInt(niz[3]), path,Integer.parseInt(niz[4]));
                    }
                    else {

                        local.createDirectory(niz[2], path,Integer.parseInt(niz[3]));
                        System.out.println(path);
                    }
                }else {

                    //String path1 = path + os;
                    if (niz.length == 3) {
                        local.createListOfDirectories(niz[1], Integer.parseInt(niz[2]), path);
                        //System.out.println(path1);
                    } else {
                        //path += os;
                        //Class local = Class.forName("LocalImplementation");// = new LocalImplementation();
                        System.out.println("evo me");
                        System.out.println(niz[1]);
                        local.createDirectory(niz[1], path);
                        System.out.println(path);
                    }
                }
//                // String path1= path+ os;
//                path+=os;
//                String name = niz[1];
//                //Class local = Class.forName("LocalImplementation");// = new LocalImplementation();
//                local.createDirectory(name,path);
//
//                path+=name;
//                System.out.println(path);
            }

//            else if (niz[0].equals("mkdir -res")){
//                String path1=path+os;
//                if (niz.length == 4) {
//                    local.createListOfDirectories(niz[2], Integer.parseInt(niz[3]), path1);
//                    //System.out.println(path1);
//                }else if(niz.length==5){
//                    local.createListOfDirectories(niz[2], Integer.parseInt(niz[3]), path1,Integer.parseInt(niz[4]));
//                }
//                else {
//
//                    local.createDirectory(niz[2], path1,Integer.parseInt(niz[3]));
//                    System.out.println(path);
//                }
//            }


            else if (niz[0].equals("rm")) {
                //String path1=path+ os;
                local.deleteFile(niz[1],path);
            }

            if (niz[0].equals("..")) {
                String niz1[] = path.split(Pattern.quote(os));
                path = "";
                for (int i=0 ; i < niz1.length-1 ; i++)
                {
                    path += niz1[i];
                    if (i != niz1.length - 2) path += os;
                }
                System.out.println(path);
            }
            if (niz[0].equals("move")) {
                String currentpath=path+os;

                //move filename
                String filename=niz[1];
                System.out.println("Unesite putanju nove lokacije:");
                path =in.nextLine();
                String path1=path+os;

                local.moveFile(filename,path1,currentpath);
                System.out.println("u metodi move sam "+ path);
            }
            if (niz[0].equals("ls")) {
                if(niz.length>1){
                    for (int i=1; i< niz.length; i++){
                        local.listFilesFromDirectory(path,niz[i]);
                    }
                }else local.listFilesFromDirectory(path);


            }
            if (niz[0].equals("sort")){
                try {
                    local.sort(path, niz[1]);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (niz[0].equals("download")) {
                String filename=niz[1];

                System.out.println("u download metodi sam "+path);
                local.downloadFile(filename,path);
            }
            if (niz[0].equals("?")) {
                System.out.println(path);
            }
            if (niz[0].equals("exit")) {
                System.exit(0);
            }

        }

        //noviFajl.mkdir();
    }
}
