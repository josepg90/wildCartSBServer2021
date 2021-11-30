package net.ausiasmarch.wildcart.repository;

import net.ausiasmarch.wildcart.entity.CompraEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompraRepository extends JpaRepository<CompraEntity, Long> {
    
    Page<CompraEntity> findByIdIgnoreCaseContainingOrCantidadIgnoreCaseContainingOrPrecioIgnoreCaseContainingOrFechaIgnoreCaseContaining(String id, String cantidad, String precio, String fecha, Pageable oPageable);

    Page<CompraEntity> findByFacturaIdAndIdIgnoreCaseContainingOrCantidadIgnoreCaseContainingOrPrecioIgnoreCaseContainingOrFechaIgnoreCaseContaining(Long filtertype, String id, String cantidad, String precio, String fecha, Pageable oPageable);

    Page<CompraEntity> findByProductoIdAndIdIgnoreCaseContainingOrCantidadIgnoreCaseContainingOrPrecioIgnoreCaseContainingOrFechaIgnoreCaseContaining(Long filtertype, String id, String cantidad, String precio, String fecha, Pageable oPageable);

    
    @Query(value = "SELECT * FROM compra where id_factura IN (SELECT id FROM factura WHERE id_usuario = :id_usuario)", nativeQuery = true)
    Page<CompraEntity> findByCompraIdUsuarioPage(Long id_usuario, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM compra where id_factura IN (SELECT id FROM factura WHERE id_usuario = :id_usuario)", nativeQuery = true)
    Long findByCompraIdUsuarioCount(Long id_usuario);
    
    @Query(value = "SELECT * FROM compra where id_factura IN (SELECT id FROM factura WHERE id_usuario = :id_usuario) AND id = :id_compra", nativeQuery = true)
    CompraEntity findByCompraIdUsuarioView(Long id_usuario, Long id_compra);
    
}
