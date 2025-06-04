package org.kindit.hotel.endpoits.room;

import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kindit.hotel.data.additionalService.AdditionalService;
import org.kindit.hotel.data.room.Room;
import org.kindit.hotel.endpoits.ServiceController;
import org.kindit.hotel.endpoits.room.request.RoomRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Getter
@Service
@RequiredArgsConstructor
public class RoomService extends ServiceController {

    private final String uploadDir = "uploads/rooms/";

    public List<Room> getAllRooms() {
        return repository.getRoomRepository().findAll();
    }

    public Optional<Room> getRoomById(Integer id) {
        return repository.getRoomRepository().findById(id);
    }

    public List<Room> getAllAvailableRoom() {
        return repository.getRoomRepository().findByIsAvailableTrue();
    }

    public Room createRoom(RoomRequest request) {
        String imageName = "";
        List<AdditionalService> additionalServices = repository.getAdditionalServiceRepository()
                .findAllById(request.getAdditionalServiceIds());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imageName = saveImage(Path.of(uploadDir), request.getImage());
        }

        Room room = Room.builder()
                .number(request.getNumber())
                .pricePerNight(request.getPricePerNight())
                .capacity(request.getCapacity())
                .description(request.getDescription())
                .imagePath(imageName)
                .isAvailable(true)
                .additionalServices(additionalServices)
                .build();

        return repository.getRoomRepository().save(room);
    }

    public Optional<Room> refreshRoom(Integer id, RoomRequest request) {
        return repository.getRoomRepository().findById(id).map(existing -> {
            List<AdditionalService> additionalServices = repository.getAdditionalServiceRepository()
                    .findAllById(request.getAdditionalServiceIds());

            existing.setNumber(request.getNumber());
            existing.setPricePerNight(request.getPricePerNight());
            existing.setDescription(request.getDescription());
            existing.setCapacity(request.getCapacity());
            existing.setAdditionalServices(additionalServices);

            if (request.getImage() != null && !request.getImage().isEmpty()) {
                existing.setImagePath(saveImage(Path.of(uploadDir), request.getImage()));
            }

            return repository.getRoomRepository().save(existing);
        });
    }

    public Optional<Room> updateRoom(Integer id, RoomRequest request) {
        return repository.getRoomRepository().findById(id).map(existing -> {
            if (request.getNumber() != null)
                existing.setNumber(request.getNumber());

            if (request.getPricePerNight() != null)
                existing.setPricePerNight(request.getPricePerNight());

            if (request.getDescription() != null)
                existing.setDescription(request.getDescription());

            if (request.getImage() != null && !request.getImage().isEmpty()) {
                existing.setImagePath(saveImage(Path.of(uploadDir), request.getImage()));
            }

            if (request.getCapacity() != null) {
                existing.setCapacity(request.getCapacity());
            }

            if (request.getAdditionalServiceIds() != null) {
                List<AdditionalService> additionalServices = repository.getAdditionalServiceRepository()
                        .findAllById(request.getAdditionalServiceIds());

                existing.setAdditionalServices(additionalServices);
            }

            return repository.getRoomRepository().save(existing);
        });
    }

    public boolean deleteRoom(Integer id) {
        Room room = repository.getRoomRepository()
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        if (room.getImagePath() != null) {
            Path imagePath = Path.of(room.getImagePath()).toAbsolutePath();
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                throw new RuntimeException("Не удалось удалить изображение: " + imagePath, e);
            }
        }

        repository.getRoomRepository().deleteById(id);

        return repository.getRoomRepository().findById(id).isEmpty();
    }

}
