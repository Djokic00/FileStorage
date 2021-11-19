package Exceptions;

public class NotStorageException extends Exception{

    private String message = "Error: Not a storage.";

    @Override
    public String getMessage() {
        return message;
    }
}
