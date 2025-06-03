package org.kindit.hotel.endpoits;

import org.kindit.hotel.Repository;
import org.kindit.hotel.data.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public abstract class ServiceController {
    @Autowired
    protected Repository repository;

    protected final User getAuthentifactedUser() {
        return  (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected final String saveImage(Path destinationFolder, MultipartFile image) {
        String imageName = UUID.randomUUID() + "_" + image.getOriginalFilename();

        Path imagePath = Path.of(destinationFolder + File.separator + imageName);

        try {
            Files.createDirectories(destinationFolder);
            Files.copy(image.getInputStream(), imagePath.toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return imageName;
    }
}
