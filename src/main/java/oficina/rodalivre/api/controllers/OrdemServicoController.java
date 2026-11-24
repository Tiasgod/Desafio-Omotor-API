package oficina.rodalivre.api.controllers;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import oficina.rodalivre.api.dtos.requests.OrdemServicoRequestDTO;
import oficina.rodalivre.api.dtos.requests.OrdemServicoStatusDTO;
import oficina.rodalivre.api.dtos.responses.OrdemServicoResponseDTO;
import oficina.rodalivre.entity.enums.StatusOrdem;
import oficina.rodalivre.service.OrdemServicoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/ordens-servico")
@Validated
@RequiredArgsConstructor
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;
    @PostMapping
    public ResponseEntity<OrdemServicoResponseDTO> cadastrar(@Valid @RequestBody OrdemServicoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ordemServicoService.cadastrar(request));
    }
    @GetMapping
    public ResponseEntity<Page<OrdemServicoResponseDTO>> listar(@RequestParam(required = false) StatusOrdem status,@RequestParam(required = false) Long clienteId,@RequestParam(required = false) Long servicoId,@RequestParam(required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,@RequestParam(required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,@RequestParam(defaultValue = "0") @Min(0) int page,@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ordemServicoService.listar(
                status, clienteId, servicoId, dataInicio, dataFim, pageable));
    }
    @GetMapping("/{id}")
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ordemServicoService.buscarPorId(id));
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrdemServicoResponseDTO> atualizarStatus(
            @PathVariable Long id,@Valid @RequestBody OrdemServicoStatusDTO request) {
        return ResponseEntity.ok(ordemServicoService.atualizarStatus(id, request));
    }
}
