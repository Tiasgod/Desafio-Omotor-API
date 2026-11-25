package oficina.rodalivre.api.controllers;
import lombok.RequiredArgsConstructor;
import oficina.rodalivre.service.ExportacaoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/exportacao")
@RequiredArgsConstructor
public class ExportacaoController {

    private final ExportacaoService exportacaoService;

    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportarCsv() {
        byte[] arquivo = exportacaoService.exportarCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"rodalivre-dados.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(arquivo);
    }
}
