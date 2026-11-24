package oficina.rodalivre.api.dtos.responses;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
public record ErroResponseDTO(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss",shape = JsonFormat.Shape.STRING)
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {}
