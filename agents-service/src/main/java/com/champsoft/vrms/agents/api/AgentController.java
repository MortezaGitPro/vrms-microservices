package com.champsoft.vrms.agents.api;

import com.champsoft.vrms.agents.api.dto.*;
import com.champsoft.vrms.agents.api.mapper.AgentApiMapper;
import com.champsoft.vrms.agents.application.service.AgentCrudService;
import com.champsoft.vrms.agents.application.service.AgentEligibilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentCrudService service;
    private final AgentEligibilityService eligibilityService;

    public AgentController(AgentCrudService service,
                           AgentEligibilityService eligibilityService) {
        this.service = service;
        this.eligibilityService = eligibilityService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CreateAgentRequest req) {
        var a = service.create(req.name(), req.role());
        return ResponseEntity.ok(AgentApiMapper.toResponse(a));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        return ResponseEntity.ok(AgentApiMapper.toResponse(service.getById(id)));
    }

    @GetMapping
    public ResponseEntity<?> list() { return ResponseEntity.ok(service.list().stream().map(AgentApiMapper::toResponse).toList()); }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody @Valid UpdateAgentRequest req) {
        var a = service.update(id, req.name(), req.role());
        return ResponseEntity.ok(AgentApiMapper.toResponse(a));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable String id) {
        var a = service.activate(id);
        return ResponseEntity.ok(AgentApiMapper.toResponse(a));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/eligibility")
    public ResponseEntity<Boolean> isEligible(@PathVariable String id) {
        return ResponseEntity.ok(eligibilityService.isEligible(id));
    }
}
