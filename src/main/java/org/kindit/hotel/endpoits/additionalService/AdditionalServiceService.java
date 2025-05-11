package org.kindit.hotel.endpoits.additionalService;

import lombok.RequiredArgsConstructor;
import org.kindit.hotel.data.additionalService.AdditionalService;
import org.kindit.hotel.endpoits.ServiceController;
import org.kindit.hotel.endpoits.additionalService.request.AdditionalServiceRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdditionalServiceService extends ServiceController {

    public List<AdditionalService> getAll() {
        return repository.getAdditionalServiceRepository().findAll();
    }

    public Optional<AdditionalService> get(Integer id) {
        return repository.getAdditionalServiceRepository().findById(id);
    }

    public AdditionalService create(AdditionalServiceRequest request) {
        return repository.getAdditionalServiceRepository().save(
                AdditionalService.builder()
                        .name(request.getName())
                        .description(request.getDescription())
                        .price(request.getPrice())
                        .build()
        );
    }

    public Optional<AdditionalService> refresh(Integer id, AdditionalServiceRequest request) {
        return repository.getAdditionalServiceRepository().findById(id).map(existingService -> {
            AdditionalService updatedService = AdditionalService.builder()
                    .id(existingService.getId())
                    .name(request.getName())
                    .description(request.getDescription())
                    .price(request.getPrice())
                    .build();

            return repository.getAdditionalServiceRepository().save(updatedService);
        });
    }

    public Optional<AdditionalService> update(Integer id, AdditionalServiceRequest request) {
        return repository.getAdditionalServiceRepository().findById(id).map(additionalService -> {

            if (request.getName() != null) additionalService.setName(request.getName());
            if (request.getDescription() != null) additionalService.setDescription(request.getDescription());
            if (request.getPrice() != null) additionalService.setPrice(request.getPrice());

            return repository.getAdditionalServiceRepository().save(additionalService);
        });
    }

    public boolean delete(Integer id) {
        return repository.getAdditionalServiceRepository().findById(id).map(existing -> {
            repository.getAdditionalServiceRepository().delete(existing);
            return true;
        }).orElse(false);
    }
}
