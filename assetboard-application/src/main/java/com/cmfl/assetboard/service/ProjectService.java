package com.cmfl.assetboard.service;

import com.cmfl.assetboard.common.data.Project;
import com.cmfl.assetboard.common.query.PageData;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.dao.sql.entity.ProjectEntity;
import com.cmfl.assetboard.dao.sql.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public Project save(Project project) {
        if (project.getId() == null) {
            project.setId(UUID.randomUUID());
            project.setCreatedTime(System.currentTimeMillis());
        }
        return projectRepository.save(ProjectEntity.fromData(project)).toData();
    }

    public Optional<Project> findById(UUID id) {
        return projectRepository.findById(id).map(ProjectEntity::toData);
    }

    public PageData<Project> findByTenantId(UUID tenantId, PageLink pageLink) {
        Page<ProjectEntity> page = projectRepository.findByTenantId(tenantId, PageRequest.of(pageLink.getPage(), pageLink.getPageSize()));
        return new PageData<>(
                page.getContent().stream().map(ProjectEntity::toData).toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext());
    }

    public PageData<Project> findByCustomerId(UUID customerId, PageLink pageLink) {
        Page<ProjectEntity> page = projectRepository.findByCustomerId(customerId, PageRequest.of(pageLink.getPage(), pageLink.getPageSize()));
        return new PageData<>(
                page.getContent().stream().map(ProjectEntity::toData).toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext());
    }

    @Transactional
    public void deleteById(UUID id) {
        projectRepository.deleteById(id);
    }
}
