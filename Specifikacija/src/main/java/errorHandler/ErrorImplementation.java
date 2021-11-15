package errorHandler;

public class ErrorImplementation implements ErrorHandler {

    @Override
    public void generateError(ErrorType errorType) {
        switch (errorType) {
            case NOT_A_STORAGE -> System.out.println("Error: Not a storage");
            case FOLDER_IS_FULL -> System.out.println("Error: Folder is full");
            case NOT_CONNECTED -> System.out.println("Error: Connection failed");
            case UNAUTHORIZED_ACTION -> System.out.println("Error: Not enough privilege for that action");
            case STORAGE_IS_FULL -> System.out.println("Error: Storage is full");
            case FILE_DOES_NOT_EXISTS -> System.out.println("Error: File with such name does not exist");
            case CANNOT_CHANGE_ROOT -> System.out.println("Error: Cannot change rootDirectory");
            case FILE_NOT_CREATED -> System.out.println("Error: File not created");
        }

    }
}
