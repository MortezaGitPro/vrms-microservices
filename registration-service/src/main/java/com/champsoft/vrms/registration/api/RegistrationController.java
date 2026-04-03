package com.champsoft.vrms.registration.api;

import com.champsoft.vrms.registration.api.dto.*;
import com.champsoft.vrms.registration.api.mapper.RegistrationApiMapper;
import com.champsoft.vrms.registration.application.service.*;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationOrchestrator orchestrator;
    private final RegistrationCrudService crud;
    private final RegistrationRepresentationAssembler assembler;

    public RegistrationController(
            RegistrationOrchestrator orchestrator,
            RegistrationCrudService crud,
            RegistrationRepresentationAssembler assembler
    ) {
        this.orchestrator = orchestrator;
        this.crud = crud;
        this.assembler = assembler;
    }

    @PostMapping
    public ResponseEntity<EntityModel<RegistrationResponse>> register(@RequestBody @Valid RegisterVehicleRequest req) {
        var reg = orchestrator.register(req.vehicleId(), req.ownerId(), req.agentId(), req.plate(), req.expiry());
        var response = RegistrationApiMapper.toResponse(reg);
        return ResponseEntity.ok(assembler.toModel(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<RegistrationResponse>> get(@PathVariable String id) {
        var response = RegistrationApiMapper.toResponse(crud.get(id));
        return ResponseEntity.ok(assembler.toModel(response));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<RegistrationResponse>>> list() {
        List<RegistrationResponse> responses = crud.list().stream()
                .map(RegistrationApiMapper::toResponse)
                .toList();

        return ResponseEntity.ok(assembler.toCollectionModel(responses));
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<EntityModel<RegistrationResponse>> renew(
            @PathVariable String id,
            @RequestBody @Valid RenewRegistrationRequest req
    ) {
        var reg = crud.renew(id, req.newExpiry());
        var response = RegistrationApiMapper.toResponse(reg);
        return ResponseEntity.ok(assembler.toModel(response));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<EntityModel<RegistrationResponse>> cancel(@PathVariable String id) {
        var reg = crud.cancel(id);
        var response = RegistrationApiMapper.toResponse(reg);
        return ResponseEntity.ok(assembler.toModel(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        crud.delete(id);
        return ResponseEntity.noContent().build();
    }
}