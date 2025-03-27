package com.example.userservice.service;

import com.example.userservice.messaging.MessagePublisher;
import com.example.userservice.model.EmailNotification;
import com.example.userservice.model.Producto;
import com.example.userservice.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;
    private final MessagePublisher messagePublisher;

    public ProductoService(ProductoRepository productoRepository, MessagePublisher messagePublisher) {
        this.productoRepository = productoRepository;
        this.messagePublisher = messagePublisher;
    }

    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> getProductoById(UUID id) {
        return productoRepository.findById(id);
    }

    @Transactional
    public Producto createProducto(Producto producto) {
        // Check if producto code already exists
/*
        if (productoRepository.existsByEmail(producto.getEmail())) {
            throw new IllegalArgumentException("Email de producto ya en uso: " + producto.getEmail());
        }
*/

        if (productoRepository.existsByCodigo(producto.getCodigo())) {
            throw new IllegalArgumentException("Código de producto ya en uso: " + producto.getCodigo());
        }
        // Save producto to database
        producto.setId(UUID.randomUUID());

        // Fallback por si @EnableJpaAuditing no funciona
        LocalDateTime ahora = LocalDateTime.now();
        if (producto.getFechaCreacion() == null) {
            producto.setFechaCreacion(ahora);
        }
        if (producto.getFechaActualizacion() == null) {
            producto.setFechaActualizacion(ahora);
        }
        Producto savedProducto = productoRepository.save(producto);
        logger.info("Producto creado exitosamente: {}", savedProducto.getId());

        try {
            // Create notification
            EmailNotification notification = EmailNotification.forNewProductoCreated(savedProducto);

            // Send notification to RabbitMQ
            messagePublisher.publishEmailNotification(notification);
        } catch (Exception e) {
            // Log the error but don't fail the producto creation
            logger.error("Error al procesar la notificación: {}", e.getMessage());
        }

        return savedProducto;
    }

    @Transactional
    public Optional<Producto> updateProducto(UUID id, Producto productoDetails) {
        return productoRepository.findById(id)
                .map(existingProducto -> {
                    existingProducto.setNombre(productoDetails.getNombre());
                    existingProducto.setPrecio(productoDetails.getPrecio());
                    existingProducto.setStock(productoDetails.getStock());
                    return productoRepository.save(existingProducto);
                });
    }

    @Transactional
    public boolean deleteProducto(UUID id) {
        return productoRepository.findById(id)
                .map(producto -> {
                    productoRepository.delete(producto);
                    logger.info("Producto eliminado: {}", id);
                    return true;
                })
                .orElse(false);
    }
}
