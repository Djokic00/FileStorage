import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        String path = "/home/aleksa";
        System.out.println("Trenutni dir: " + path);
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();
        String niz[] = s.split(" ");
        //Main objekat = new Main();
        if (niz[0].equals("cd")) {
            path += niz[1];
        }
        else if (niz[0].equals("touch")) {
            LocalImplementation local = new LocalImplementation();
            local.createFile(niz[1],path);
        }
        else if (niz[0].equals("rm")) {
            LocalImplementation local = new LocalImplementation();
            local.deleteFile(niz[1],path);
        }

        //noviFajl.mkdir();
    }

}
