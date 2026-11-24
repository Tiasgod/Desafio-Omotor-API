package oficina.rodalivre.repository;
import oficina.rodalivre.entity.OrdemServicoEntity;
import oficina.rodalivre.entity.enums.StatusOrdem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface OrdemServicoRepository
        extends JpaRepository<OrdemServicoEntity, Long>,
                JpaSpecificationExecutor<OrdemServicoEntity> {
    boolean existsByCliente_IdAndStatus(Long clienteId,StatusOrdem status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT o
            FROM OrdemServicoEntity o
            WHERE o.id = :id
            """)
    Optional<OrdemServicoEntity> buscarPorIdComBloqueio(
            @Param("id") Long id
    );
}
