package com.cmfl.assetboard.controller;

import com.cmfl.assetboard.common.data.Project;
import com.cmfl.assetboard.common.query.PageData;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public Project save(@RequestBody Project project) {
        return projectService.save(project);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getById(@PathVariable UUID projectId) {
        return projectService.findById(projectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public PageData<Project> list(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "0") int page) {
        if (customerId != null) {
            return projectService.findByCustomerId(customerId, new PageLink(pageSize, page));
        }
        return projectService.findByTenantId(tenantId, new PageLink(pageSize, page));
    }

    @DeleteMapping("/{projectId}")
    public void delete(@PathVariable UUID projectId) {
        projectService.deleteById(projectId);
    }
}
