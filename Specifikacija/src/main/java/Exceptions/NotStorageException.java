package Exceptions;

public class NotStorageException extends Exception{

    String message = "Error: Not a storage.";

    public NotStorageException() {
    }

    @Override
    public String getMessage() {
        return message;
    }
}
