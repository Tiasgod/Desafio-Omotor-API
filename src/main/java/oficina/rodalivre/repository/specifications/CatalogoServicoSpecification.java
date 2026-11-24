package oficina.rodalivre.repository.specifications;
import oficina.rodalivre.api.dtos.filters.CatalogoServicoFiltroDTO;
import oficina.rodalivre.entity.CatalogoServicoEntity;
import oficina.rodalivre.entity.enums.CategoriaServico;
import org.springframework.data.jpa.domain.Specification;
import java.util.Locale;

public final class CatalogoServicoSpecification {

    private CatalogoServicoSpecification() {}

    public static Specification<CatalogoServicoEntity> filtrar(CatalogoServicoFiltroDTO filtro) {
        return Specification.allOf(
                nomeContem(filtro.nome()),
                categoriaIgual(filtro.categoria())
        );
    }

    private static Specification<CatalogoServicoEntity> nomeContem(String nome) {
        return (root, query, cb) -> {
            if (nome == null || nome.isBlank()) {
                return cb.conjunction();
            }
            String valor = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.like(cb.lower(root.get("nome")), valor);
        };
    }

    private static Specification<CatalogoServicoEntity> categoriaIgual(CategoriaServico categoria) {
        return (root, query, cb) -> {
            if (categoria == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("categoria"), categoria);
        };
    }
}
