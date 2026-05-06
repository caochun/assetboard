package com.cmfl.assetboard.controller;

import com.cmfl.assetboard.common.data.Asset;
import com.cmfl.assetboard.common.query.PageData;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.service.AssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/asset")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping
    public Asset save(@RequestBody Asset asset) {
        return assetService.save(asset);
    }

    @GetMapping("/{assetId}")
    public ResponseEntity<Asset> getById(@PathVariable UUID assetId) {
        return assetService.findById(assetId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public PageData<Asset> list(
            @RequestParam UUID tenantId,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) UUID customerId) {
        PageLink pageLink = new PageLink(pageSize, page);
        if (customerId != null) {
            return assetService.findByCustomerId(tenantId, customerId, pageLink);
        }
        if (type != null && !type.isBlank()) {
            return assetService.findByTenantIdAndType(tenantId, type, pageLink);
        }
        return assetService.findByTenantId(tenantId, pageLink);
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<Void> delete(@PathVariable UUID assetId) {
        assetService.deleteById(assetId);
        return ResponseEntity.ok().build();
    }
}
