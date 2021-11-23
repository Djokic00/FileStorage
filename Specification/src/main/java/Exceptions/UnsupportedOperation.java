package Exceptions;

public class UnsupportedOperation extends Exception{
    String message = "Error: Unsupported operation";

    @Override
    public String getMessage() {
        return message;
    }
}
