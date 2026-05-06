package com.cmfl.assetboard.service;

import com.cmfl.assetboard.common.data.Customer;
import com.cmfl.assetboard.common.query.PageData;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.dao.sql.entity.CustomerEntity;
import com.cmfl.assetboard.dao.sql.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer save(Customer customer) {
        if (customer.getId() == null) {
            customer.setId(UUID.randomUUID());
            customer.setCreatedTime(System.currentTimeMillis());
        }
        return customerRepository.save(CustomerEntity.fromData(customer)).toData();
    }

    public Optional<Customer> findById(UUID id) {
        return customerRepository.findById(id).map(CustomerEntity::toData);
    }

    public PageData<Customer> findByTenantId(UUID tenantId, PageLink pageLink) {
        Page<CustomerEntity> page = customerRepository.findByTenantId(tenantId, PageRequest.of(pageLink.getPage(), pageLink.getPageSize()));
        return new PageData<>(
                page.getContent().stream().map(CustomerEntity::toData).toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext());
    }

    @Transactional
    public void deleteById(UUID id) {
        customerRepository.deleteById(id);
    }
}
