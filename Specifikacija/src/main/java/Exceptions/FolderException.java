package Exceptions;

public class FolderException extends Exception{

    private String message = "Error: Folder is full";

    public FolderException() {
    }

    @Override
    public String getMessage() {
        return message;
    }

}
