package com.zuply.modules.tagging.repository;

import com.zuply.modules.tagging.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TagRepository
 * -------------
 * JPA repository for Tag entity.
 *
 * ASSIGNED TO: Manjunath
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    // Fetch all tags for a specific product
    List<Tag> findByProductId(Long productId);

    // Delete all tags for a product (used when regenerating tags)
    void deleteByProductId(Long productId);
}
