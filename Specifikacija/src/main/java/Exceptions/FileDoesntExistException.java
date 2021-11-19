package Exceptions;

public class FileDoesntExistException extends Exception{

    String message = "Error: File with such name does not exist";

    public FileDoesntExistException() {
    }

    @Override
    public String getMessage() {
        return message;
    }

}
