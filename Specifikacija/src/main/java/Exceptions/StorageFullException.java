package Exceptions;

public class StorageFullException extends Exception{
    String message="Storage is full";

    public StorageFullException() {
    }

    @Override
    public String getMessage() {
        return message;
    }
}
