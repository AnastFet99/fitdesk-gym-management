package gr.aueb.cf10.gymapp.core.exceptions;

import java.util.UUID;

public class EntityNotFoundException extends AppException {

    public EntityNotFoundException(String entityName, Long id) {
        super(String.format("%s with id=%d was not found", entityName, id));
    }

    public EntityNotFoundException(String entityName, UUID uuid) {
        super(String.format("%s with uuid=%s was not found", entityName, uuid));
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
