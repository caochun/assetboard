package com.cmfl.assetboard.controller;

import com.cmfl.assetboard.common.data.EntityType;
import com.cmfl.assetboard.common.data.relation.EntityRelation;
import com.cmfl.assetboard.service.RelationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/relation")
public class RelationController {

    private final RelationService relationService;

    public RelationController(RelationService relationService) {
        this.relationService = relationService;
    }

    @PostMapping
    public EntityRelation save(@RequestBody EntityRelation relation) {
        return relationService.save(relation);
    }

    @GetMapping("/from")
    public List<EntityRelation> getByFrom(
            @RequestParam UUID fromId,
            @RequestParam EntityType fromType) {
        return relationService.findByFrom(fromId, fromType);
    }

    @GetMapping("/to")
    public List<EntityRelation> getByTo(
            @RequestParam UUID toId,
            @RequestParam EntityType toType) {
        return relationService.findByTo(toId, toType);
    }

    @DeleteMapping
    public void delete(@RequestBody EntityRelation relation) {
        relationService.delete(relation);
    }
}
