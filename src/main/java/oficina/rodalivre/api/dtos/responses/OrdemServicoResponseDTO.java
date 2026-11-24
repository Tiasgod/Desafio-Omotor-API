package oficina.rodalivre.api.dtos.responses;
import com.fasterxml.jackson.annotation.JsonFormat;
import oficina.rodalivre.entity.enums.StatusOrdem;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdemServicoResponseDTO(
        Long id,
        Long clienteId,
        String clienteNome,
        Long servicoId,
        String servicoNome,
        @JsonFormat(
                pattern = "yyyy-MM-dd'T'HH:mm:ss",
                shape = JsonFormat.Shape.STRING
        )
        LocalDateTime dataEntrada,
        String modeloBicicleta,
        String descricao,
        BigDecimal valor,
        StatusOrdem status
) {}
