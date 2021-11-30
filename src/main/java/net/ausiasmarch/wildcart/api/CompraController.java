package net.ausiasmarch.wildcart.api;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import net.ausiasmarch.wildcart.entity.CompraEntity;
import net.ausiasmarch.wildcart.entity.UsuarioEntity;
import net.ausiasmarch.wildcart.repository.CompraRepository;
import net.ausiasmarch.wildcart.repository.FacturaRepository;
import net.ausiasmarch.wildcart.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/compra")
public class CompraController {

    @Autowired
    HttpSession oHttpSession;

    @Autowired
    CompraRepository oCompraRepository;

    @Autowired
    FacturaRepository oFacturaRepository;

    @Autowired
    CompraService oCompraService;

    @GetMapping("/{id}")
    public ResponseEntity<CompraEntity> view(@PathVariable(value = "id") Long id) {
        CompraEntity oCompraEntity = oCompraRepository.getById(id);
        UsuarioEntity oUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        if (oUsuarioEntity == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } else if (oUsuarioEntity.getTipousuario().getId() == 1) {
            if (oCompraRepository.existsById(id)) {
                return new ResponseEntity<CompraEntity>(oCompraRepository.getById(id), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } else if (oCompraRepository.existsById(id) && oUsuarioEntity.getId() == oCompraEntity.getFactura().getUsuario().getId()) {

            return new ResponseEntity<CompraEntity>(oCompraRepository.findByCompraIdUsuarioView(oUsuarioEntity.getId(), oCompraRepository.getById(id).getId()), HttpStatus.OK);

        } else if (oCompraRepository.existsById(id) && oUsuarioEntity.getId() != oCompraEntity.getFactura().getUsuario().getId()) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        CompraEntity oCompraEntity = null;
        UsuarioEntity oUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        if (oUsuarioEntity == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } else if (oUsuarioEntity.getTipousuario().getId() == 1) {

            return new ResponseEntity<Long>(oCompraRepository.count(), HttpStatus.OK);

        } else {

            return new ResponseEntity<Long>(oCompraRepository.findByCompraIdUsuarioCount(oUsuarioEntity.getId()), HttpStatus.OK);
        }

    }

    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody CompraEntity oCompraEntity) {
        UsuarioEntity oUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        if (oUsuarioEntity == null) {
            return new ResponseEntity<Long>(0L, HttpStatus.UNAUTHORIZED);
        } else {
            if (oUsuarioEntity.getTipousuario().getId() == 1) {
                oCompraEntity.setId(null);
                return new ResponseEntity<CompraEntity>(oCompraRepository.save(oCompraEntity), HttpStatus.OK);
            } else {
                return new ResponseEntity<Long>(0L, HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @PutMapping("")
    public ResponseEntity<?> update(@RequestBody CompraEntity oCompraEntity) {
        UsuarioEntity oUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        if (oUsuarioEntity == null) {
            return new ResponseEntity<Long>(0L, HttpStatus.UNAUTHORIZED);
        } else {
            if (oUsuarioEntity.getTipousuario().getId() == 1) {
                if (oCompraRepository.existsById(oCompraEntity.getId())) {
                    return new ResponseEntity<CompraEntity>(oCompraRepository.save(oCompraEntity), HttpStatus.OK);
                } else {
                    return new ResponseEntity<Long>(0L, HttpStatus.NOT_MODIFIED);
                }
            } else {
                return new ResponseEntity<Long>(0L, HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(value = "id") Long id) {
        UsuarioEntity oUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        if (oUsuarioEntity == null) {
            return new ResponseEntity<Long>(0L, HttpStatus.UNAUTHORIZED);
        } else {
            if (oUsuarioEntity.getTipousuario().getId() == 1) {

                if (oCompraRepository.existsById(id)) {
                    oCompraRepository.deleteById(id);
                    if (oCompraRepository.existsById(id)) {
                        return new ResponseEntity<Long>(0L, HttpStatus.NOT_MODIFIED);
                    } else {
                        return new ResponseEntity<Long>(id, HttpStatus.OK);
                    }
                } else {
                    return new ResponseEntity<Long>(0L, HttpStatus.NOT_MODIFIED);
                }
            } else {
                return new ResponseEntity<Long>(0L, HttpStatus.UNAUTHORIZED);
            }
        }
    }
    
    @GetMapping("/page")
    public ResponseEntity<?> getPage(
            @PageableDefault(page = 0, size = 5, direction = Direction.ASC) Pageable oPageable,
            @RequestParam(required = false) Long filtertype,
            @RequestParam(required = false) String filter) {

        UsuarioEntity oUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");

        if (oUsuarioEntity == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } else {

            if (oUsuarioEntity.getTipousuario().getId() == 1) {
                Page<CompraEntity> oPage;
                if (filtertype != null) {
                    oPage = oCompraRepository.findByFacturaIdAndIdIgnoreCaseContainingOrCantidadIgnoreCaseContainingOrPrecioIgnoreCaseContainingOrFechaIgnoreCaseContaining(
                            filtertype,
                            filter == null ? "" : filter, filter == null ? "" : filter, filter == null ? "" : filter, filter == null ? "" : filter, oPageable);
                } else {
                    oPage = oCompraRepository.findByIdIgnoreCaseContainingOrCantidadIgnoreCaseContainingOrPrecioIgnoreCaseContainingOrFechaIgnoreCaseContaining(
                            filter == null ? "" : filter, filter == null ? "" : filter, filter == null ? "" : filter, filter == null ? "" : filter, oPageable);
                }

                return new ResponseEntity<Page<CompraEntity>>(oPage, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @PostMapping("/compra")
    public ResponseEntity<?> createCompra() {
        UsuarioEntity oUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        if (oUsuarioEntity == null) {
            return new ResponseEntity<Long>(0L, HttpStatus.UNAUTHORIZED);
        } else {
            if (oUsuarioEntity.getTipousuario().getId() == 1) {
                return new ResponseEntity<CompraEntity>(oCompraRepository.save(oCompraService.generateRandomCompra()),
                        HttpStatus.OK);
            }

            return new ResponseEntity<Long>(0L, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/compra/{amount}")
    public ResponseEntity<?> createCompras(@PathVariable(value = "amount") Integer amount
    ) {
        List<CompraEntity> compraList = new ArrayList<>();

        UsuarioEntity oUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        if (oUsuarioEntity == null) {
            return new ResponseEntity<Long>(0L, HttpStatus.UNAUTHORIZED);
        } else {
            if (oUsuarioEntity.getTipousuario().getId() == 1) {

                for (int i = 0; i < amount; i++) {
                    CompraEntity oCompraEntity = oCompraService.generateRandomCompra();
                    oCompraRepository.save(oCompraEntity);
                    compraList.add(oCompraEntity);
                }
            }
            return new ResponseEntity<List<CompraEntity>>(compraList, HttpStatus.OK);
        }
    }
}
