package com.cmfl.assetboard.controller;

import com.cmfl.assetboard.common.data.DataSourceConfig;
import com.cmfl.assetboard.service.DataSourceConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dataSourceConfig")
public class DataSourceConfigController {

    private final DataSourceConfigService service;

    public DataSourceConfigController(DataSourceConfigService service) {
        this.service = service;
    }

    @GetMapping
    public List<DataSourceConfig> getByAssetId(@RequestParam UUID assetId) {
        return service.findByAssetId(assetId);
    }

    @PostMapping
    public DataSourceConfig save(@RequestBody DataSourceConfig config) {
        return service.save(config);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.deleteById(id);
    }
}
