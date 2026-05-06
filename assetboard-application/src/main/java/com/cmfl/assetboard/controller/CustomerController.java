package com.cmfl.assetboard.controller;

import com.cmfl.assetboard.common.data.Customer;
import com.cmfl.assetboard.common.query.PageData;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public Customer save(@RequestBody Customer customer) {
        return customerService.save(customer);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getById(@PathVariable UUID customerId) {
        return customerService.findById(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public PageData<Customer> getByTenantId(
            @RequestParam UUID tenantId,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "0") int page) {
        return customerService.findByTenantId(tenantId, new PageLink(pageSize, page));
    }

    @DeleteMapping("/{customerId}")
    public void delete(@PathVariable UUID customerId) {
        customerService.deleteById(customerId);
    }
}
