package com.zuply.modules.upload.repository;

import com.zuply.modules.upload.dto.ImageStatus;
import com.zuply.modules.upload.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByUserId(Long userId);

    List<Image> findByStatus(ImageStatus status);

    Optional<Image> findByIdAndUserId(Long id, Long userId);
}
