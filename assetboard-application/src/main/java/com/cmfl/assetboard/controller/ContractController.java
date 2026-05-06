package com.cmfl.assetboard.controller;

import com.cmfl.assetboard.common.data.Contract;
import com.cmfl.assetboard.common.query.PageData;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.service.ContractService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/contract")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping
    public Contract save(@RequestBody Contract contract) {
        return contractService.save(contract);
    }

    @GetMapping("/{contractId}")
    public ResponseEntity<Contract> getById(@PathVariable UUID contractId) {
        return contractService.findById(contractId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public PageData<Contract> list(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "0") int page) {
        if (projectId != null) {
            return contractService.findByProjectId(projectId, new PageLink(pageSize, page));
        }
        return contractService.findAll(new PageLink(pageSize, page));
    }

    @DeleteMapping("/{contractId}")
    public void delete(@PathVariable UUID contractId) {
        contractService.deleteById(contractId);
    }
}
