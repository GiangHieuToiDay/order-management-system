package com.trainning.ordersystem.repository.impl;

import com.trainning.ordersystem.dto.response.role.RoleResponse;
import com.trainning.ordersystem.repository.RoleRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryCustomImpl implements RoleRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<RoleResponse> findAllRoleResponses() {
        String sql = """
            SELECT r.id AS id,
                   r.name AS name,
                   r.description AS description
            FROM roles r
            ORDER BY r.id ASC
        """;

        Query query = entityManager.createNativeQuery(sql, "RoleResponseMapping");

        @SuppressWarnings("unchecked")
        List<RoleResponse> results = query.getResultList();
        return results;
    }

    @Override
    public Optional<RoleResponse> findRoleResponseById(Long id) {
        String sql = """
            SELECT r.id AS id,
                   r.name AS name,
                   r.description AS description
            FROM roles r
            WHERE r.id = :id
        """;

        Query query = entityManager.createNativeQuery(sql, "RoleResponseMapping");
        query.setParameter("id", id);

        @SuppressWarnings("unchecked")
        List<RoleResponse> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<RoleResponse> findRoleResponseByName(String name) {
        String sql = """
            SELECT r.id AS id,
                   r.name AS name,
                   r.description AS description
            FROM roles r
            WHERE r.name = :name
        """;

        Query query = entityManager.createNativeQuery(sql, "RoleResponseMapping");
        query.setParameter("name", name);

        @SuppressWarnings("unchecked")
        List<RoleResponse> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
