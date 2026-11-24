package oficina.rodalivre.api.dtos.requests;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdemServicoRequestDTO(

        @NotNull(message = "O cliente é obrigatório")
        Long clienteId,

        @NotNull(message = "O serviço é obrigatório")
        Long servicoId,

        @NotNull(message = "A data de entrada é obrigatória")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss",shape = JsonFormat.Shape.STRING)
        LocalDateTime dataEntrada,

        @NotBlank(message = "O modelo da bicicleta é obrigatório")
        @Size(min = 2,max = 80,message = "O modelo deve ter entre 2 e 80 caracteres")
        String modeloBicicleta,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(min = 5,max = 500,message = "A descrição deve ter entre 5 e 500 caracteres")
        String descricao,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.00",inclusive = false,message = "O valor deve ser maior que zero")
        @Digits(integer = 10,fraction = 2,message = "O valor deve ter no máximo 2 casas decimais")
        BigDecimal valor

) {}
