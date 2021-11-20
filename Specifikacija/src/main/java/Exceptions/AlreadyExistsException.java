package Exceptions;

public class AlreadyExistsException extends Exception{
    String message = "Error: File/Folder with such name already exists";

    @Override
    public String getMessage() {
        return message;
    }
}
