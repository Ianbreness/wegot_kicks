package WGK.service;

import WGK.domain.Sneaker;
import WGK.repository.SneakerRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void save(Sneaker sneaker) throws Exception {
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