package oficina.rodalivre.api.dtos.responses;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        String cidade,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss",shape = JsonFormat.Shape.STRING)
        LocalDateTime dataCadastro,
        Boolean ativo
) {}
