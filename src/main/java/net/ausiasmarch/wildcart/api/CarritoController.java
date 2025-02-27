package net.ausiasmarch.wildcart.api;

import java.util.ArrayList;
import javax.servlet.http.HttpSession;
import net.ausiasmarch.wildcart.entity.CarritoEntity;
import net.ausiasmarch.wildcart.entity.ProductoEntity;
import net.ausiasmarch.wildcart.entity.UsuarioEntity;
import net.ausiasmarch.wildcart.repository.CarritoRepository;
import net.ausiasmarch.wildcart.repository.ProductoRepository;
import net.ausiasmarch.wildcart.repository.UsuarioRepository;
import net.ausiasmarch.wildcart.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    HttpSession oHttpSession;

    @Autowired
    CarritoRepository oCarritoRepository;

    @Autowired
    UsuarioRepository oUsuarioRepository;
    
    @Autowired
    ProductoRepository oProductoRepository;
    
    @Autowired
    CarritoService oCarritoService;

    @GetMapping("")
    public ResponseEntity<Page<CarritoEntity>> get(@PageableDefault(page = 0, size = 10, direction = Sort.Direction.ASC) Pageable oPageable) {
        UsuarioEntity oSessionUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        Page<CarritoEntity> oCarritoEntity;
        try {
            oSessionUsuarioEntity = oUsuarioRepository.findById(oSessionUsuarioEntity.getId()).get();
        } catch (Exception ex) {
            oSessionUsuarioEntity = null;
        }
        if (oSessionUsuarioEntity != null) {
            oCarritoEntity = oCarritoRepository.findAllByUsuario(oSessionUsuarioEntity, oPageable);
            return new ResponseEntity<Page<CarritoEntity>>(oCarritoEntity, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }
    
    @GetMapping("/admin/filter/{filter}")
    public ResponseEntity<Page<CarritoEntity>> getAdmin(@PathVariable(value = "filter") String filter, @PageableDefault(page = 0, size = 10, direction = Sort.Direction.ASC) Pageable oPageable) {
        UsuarioEntity oSessionUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        Page<CarritoEntity> oCarritoEntity;
        try {
            oSessionUsuarioEntity = oUsuarioRepository.findById(oSessionUsuarioEntity.getId()).get();
        } catch (Exception ex) {
            oSessionUsuarioEntity = null;
        }
        if (oSessionUsuarioEntity != null && oSessionUsuarioEntity.getTipousuario().getId() == 1) {
            oCarritoEntity = oCarritoRepository.findAllByIdIgnoreCaseContainingOrCantidadIgnoreCaseContainingOrPrecioIgnoreCaseContainingOrProductoIgnoreCaseContainingOrUsuarioIgnoreCaseContaining(filter, filter, filter, filter, filter, oPageable);
            return new ResponseEntity<Page<CarritoEntity>>(oCarritoEntity, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/{id}/{amount}")
    public ResponseEntity<?> add(@PathVariable(value = "id") long id, @PathVariable(value = "amount") int amount) {
        UsuarioEntity oSessionUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        CarritoEntity oCarritoEntity = new CarritoEntity();
        CarritoEntity oCarritoEntityProducto;
        ProductoEntity oProducto = new ProductoEntity();
        try {
            oSessionUsuarioEntity = oUsuarioRepository.findById(oSessionUsuarioEntity.getId()).get();
        } catch (Exception ex) {
            oSessionUsuarioEntity = null;
        }
        if (oSessionUsuarioEntity != null) {
            oProducto = oProductoRepository.getById(id);
            oCarritoEntityProducto = oCarritoRepository.findByUsuarioAndProducto(oSessionUsuarioEntity, oProducto);
            oCarritoEntity.setProducto(oProducto);
            oCarritoEntity.setUsuario(oSessionUsuarioEntity);
            if (oCarritoEntityProducto == null) {
                oCarritoEntity.setId(null);
                oCarritoEntity.setCantidad(amount);
            } else {
                oCarritoEntity.setId(oCarritoEntityProducto.getId());
                oCarritoEntity.setCantidad(oCarritoEntityProducto.getCantidad() + amount);
            }
            oCarritoRepository.save(oCarritoEntity);
            return new ResponseEntity<>(null, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }
    
    @PostMapping("/admin/{id}/{amount}/{user}")
    public ResponseEntity<?> addAdmin(@PathVariable(value = "id") long id, @PathVariable(value = "amount") int amount, @PathVariable(value = "user") long user) {
        UsuarioEntity oSessionUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        CarritoEntity oCarritoEntity = new CarritoEntity();
        CarritoEntity oCarritoEntityProducto;
        ProductoEntity oProducto;
        UsuarioEntity oUsuario;
        try {
            oSessionUsuarioEntity = oUsuarioRepository.findById(oSessionUsuarioEntity.getId()).get();
        } catch (Exception ex) {
            oSessionUsuarioEntity = null;
        }
        if (oSessionUsuarioEntity != null && oSessionUsuarioEntity.getTipousuario().getId() == 1) {
            oProducto = oProductoRepository.getById(id);
            oUsuario = oUsuarioRepository.getById(user);
            oCarritoEntityProducto = oCarritoRepository.findByUsuarioAndProducto(oUsuario, oProducto);
            oCarritoEntity.setProducto(oProducto);
            oCarritoEntity.setUsuario(oUsuario);
            if (oCarritoEntityProducto == null) {
                oCarritoEntity.setId(null);
                oCarritoEntity.setCantidad(amount);
            } else {
                oCarritoEntity.setId(oCarritoEntityProducto.getId());
                oCarritoEntity.setCantidad(oCarritoEntityProducto.getCantidad() + amount);
            }
            oCarritoRepository.save(oCarritoEntity);
            return new ResponseEntity<>(null, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }
    
    @PostMapping("/random/{rows}")
    public ResponseEntity<?> random(@PathVariable(value = "rows") int rows) {
        try {
            ArrayList<CarritoEntity> carritos = oCarritoService.generate(rows);
            for (int i = 0; i < carritos.size(); i++) {
                oCarritoRepository.save(carritos.get(i));
            }
            return new ResponseEntity<>(carritos, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }   
    }
    
    @DeleteMapping("")
    public ResponseEntity<?> delete() {
        UsuarioEntity oSessionUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        try {
            oSessionUsuarioEntity = oUsuarioRepository.findById(oSessionUsuarioEntity.getId()).get();
        } catch (Exception ex) {
            oSessionUsuarioEntity = null;
        }
        if (oSessionUsuarioEntity != null) {
            oCarritoRepository.deleteAllByUsuario(oSessionUsuarioEntity);
            return new ResponseEntity<>(null, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }
    
    @DeleteMapping("/admin/{user}")
    public ResponseEntity<?> deleteAdmin(@PathVariable(value = "user") long user) {
        UsuarioEntity oSessionUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        UsuarioEntity oUsuario;
        try {
            oSessionUsuarioEntity = oUsuarioRepository.findById(oSessionUsuarioEntity.getId()).get();
        } catch (Exception ex) {
            oSessionUsuarioEntity = null;
        }
        if (oSessionUsuarioEntity != null && oSessionUsuarioEntity.getTipousuario().getId() == 1) {
            oUsuario = oUsuarioRepository.getById(user);
            oCarritoRepository.deleteAllByUsuario(oUsuario);
            return new ResponseEntity<>(null, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }
    
    @DeleteMapping("/{id_producto}/{amount}")
    public ResponseEntity<?> deleteOne(@PathVariable(value = "id_producto") long id_producto, @PathVariable(value = "amount") int amount) {
        UsuarioEntity oSessionUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        CarritoEntity oCarritoEntity;
        ProductoEntity oProducto;
        try {
            oSessionUsuarioEntity = oUsuarioRepository.findById(oSessionUsuarioEntity.getId()).get();
        } catch (Exception ex) {
            oSessionUsuarioEntity = null;
        }
        if (oSessionUsuarioEntity != null) {
            oProducto = oProductoRepository.getById(id_producto);
            oCarritoEntity = oCarritoRepository.findByUsuarioAndProducto(oSessionUsuarioEntity, oProducto);
            if (amount >= oCarritoEntity.getCantidad()) {
                oCarritoRepository.deleteByUsuarioAndProducto(oSessionUsuarioEntity, oProducto);
            } else {
                oCarritoEntity.setCantidad(oCarritoEntity.getCantidad() - amount);
                oCarritoRepository.save(oCarritoEntity);
            }
            return new ResponseEntity<>(null, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }
    
    @DeleteMapping("/admin/{id_producto}/{amount}/{user}")
    public ResponseEntity<?> deleteOneAdmin(@PathVariable(value = "id_producto") long id_producto, @PathVariable(value = "amount") int amount, @PathVariable(value = "user") long user) {
        UsuarioEntity oSessionUsuarioEntity = (UsuarioEntity) oHttpSession.getAttribute("usuario");
        CarritoEntity oCarritoEntity = new CarritoEntity();
        ProductoEntity oProducto;
        UsuarioEntity oUsuario;
        try {
            oSessionUsuarioEntity = oUsuarioRepository.findById(oSessionUsuarioEntity.getId()).get();
        } catch (Exception ex) {
            oSessionUsuarioEntity = null;
        }
        if (oSessionUsuarioEntity != null && oSessionUsuarioEntity.getTipousuario().getId() == 1) {
            oUsuario = oUsuarioRepository.getById(user);
            oProducto = oProductoRepository.getById(id_producto);
            oCarritoEntity = oCarritoRepository.findByUsuarioAndProducto(oUsuario, oProducto);
            if (amount >= oCarritoEntity.getCantidad()) {
                oCarritoRepository.deleteByUsuarioAndProducto(oUsuario, oProducto);
            } else {
                oCarritoEntity.setCantidad(oCarritoEntity.getCantidad() - amount);
                oCarritoRepository.save(oCarritoEntity);
            }
            return new ResponseEntity<>(null, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }
    
}