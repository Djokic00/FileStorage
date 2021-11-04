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
            String str = in.nextLine();
            String niz[] = str.split(" ");

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
                String path1=path+os;
                //path += os;
                //Class local = Class.forName("LocalImplementation");// = new LocalImplementation();
                local.createFile(niz[1],path1);
                System.out.println(path);
            }
            else if (niz[0].equals("mkdir")) {
                // String path1= path+ os;
                path+=os;
                String name = niz[1];
                //Class local = Class.forName("LocalImplementation");// = new LocalImplementation();
                local.createDirectory(name,path);

                path+=name;
                System.out.println(path);
            }
            else if (niz[0].equals("rm")) {
                String path1=path+ os;
                local.deleteFile(niz[1],path1);
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
                local.listFilesFromDirectory(path);
            }
            if (niz[0].equals("save")) {
                String filename=niz[1];

                System.out.println("u save metodi sam "+path);
                local.saveFile(filename,path);
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
