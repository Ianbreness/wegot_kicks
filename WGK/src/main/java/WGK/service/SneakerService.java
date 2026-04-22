package WGK.service;

import WGK.domain.Sneaker;
import WGK.repository.SneakerRepository;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.ArrayList;

@Service
public class SneakerService {

    @Autowired
    private SneakerRepository sneakerRepository;

    @Transactional(readOnly = true)
    public List<Sneaker> getSneakers(boolean activo) {
        if (activo) return sneakerRepository.findByActivoTrue();
        return sneakerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Sneaker getSneaker(Integer id) {
        return sneakerRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Sneaker> buscarPorNombre(String nombre) {
        return sneakerRepository.findByDescripcionContainingIgnoreCase(nombre);
    }

    @Transactional(readOnly = true)
    public List<Sneaker> getPorMarca(Integer idMarca) {
        return sneakerRepository.findByMarcaIdMarca(idMarca);
    }

    /**
     * Guarda el sneaker.
     *
     * Lógica de imagen (en orden de prioridad):
     *   1. Si se sube un archivo → se copia a C:/wgk-imagenes/ y se guarda /uploads/nombre.jpg en BD
     *   2. Si no hay archivo pero rutaImagen ya tiene valor → se conserva (URL externa o ruta previa)
     *   3. Si no hay nada → ruta queda null (se mostrará placeholder)
     */
    @Transactional
    /**
     * Guarda el sneaker con soporte para múltiples imágenes.
     * - imagenPrincipal: foto principal (la que aparece en el catálogo)
     * - imagenesExtra: lista de fotos adicionales (thumbnails en detalle)
     * Si no se sube ningún archivo, se conservan las rutas actuales.
     */
    public void save(Sneaker sneaker,
                     MultipartFile imagenPrincipal,
                     List<MultipartFile> imagenesExtra) throws Exception {

        // Carpeta static/img dentro del proyecto
        String staticImg = "src/main/resources/static/img/";
        Path carpeta = Paths.get(staticImg);
        Files.createDirectories(carpeta);

        // ── Imagen principal ──
        if (imagenPrincipal != null && !imagenPrincipal.isEmpty()) {
            String nombre = System.currentTimeMillis() + "_" + imagenPrincipal.getOriginalFilename();
            Files.copy(imagenPrincipal.getInputStream(),
                       carpeta.resolve(nombre),
                       StandardCopyOption.REPLACE_EXISTING);
            sneaker.setRutaImagen("/img/" + nombre);
        }

        // ── Imágenes adicionales ──
        if (imagenesExtra != null) {
            List<String> rutas = new ArrayList<>();
            for (MultipartFile f : imagenesExtra) {
                if (f != null && !f.isEmpty()) {
                    String nombre = System.currentTimeMillis() + "_" + f.getOriginalFilename();
                    Files.copy(f.getInputStream(),
                               carpeta.resolve(nombre),
                               StandardCopyOption.REPLACE_EXISTING);
                    rutas.add("/img/" + nombre);
                }
            }
            if (!rutas.isEmpty()) {
                sneaker.setImagenes(String.join(",", rutas));
            }
        }

        sneakerRepository.save(sneaker);
    }

    @Transactional
    public void delete(Integer id) throws Exception {
        sneakerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Sneaker> consultaDerivada(double precioInf, double precioSup) {
        return sneakerRepository.findByPrecioBetweenOrderByPrecioAsc(
                new BigDecimal(precioInf), new BigDecimal(precioSup));
    }

    @Transactional(readOnly = true)
    public List<Sneaker> consultaJPQL(double precioInf, double precioSup) {
        return sneakerRepository.buscarPorRangoPrecioJPQL(
                new BigDecimal(precioInf), new BigDecimal(precioSup));
    }

    @Transactional(readOnly = true)
    public List<Sneaker> consultaSQL(double precioInf, double precioSup) {
        return sneakerRepository.buscarPorRangoPrecioSQL(
                new BigDecimal(precioInf), new BigDecimal(precioSup));
    }
}