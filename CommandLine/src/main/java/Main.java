import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    static LocalImplementation local = new LocalImplementation();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String path = "";
        String os = "";
        Scanner in = new Scanner(System.in);
        System.out.println("Unesite putanju do skladista: ");
        String s = in.nextLine();
        if (s.contains("/")) os="/";
        else os = "\"";
        Path putanja = Paths.get(s);
        System.out.println(Files.exists(putanja));

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
            }

            if (niz[0].equals("cd")) {
                path += os;
                path += niz[1];
                System.out.println(path);
            }
            else if (niz[0].equals("touch")) {
                path += os;
                //Class local = Class.forName("LocalImplementation");// = new LocalImplementation();
                local.createFile(niz[1],path);
            }
            else if (niz[0].equals("rm")) {
                local.deleteFile(niz[1],path);
            }

            if (niz[0].equals("..")) {
                String niz1[] = s.split(os);
                path = "";
                for (int i=0 ; i < niz1.length - 1; i++)
                {
                    path += niz1[i];
                    if (i != niz1.length - 2) path += os;
                }
                System.out.println(path);
            }
        }

        //noviFajl.mkdir();
    }
}
