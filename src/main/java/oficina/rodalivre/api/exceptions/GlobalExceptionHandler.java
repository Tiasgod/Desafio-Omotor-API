package oficina.rodalivre.api.exceptions;
import jakarta.validation.ConstraintViolationException;
import oficina.rodalivre.api.dtos.responses.ErroResponseDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatarErroCampo)
                .distinct()
                .collect(Collectors.joining("; "));

        return criarResposta(HttpStatus.BAD_REQUEST,
                mensagem.isBlank() ? "Dados da requisição inválidos" : mensagem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResponseDTO> tratarRestricoes(ConstraintViolationException ex) {
        String mensagem = ex.getConstraintViolations().stream()
                .map(violacao -> violacao.getMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        return criarResposta(HttpStatus.BAD_REQUEST,
                mensagem.isBlank() ? "Parâmetro inválido" : mensagem);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacaoMetodo(HandlerMethodValidationException ex) {
        String mensagem = ex.getAllValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream())
                .map(erro -> erro.getDefaultMessage())
                .filter(msg -> msg != null && !msg.isBlank())
                .distinct()
                .collect(Collectors.joining("; "));

        return criarResposta(HttpStatus.BAD_REQUEST,
                mensagem.isBlank() ? "Parâmetros inválidos" : mensagem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarJsonInvalido(HttpMessageNotReadableException ex) {
        return criarResposta(HttpStatus.BAD_REQUEST,
                "JSON inválido, campo desconhecido ou valor incompatível");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tratarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return criarResposta(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro: " + ex.getName());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponseDTO> tratarParametroAusente(
            MissingServletRequestParameterException ex) {

        return criarResposta(HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente: " + ex.getParameterName());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponseDTO> tratarRegraNegocio(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());

        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String mensagem = ex.getReason() != null
                ? ex.getReason()
                : "Não foi possível processar a requisição";

        return criarResposta(status, mensagem);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponseDTO> tratarIntegridade(DataIntegrityViolationException ex) {
        SQLException erro = buscarErroSql(ex);

        if (erro != null) {
            String estado = erro.getSQLState();

            if ("23505".equals(estado)) {
                String detalhe = erro.getMessage();

                if (detalhe != null) {
                    if (detalhe.contains("uk_clientes_email")) {
                        return criarResposta(HttpStatus.CONFLICT, "Já existe um cliente com este e-mail");
                    }

                    if (detalhe.contains("uk_servicos_codigo")) {
                        return criarResposta(HttpStatus.CONFLICT, "Já existe um serviço com este código");
                    }

                    if (detalhe.contains("uk_ordem_aberta_por_cliente")) {
                        return criarResposta(HttpStatus.CONFLICT, "O cliente já possui uma ordem aberta");
                    }
                }

                return criarResposta(HttpStatus.CONFLICT,"Registro duplicado");
            }

            if ("23503".equals(estado)) {
                return criarResposta(HttpStatus.CONFLICT,"Operação viola um relacionamento entre registros");
            }

            if ("23514".equals(estado) || "23502".equals(estado)) {
                return criarResposta(HttpStatus.BAD_REQUEST,"Dados violam uma restrição do banco");
            }
        }

        return criarResposta(HttpStatus.CONFLICT, "Operação viola uma restrição de integridade");
    }

    private String formatarErroCampo(FieldError erro) {
        String mensagem = erro.getDefaultMessage() != null
                ? erro.getDefaultMessage()
                : "valor inválido";

        return erro.getField() + ": " + mensagem;
    }

    private SQLException buscarErroSql(Throwable ex) {
        Throwable atual = ex;

        while (atual != null) {
            if (atual instanceof SQLException sql) {
                return sql;
            }

            atual = atual.getCause();
        }

        return null;
    }

    private ResponseEntity<ErroResponseDTO> criarResposta(HttpStatus status, String mensagem) {
        ErroResponseDTO erro = new ErroResponseDTO(LocalDateTime.now(),status.value(),status.getReasonPhrase(),mensagem);
        return ResponseEntity.status(status).body(erro);
    }
}
