package WGK.service;

import WGK.domain.Marca;
import WGK.repository.MarcaRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MarcaService {

    @Autowired
    private MarcaRepository marcaRepository;

    // Lee la ruta de carpeta externa desde application.properties
    @Value("${wgk.imagenes.ruta}")
    private String rutaImagenes;

    @Transactional(readOnly = true)
    public List<Marca> getMarcas(boolean activo) {
        if (activo) return marcaRepository.findByActivoTrue();
        return marcaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Marca> getMarca(Integer id) {
        return marcaRepository.findById(id);
    }

    /**
     * Guarda la marca.
     *
     * Lógica de imagen:
     *   1. Si se sube un archivo → se copia a C:/wgk-imagenes/ y se guarda /uploads/nombre.jpg en BD
     *   2. Si no hay archivo pero rutaImagen ya tiene valor → se conserva
     *   3. Si no hay nada → ruta queda null
     */
    @Transactional
    public void save(Marca marca, MultipartFile imagenFile) throws Exception {

        if (imagenFile != null && !imagenFile.isEmpty()) {
            String nombreArchivo = imagenFile.getOriginalFilename();

            // Crear la carpeta si no existe aún
            Path carpeta = Paths.get(rutaImagenes);
            Files.createDirectories(carpeta);

            // Copiar el archivo — sobreescribe si ya existe uno con el mismo nombre
            Path destino = carpeta.resolve(nombreArchivo);
            Files.copy(imagenFile.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            // Guardar en BD la URL relativa que Spring servirá en /uploads/
            marca.setRutaImagen("/uploads/" + nombreArchivo);
        }
        // Si no se subió archivo, se respeta el valor actual de rutaImagen

        marcaRepository.save(marca);
    }

    @Transactional
    public void delete(Integer id) throws Exception {
        marcaRepository.deleteById(id);
    }
}