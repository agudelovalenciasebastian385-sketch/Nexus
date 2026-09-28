package nexusmarket.domain.exceptions;

/** A referenced entity does not exist. */
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entity, String id) {
        super(entity + " with id " + id + " not found.");
    }
}
