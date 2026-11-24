package oficina.rodalivre.api.controllers;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import oficina.rodalivre.api.dtos.requests.CatalogoServicoRequestDTO;
import oficina.rodalivre.api.dtos.requests.CatalogoServicoUpdateDTO;
import oficina.rodalivre.api.dtos.responses.CatalogoServicoResponseDTO;
import oficina.rodalivre.entity.enums.CategoriaServico;
import oficina.rodalivre.service.CatalogoServicoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/servicos")
@Validated
@RequiredArgsConstructor
public class CatalogoServicoController {

    private final CatalogoServicoService catalogoServicoService;
    @PostMapping
    public ResponseEntity<CatalogoServicoResponseDTO> cadastrar(@Valid @RequestBody CatalogoServicoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogoServicoService.cadastrar(request));
    }
    @GetMapping
    public ResponseEntity<Page<CatalogoServicoResponseDTO>> listar(@RequestParam(required = false) String nome,@RequestParam(required = false) CategoriaServico categoria,@RequestParam(defaultValue = "0") @Min(0) int page,@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(catalogoServicoService.listar(nome, categoria, pageable));
    }
    @GetMapping("/{id}")
    public ResponseEntity<CatalogoServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoServicoService.buscarPorId(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<CatalogoServicoResponseDTO> atualizar(
            @PathVariable Long id,@Valid @RequestBody CatalogoServicoUpdateDTO request) {
        return ResponseEntity.ok(catalogoServicoService.atualizar(id, request));
    }
}
