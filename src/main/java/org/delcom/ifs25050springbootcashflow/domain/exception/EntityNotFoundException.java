package org.delcom.ifs25050springbootcashflow.domain.exception;

public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entityName, Object id) {
        super(entityName + " dengan ID " + id + " tidak ditemukan.");
    }
}