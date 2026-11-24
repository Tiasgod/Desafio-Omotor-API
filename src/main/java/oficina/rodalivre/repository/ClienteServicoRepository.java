package oficina.rodalivre.repository;
import oficina.rodalivre.entity.ClienteServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ClienteServicoRepository
        extends JpaRepository<ClienteServicoEntity, Long>,
                JpaSpecificationExecutor<ClienteServicoEntity> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email,Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM ClienteServicoEntity c
            WHERE c.id = :id
            """)
    Optional<ClienteServicoEntity> buscarPorIdComBloqueio(
            @Param("id") Long id
    );
}
