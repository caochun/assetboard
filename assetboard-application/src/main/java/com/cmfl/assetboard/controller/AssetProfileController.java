package com.cmfl.assetboard.controller;

import com.cmfl.assetboard.common.data.AssetProfile;
import com.cmfl.assetboard.common.query.PageData;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.service.AssetProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/assetProfile")
public class AssetProfileController {

    private final AssetProfileService service;

    public AssetProfileController(AssetProfileService service) {
        this.service = service;
    }

    @PostMapping
    public AssetProfile save(@RequestBody AssetProfile profile) {
        return service.save(profile);
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<AssetProfile> getById(@PathVariable UUID profileId) {
        return service.findById(profileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public PageData<AssetProfile> getByTenantId(
            @RequestParam UUID tenantId,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "0") int page) {
        return service.findByTenantId(tenantId, new PageLink(pageSize, page));
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> delete(@PathVariable UUID profileId) {
        service.deleteById(profileId);
        return ResponseEntity.ok().build();
    }
}
