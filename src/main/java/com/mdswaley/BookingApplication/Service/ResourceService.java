package com.mdswaley.BookingApplication.Service;

import com.mdswaley.BookingApplication.DTO.ResourceRequest;
import com.mdswaley.BookingApplication.DTO.ResourceResponse;
import com.mdswaley.BookingApplication.Entity.ResourceEntity;
import com.mdswaley.BookingApplication.Exception.ResourceNotFoundException;
import com.mdswaley.BookingApplication.Repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ResourceService {

    private final ResourceRepository resourceRepository;

    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResources() {

        return resourceRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {

        ResourceEntity resource = resourceRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));

        return toResponse(resource);
    }

    public ResourceResponse createResource(ResourceRequest request) {

        ResourceEntity resource = ResourceEntity.builder()
                .name(request.name())
                .description(request.description())
                .type(request.type())
                .available(request.available())
                .price(request.price())
                .build();

        ResourceEntity saved = resourceRepository.save(resource);

        return toResponse(saved);
    }

    public ResourceResponse updateResource(Long id, ResourceRequest request) {

        ResourceEntity resource = resourceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Resource not found with id: " + id)
                        );

        resource.setName(request.name());
        resource.setDescription(request.description());
        resource.setType(request.type());
        resource.setAvailable(request.available());
        resource.setPrice(request.price());

        return toResponse(resource);
    }

    public void deleteResource(Long id) {

        ResourceEntity resource = resourceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Resource not found with id: " + id)
                        );

        resourceRepository.delete(resource);
    }

    private ResourceResponse toResponse(ResourceEntity resource) {

        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getType(),
                resource.isAvailable(),
                resource.getPrice(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}
