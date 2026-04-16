package com.zuply.modules.tagging.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Tag Entity
 * ----------
 * Represents a single keyword tag associated with a product.
 *
 * ASSIGNED TO: Manjunath
 */
@Entity
@Table(name = "tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to the product this tag belongs to
    @Column(name = "product_id", nullable = false)
    private Long productId;

    // The keyword tag value
    @Column(name = "tag_name", nullable = false)
    private String tagName;
}
