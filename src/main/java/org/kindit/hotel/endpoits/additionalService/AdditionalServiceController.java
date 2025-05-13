package org.kindit.hotel.endpoits.additionalService;

import lombok.RequiredArgsConstructor;
import org.kindit.hotel.data.additionalService.AdditionalService;
import org.kindit.hotel.endpoits.ApiController;
import org.kindit.hotel.endpoits.additionalService.request.AdditionalServiceRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/additionalService")
@RequiredArgsConstructor
public class AdditionalServiceController extends ApiController<AdditionalServiceService> {

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<List<AdditionalService>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdditionalService> getService(@PathVariable Integer id) {
        return service.get(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND).build()
                );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<AdditionalService> createService(@RequestBody AdditionalServiceRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<AdditionalService> refreshService(@PathVariable Integer id, @RequestBody AdditionalServiceRequest request) {
        return service.refresh(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<AdditionalService> updateService(@PathVariable Integer id, @RequestBody AdditionalServiceRequest request) {
        return service.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<Void> deleteService(@PathVariable Integer id) {
        boolean deleted = service.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
