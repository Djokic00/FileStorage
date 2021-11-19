package Exceptions;

public class FolderFullException extends Exception{

    String message = "Error: Folder is full";

    public FolderFullException() {
    }

    @Override
    public String getMessage() {
        return message;
    }

}
