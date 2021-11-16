package Exceptions;

public class FileDoesntExistException extends Exception{
    public FileDoesntExistException(String message) {
        super(message);
    }
}
