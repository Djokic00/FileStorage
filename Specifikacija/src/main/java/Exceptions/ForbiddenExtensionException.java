package Exceptions;

public class ForbiddenExtensionException extends Exception{

    String message = "Error: Forbidden file extension";

    public ForbiddenExtensionException() {
    }

    @Override
    public String getMessage() {
        return message;
    }

}
