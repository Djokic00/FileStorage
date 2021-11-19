package Exceptions;

public class FileNotCreatedException extends Exception{

    String message = "Error: File not created";

    public FileNotCreatedException() {
    }

    @Override
    public String getMessage() {
        return message;
    }

}
