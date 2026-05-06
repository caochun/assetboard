package com.cmfl.assetboard.service;

import com.cmfl.assetboard.common.data.Contract;
import com.cmfl.assetboard.common.query.PageData;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.dao.sql.entity.ContractEntity;
import com.cmfl.assetboard.dao.sql.repository.ContractRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ContractService {

    private final ContractRepository contractRepository;

    public ContractService(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Transactional
    public Contract save(Contract contract) {
        if (contract.getId() == null) {
            contract.setId(UUID.randomUUID());
            contract.setCreatedTime(System.currentTimeMillis());
        }
        return contractRepository.save(ContractEntity.fromData(contract)).toData();
    }

    public Optional<Contract> findById(UUID id) {
        return contractRepository.findById(id).map(ContractEntity::toData);
    }

    public PageData<Contract> findByProjectId(UUID projectId, PageLink pageLink) {
        Page<ContractEntity> page = contractRepository.findByProjectId(projectId, PageRequest.of(pageLink.getPage(), pageLink.getPageSize()));
        return new PageData<>(
                page.getContent().stream().map(ContractEntity::toData).toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext());
    }

    public PageData<Contract> findAll(PageLink pageLink) {
        Page<ContractEntity> page = contractRepository.findAll(PageRequest.of(pageLink.getPage(), pageLink.getPageSize()));
        return new PageData<>(
                page.getContent().stream().map(ContractEntity::toData).toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext());
    }

    @Transactional
    public void deleteById(UUID id) {
        contractRepository.deleteById(id);
    }
}
