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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MarcaService {

    @Autowired
    private MarcaRepository marcaRepository;

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
            String nombre = System.currentTimeMillis() + "_" + imagenFile.getOriginalFilename();

            // Carpeta static/img dentro del proyecto
            Path carpeta = Paths.get("src/main/resources/static/img/");
            Files.createDirectories(carpeta);

            Files.copy(imagenFile.getInputStream(),
                       carpeta.resolve(nombre),
                       StandardCopyOption.REPLACE_EXISTING);

            marca.setRutaImagen("/img/" + nombre);
        }
        // Si no se subió archivo, se respeta el valor actual de rutaImagen

        marcaRepository.save(marca);
    }

    @Transactional
    public void delete(Integer id) throws Exception {
        marcaRepository.deleteById(id);
    }
}