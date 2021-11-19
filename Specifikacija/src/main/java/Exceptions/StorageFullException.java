package Exceptions;

public class StorageFullException extends Exception{

    String message = "Error: Storage is full";

    public StorageFullException() {
    }

    @Override
    public String getMessage() {
        return message;
    }
}
