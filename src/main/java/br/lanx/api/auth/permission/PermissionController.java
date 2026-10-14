package br.lanx.api.auth.permission;

import static br.lanx.api.auth.permission.AuthPermissions.PERFIS_READ;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.auth.permission.dto.FeatureNode;
import br.lanx.api.shared.web.ApiResponse;

/** Alimenta o painel: o que existe para ser concedido às roles. */
@RestController
@RequestMapping("/auth/features")
public class PermissionController {

    private final PermissionService service;

    PermissionController(PermissionService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PERFIS_READ + "')")
    ApiResponse<List<FeatureNode>> catalog() {
        return ApiResponse.ok("Catálogo de funcionalidades", service.featureTree());
    }
}
