package com.example.userservice.repository;

import com.example.userservice.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    Optional<Producto> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCodigo(String codigo);
}
