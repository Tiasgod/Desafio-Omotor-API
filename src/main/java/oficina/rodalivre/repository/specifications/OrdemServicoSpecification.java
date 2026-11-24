package oficina.rodalivre.repository.specifications;
import oficina.rodalivre.api.dtos.filters.OrdemServicoFiltroDTO;
import oficina.rodalivre.entity.OrdemServicoEntity;
import oficina.rodalivre.entity.enums.StatusOrdem;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;

public final class OrdemServicoSpecification {

    private OrdemServicoSpecification() {}

    public static Specification<OrdemServicoEntity> filtrar(OrdemServicoFiltroDTO filtro) {
        return Specification.allOf(statusIgual(filtro.status()),clienteIgual(filtro.clienteId()),servicoIgual(filtro.servicoId()),dataAPartirDe(filtro.dataInicio()),dataAte(filtro.dataFim()));
    }

    private static Specification<OrdemServicoEntity> statusIgual(StatusOrdem status) {
        return (root, query, cb) -> status == null
                ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    private static Specification<OrdemServicoEntity> clienteIgual(Long clienteId) {
        return (root, query, cb) -> clienteId == null
                ? cb.conjunction()
                : cb.equal(root.get("cliente").get("id"), clienteId);
    }

    private static Specification<OrdemServicoEntity> servicoIgual(Long servicoId) {
        return (root, query, cb) -> servicoId == null
                ? cb.conjunction()
                : cb.equal(root.get("servico").get("id"), servicoId);
    }

    private static Specification<OrdemServicoEntity> dataAPartirDe(LocalDateTime dataInicio) {
        return (root, query, cb) -> dataInicio == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("dataEntrada"), dataInicio);
    }

    private static Specification<OrdemServicoEntity> dataAte(LocalDateTime dataFim) {
        return (root, query, cb) -> dataFim == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("dataEntrada"), dataFim);
    }
}
