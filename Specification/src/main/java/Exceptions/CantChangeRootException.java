package Exceptions;

public class CantChangeRootException extends Exception{

    private String message = "Error: Cannot change rootDirectory";

    public CantChangeRootException() {
    }

    @Override
    public String getMessage() {
        return message;
    }

}
