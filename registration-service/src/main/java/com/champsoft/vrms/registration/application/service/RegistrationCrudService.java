package com.champsoft.vrms.registration.application.service;

import com.champsoft.vrms.registration.application.exception.RegistrationNotFoundException;
import com.champsoft.vrms.registration.application.port.out.RegistrationRepositoryPort;
import com.champsoft.vrms.registration.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RegistrationCrudService {

    private final RegistrationRepositoryPort repo;
    public RegistrationCrudService(RegistrationRepositoryPort repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public Registration get(String id) {
        return repo.findById(RegistrationId.of(id))
                .orElseThrow(() -> new RegistrationNotFoundException("Registration not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Registration> list() {
        return repo.findAll();
    }

    @Transactional
    public Registration renew(String id, LocalDate newExpiry) {
        var reg = get(id);
        reg.renew(new ExpiryDate(newExpiry));
        return repo.save(reg);
    }

    @Transactional
    public Registration cancel(String id) {
        var reg = get(id);
        reg.cancel();
        return repo.save(reg);
    }

    @Transactional
    public void delete(String id) {
        get(id);
        repo.deleteById(RegistrationId.of(id));
    }
}
