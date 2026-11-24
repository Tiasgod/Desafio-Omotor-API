package oficina.rodalivre.api.controllers;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import oficina.rodalivre.api.dtos.requests.ClienteRequestDTO;
import oficina.rodalivre.api.dtos.requests.ClienteUpdateDTO;
import oficina.rodalivre.api.dtos.responses.ClienteResponseDTO;
import oficina.rodalivre.service.ClienteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
@Validated
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> cadastrar(@Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clienteService.cadastrar(request));
    }
    @GetMapping
    public ResponseEntity<Page<ClienteResponseDTO>> listar(@RequestParam(required = false) String nome,@RequestParam(required = false) String cidade,@RequestParam(defaultValue = "0") @Min(0) int page,@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(clienteService.listar(nome, cidade, pageable));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteUpdateDTO request) {
        return ResponseEntity.ok(clienteService.atualizar(id, request));
    }
}
