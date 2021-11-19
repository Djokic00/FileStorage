package Exceptions;

public class FileNotCreatedException extends Exception{

    private String message = "Error: File not created";

    @Override
    public String getMessage() {
        return message;
    }

}
