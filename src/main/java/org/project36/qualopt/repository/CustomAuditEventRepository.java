package org.project36.qualopt.repository;

import org.project36.qualopt.config.Constants;
import org.project36.qualopt.config.audit.AuditEventConverter;
import org.project36.qualopt.domain.PersistentAuditEvent;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Instant;

/**
 * An implementation of Spring Boot's AuditEventRepository.
 */
@Repository
public class CustomAuditEventRepository implements AuditEventRepository {

    private static final String AUTHORIZATION_FAILURE = "AUTHORIZATION_FAILURE";

    private final PersistenceAuditEventRepository persistenceAuditEventRepository;

    private final AuditEventConverter auditEventConverter;

    public CustomAuditEventRepository(PersistenceAuditEventRepository persistenceAuditEventRepository,
            AuditEventConverter auditEventConverter) {

        this.persistenceAuditEventRepository = persistenceAuditEventRepository;
        this.auditEventConverter = auditEventConverter;
    }

    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {
        Iterable<PersistentAuditEvent> persistentAuditEvents;
        if(principal == null && after == null && type == null) {
            persistentAuditEvents = persistenceAuditEventRepository.findAll();
        }
        else if (after == null && type == null) {
            persistentAuditEvents = persistenceAuditEventRepository.findByPrincipal(principal);
        }
        else if (principal == null && type == null) {
            persistentAuditEvents = persistenceAuditEventRepository.findByAuditEventDateAfter(after);
        }
        else if (type == null) {
            persistentAuditEvents = persistenceAuditEventRepository.findByPrincipalAndAuditEventDateAfter(principal, after);
        }
        else {
            persistentAuditEvents = persistenceAuditEventRepository.findByPrincipalAndAuditEventDateAfterAndAuditEventType(principal, after, type);
        }
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void add(AuditEvent event) {
        if (!AUTHORIZATION_FAILURE.equals(event.getType()) &&
            !Constants.ANONYMOUS_USER.equals(event.getPrincipal())) {

            PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent();
            persistentAuditEvent.setPrincipal(event.getPrincipal());
            persistentAuditEvent.setAuditEventType(event.getType());
            persistentAuditEvent.setAuditEventDate(event.getTimestamp());
            persistentAuditEvent.setData(auditEventConverter.convertDataToStrings(event.getData()));
            persistenceAuditEventRepository.save(persistentAuditEvent);
        }
    }
}
