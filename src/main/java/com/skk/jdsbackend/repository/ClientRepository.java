package com.skk.jdsbackend.repository;

import com.skk.jdsbackend.entity.Client;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    @EntityGraph(attributePaths = "cases")
    List<Client> findAll();

    Optional<Client> findByEmail(String email);

    @EntityGraph(attributePaths = "cases")
    List<Client> findByFirstnameContainingIgnoreCaseOrLastnameContainingIgnoreCase(String firstname, String lastname);

    boolean existsByEmail(String email);
}
