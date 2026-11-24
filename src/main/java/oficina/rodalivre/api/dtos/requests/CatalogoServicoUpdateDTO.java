package oficina.rodalivre.api.dtos.requests;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import oficina.rodalivre.entity.enums.CategoriaServico;
import java.math.BigDecimal;

public record CatalogoServicoUpdateDTO(

        @NotBlank(message = "O código é obrigatório")
        @Size(min = 2, max = 20)
        String codigo,

        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 120)
        String nome,

        @NotNull(message = "A categoria é obrigatória")
        CategoriaServico categoria,

        @NotNull(message = "O preço sugerido é obrigatório")
        @DecimalMin(value = "0.00", inclusive = false, message = "O preço deve ser maior que zero")
        @Digits(integer = 10, fraction = 2)
        BigDecimal precoSugerido,

        @NotNull(message = "O campo ativo é obrigatório")
        Boolean ativo

) {}
