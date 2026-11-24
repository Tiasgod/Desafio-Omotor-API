package oficina.rodalivre.api.dtos.responses;
import oficina.rodalivre.entity.enums.CategoriaServico;
import java.math.BigDecimal;

public record CatalogoServicoResponseDTO(Long id, String codigo, String nome, CategoriaServico categoria, BigDecimal precoSugerido, Boolean ativo)
{}
