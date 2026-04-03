package com.champsoft.vrms.cars.api;

import com.champsoft.vrms.cars.api.dto.CreateVehicleRequest;
import com.champsoft.vrms.cars.api.dto.UpdateVehicleRequest;
import com.champsoft.vrms.cars.api.mapper.VehicleApiMapper;
import com.champsoft.vrms.cars.application.service.VehicleCrudService;
import com.champsoft.vrms.cars.application.service.VehicleEligibilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class VehicleController {

    private final VehicleCrudService service;
    private final VehicleEligibilityService eligibilityService;


    public VehicleController(VehicleCrudService service,
                             VehicleEligibilityService eligibilityService) {
        this.service = service;
        this.eligibilityService = eligibilityService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CreateVehicleRequest req) {
        var v = service.create(req.vin(), req.make(), req.model(), req.year());
        return ResponseEntity.ok(VehicleApiMapper.toResponse(v));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        return ResponseEntity.ok(VehicleApiMapper.toResponse(service.getById(id)));
    }

    @GetMapping
    public ResponseEntity<?> list() {
        List<?> data = service.list().stream().map(VehicleApiMapper::toResponse).toList();
        return ResponseEntity.ok(data);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody @Valid UpdateVehicleRequest req) {
        var v = service.updateSpecs(id, req.make(), req.model(), req.year());
        return ResponseEntity.ok(VehicleApiMapper.toResponse(v));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable String id) {
        var v = service.activate(id);
        return ResponseEntity.ok(VehicleApiMapper.toResponse(v));
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
