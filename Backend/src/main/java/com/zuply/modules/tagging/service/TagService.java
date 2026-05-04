package com.zuply.modules.tagging.service;

import com.zuply.modules.tagging.model.Tag;
import com.zuply.modules.tagging.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TagService
 * ----------
 * Saves AI-generated tags to the Tags table.
 * Handles deduplication before saving.
 *
 * ASSIGNED TO: Manjunath
 *
 * TODO:
 *  1. Accept productId and raw tag list from AIGeneratedContent
 *  2. Deduplicate tags (lowercase + distinct)
 *  3. Delete existing tags for the product (if regenerating)
 *  4. Build Tag entities and save to DB
 *  5. Return the saved tag name list
 */
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public List<String> saveTags(Long productId, List<String> rawTags) {

        // Step 1 — Deduplicate and normalize tags
        List<String> uniqueTags = rawTags.stream()
            .map(String::toLowerCase)
            .map(String::trim)
            .distinct()
            .collect(Collectors.toList());

        // Step 2 — Delete existing tags for this product (allows regeneration)
        tagRepository.deleteByProductId(productId);

        // Step 3 — Build and save Tag entities
        List<Tag> tagEntities = uniqueTags.stream()
            .map(tagName -> Tag.builder()
                .productId(productId)
                .tagName(tagName)
                .build())
            .collect(Collectors.toList());
        tagRepository.saveAll(tagEntities);

        // Step 4 — Return saved tag names
        return uniqueTags;
    }
}
