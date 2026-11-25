package oficina.rodalivre.service;
import oficina.rodalivre.api.dtos.requests.ClienteRequestDTO;
import oficina.rodalivre.api.dtos.requests.ClienteUpdateDTO;
import oficina.rodalivre.api.dtos.responses.ClienteResponseDTO;
import oficina.rodalivre.entity.ClienteServicoEntity;
import oficina.rodalivre.repository.ClienteServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteServicoRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteServicoEntity cliente;

    @BeforeEach
    void prepararDados() {

        cliente = ClienteServicoEntity.builder()
                .nome("Pedro Pedreiro")
                .email("pedropedreiro@gmail.com")
                .telefone("31987654321")
                .cidade("Belo Horizonte")
                .ativo(true)
                .build();

        ReflectionTestUtils.setField(cliente, "id", 1L);

        ReflectionTestUtils.setField(
                cliente,
                "dataCadastro",
                LocalDateTime.of(2026, 3, 1, 10, 15)
        );
    }

    @Test
    void deveCadastrarClienteValido() {

        ClienteRequestDTO request = new ClienteRequestDTO(
                "Pedro Pedreiro",
                "pedropedreiro@gmail.com",
                "31987654321",
                "Belo Horizonte"
        );

        when(clienteRepository.save(any(ClienteServicoEntity.class)))
                .thenReturn(cliente);

        ClienteResponseDTO resposta =
                clienteService.cadastrar(request);

        assertEquals(1L, resposta.id());
        assertEquals("Pedro Pedreiro", resposta.nome());
        assertEquals("pedropedreiro@gmail.com", resposta.email());
        assertTrue(resposta.ativo());

        verify(clienteRepository).save(any(ClienteServicoEntity.class));
    }

    @Test
    void deveNormalizarEmailNoCadastro() {

        ClienteRequestDTO request = new ClienteRequestDTO(
                "Pedro Pedreiro",
                "  PEDROPEDREIRO@GMAIL.COM  ",
                "31987654321",
                "Belo Horizonte"
        );

        when(clienteRepository.save(any(ClienteServicoEntity.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        ClienteResponseDTO resposta =
                clienteService.cadastrar(request);

        assertEquals(
                "pedropedreiro@gmail.com",
                resposta.email()
        );

        verify(clienteRepository).existsByEmailIgnoreCase(
                "pedropedreiro@gmail.com"
        );
    }

    @Test
    void deveRetornar409QuandoEmailJaExiste() {

        ClienteRequestDTO request = new ClienteRequestDTO(
                "Outro Cliente",
                "pedropedreiro@gmail.com",
                "11999999999",
                "Sao Paulo"
        );

        when(clienteRepository.existsByEmailIgnoreCase(
                "pedropedreiro@gmail.com"
        )).thenReturn(true);

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> clienteService.cadastrar(request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                erro.getStatusCode()
        );

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveBuscarClientePorId() {

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        ClienteResponseDTO resposta =
                clienteService.buscarPorId(1L);

        assertEquals(1L, resposta.id());
        assertEquals("Pedro Pedreiro", resposta.nome());
        assertEquals("Belo Horizonte", resposta.cidade());

        verify(clienteRepository).findById(1L);
    }

    @Test
    void deveRetornar404QuandoClienteNaoExiste() {

        when(clienteRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> clienteService.buscarPorId(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                erro.getStatusCode()
        );
    }

    @Test
    void deveAtualizarCliente() {

        ClienteUpdateDTO request = new ClienteUpdateDTO(
                "Pedro Atualizado",
                "PEDRO@EMAIL.COM",
                "31999999999",
                "Contagem",
                false
        );

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(clienteRepository.save(any(ClienteServicoEntity.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        ClienteResponseDTO resposta =
                clienteService.atualizar(1L, request);

        assertEquals("Pedro Atualizado", resposta.nome());
        assertEquals("pedro@email.com", resposta.email());
        assertEquals("31999999999", resposta.telefone());
        assertEquals("Contagem", resposta.cidade());
        assertFalse(resposta.ativo());

        verify(clienteRepository).save(cliente);
    }

    @Test
    void deveRetornar409AoAtualizarComEmailDuplicado() {

        ClienteUpdateDTO request = new ClienteUpdateDTO(
                "Pedro Atualizado",
                "outro@email.com",
                "31999999999",
                "Contagem",
                true
        );

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(clienteRepository.existsByEmailIgnoreCaseAndIdNot(
                "outro@email.com",
                1L
        )).thenReturn(true);

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> clienteService.atualizar(1L, request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                erro.getStatusCode()
        );

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveListarClientesComPaginacao() {

        Pageable pageable = PageRequest.of(0, 20);

        Page<ClienteServicoEntity> pagina =
                new PageImpl<>(
                        List.of(cliente),
                        pageable,
                        1
                );

        when(clienteRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(pagina);

        Page<ClienteResponseDTO> resultado =
                clienteService.listar(
                        "Pedro",
                        "Belo Horizonte",
                        pageable
                );

        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        assertEquals(
                "Pedro Pedreiro",
                resultado.getContent().get(0).nome()
        );

        verify(clienteRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }
}
