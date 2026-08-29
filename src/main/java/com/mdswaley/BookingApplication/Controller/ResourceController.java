package com.mdswaley.BookingApplication.Controller;

import com.mdswaley.BookingApplication.DTO.ResourceRequest;
import com.mdswaley.BookingApplication.DTO.ResourceResponse;
import com.mdswaley.BookingApplication.Service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<List<ResourceResponse>> getAllResources() {

        return ResponseEntity.ok(
                resourceService.getAllResources()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResourceById(@PathVariable Long id) {

        return ResponseEntity.ok(
                resourceService.getResourceById(id)
        );
    }

    @PostMapping
    public ResponseEntity<ResourceResponse> createResource(@Valid @RequestBody ResourceRequest request) {

        ResourceResponse response =
                resourceService.createResource(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponse> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequest request
    ) {

        return ResponseEntity.ok(
                resourceService.updateResource(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long id
    ) {

        resourceService.deleteResource(id);

        return ResponseEntity.noContent().build();
    }
}
