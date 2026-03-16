package WGK.service;

import WGK.domain.Marca;
import WGK.repository.MarcaRepository;
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
        if (activo) {
            return marcaRepository.findByActivoTrue();
        }
        return marcaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Marca> getMarca(Integer id) {
        return marcaRepository.findById(id);
    }

    @Transactional
    public void save(Marca marca, MultipartFile imagenFile) throws Exception {
        if (imagenFile != null && !imagenFile.isEmpty()) {
            String nombreArchivo = imagenFile.getOriginalFilename();
            marca.setRutaImagen("/img/" + nombreArchivo);
        }
        marcaRepository.save(marca);
    }

    @Transactional
    public void delete(Integer id) throws Exception {
        marcaRepository.deleteById(id);
    }
}