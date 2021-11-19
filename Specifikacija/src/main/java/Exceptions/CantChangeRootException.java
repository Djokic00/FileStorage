package Exceptions;

public class CantChangeRootException extends Exception{

    private String message = "Error: Cannot change rootDirectory";

    @Override
    public String getMessage() {
        return message;
    }

}
