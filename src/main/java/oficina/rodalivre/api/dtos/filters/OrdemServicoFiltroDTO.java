package oficina.rodalivre.api.dtos.filters;
import oficina.rodalivre.entity.enums.StatusOrdem;
import java.time.LocalDateTime;

public record OrdemServicoFiltroDTO(StatusOrdem status,Long clienteId,Long servicoId,LocalDateTime dataInicio,LocalDateTime dataFim) 
{}
