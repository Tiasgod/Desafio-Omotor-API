package oficina.rodalivre.api.dtos.requests;
import jakarta.validation.constraints.NotNull;
import oficina.rodalivre.entity.enums.StatusOrdem;

public record OrdemServicoStatusDTO(

        @NotNull(message = "O status é obrigatório")
        StatusOrdem status
) {}
