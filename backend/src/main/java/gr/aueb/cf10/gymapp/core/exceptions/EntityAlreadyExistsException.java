package gr.aueb.cf10.gymapp.core.exceptions;

public class EntityAlreadyExistsException extends AppException {

    public EntityAlreadyExistsException(String entityName, String field, String value) {
        super(String.format("%s with %s='%s' already exists", entityName, field, value));
    }

    public EntityAlreadyExistsException(String message) {
        super(message);
    }
}
