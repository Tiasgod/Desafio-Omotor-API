package oficina.rodalivre.repository;
import oficina.rodalivre.entity.CatalogoServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CatalogoServicoRepository
        extends JpaRepository<CatalogoServicoEntity, Long>,
                JpaSpecificationExecutor<CatalogoServicoEntity> {
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
}
