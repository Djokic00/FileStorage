package Exceptions;

public class StorageException extends Exception{

    private String message = "Error: Storage is full";

    @Override
    public String getMessage() {
        return message;
    }
}
