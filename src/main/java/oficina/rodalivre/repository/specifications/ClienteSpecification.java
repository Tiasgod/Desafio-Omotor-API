package oficina.rodalivre.repository.specifications;
import oficina.rodalivre.api.dtos.filters.ClienteFiltroDTO;
import oficina.rodalivre.entity.ClienteServicoEntity;
import org.springframework.data.jpa.domain.Specification;
import java.util.Locale;

public final class ClienteSpecification {

    private ClienteSpecification() {}

    public static Specification<ClienteServicoEntity> filtrar(ClienteFiltroDTO filtro) {
        return Specification.allOf(
                nomeContem(filtro.nome()),
                cidadeIgual(filtro.cidade())
        );
    }
    
    private static Specification<ClienteServicoEntity> nomeContem(String nome) {
        return (root, query, cb) -> {
            if (nome == null || nome.isBlank()) {
                return cb.conjunction();
            }

            String valor = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.like(cb.lower(root.get("nome")), valor);
        };
    }

    private static Specification<ClienteServicoEntity> cidadeIgual(String cidade) {
        return (root, query, cb) -> {
            if (cidade == null || cidade.isBlank()) {
                return cb.conjunction();
            }

            String valor = cidade.trim().toLowerCase(Locale.ROOT);
            return cb.equal(cb.lower(root.get("cidade")), valor);
        };
    }
}
