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

@Service
public class SneakerService {

    @Autowired
    private SneakerRepository sneakerRepository;

    // Lee la ruta de carpeta externa desde application.properties
    @Value("${wgk.imagenes.ruta}")
    private String rutaImagenes;

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
    public void save(Sneaker sneaker, MultipartFile imagenFile) throws Exception {

        if (imagenFile != null && !imagenFile.isEmpty()) {
            String nombreArchivo = imagenFile.getOriginalFilename();

            // Crear la carpeta si no existe aún
            Path carpeta = Paths.get(rutaImagenes);
            Files.createDirectories(carpeta);

            // Copiar el archivo — sobreescribe si ya existe uno con el mismo nombre
            Path destino = carpeta.resolve(nombreArchivo);
            Files.copy(imagenFile.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            // Guardar en BD la URL relativa que Spring servirá en /uploads/
            sneaker.setRutaImagen("/uploads/" + nombreArchivo);
        }
        // Si no se subió archivo, se respeta el valor actual de rutaImagen

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