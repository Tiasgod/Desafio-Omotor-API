package oficina.rodalivre.service;
import oficina.rodalivre.api.dtos.requests.OrdemServicoRequestDTO;
import oficina.rodalivre.api.dtos.requests.OrdemServicoStatusDTO;
import oficina.rodalivre.api.dtos.responses.OrdemServicoResponseDTO;
import oficina.rodalivre.entity.CatalogoServicoEntity;
import oficina.rodalivre.entity.ClienteServicoEntity;
import oficina.rodalivre.entity.OrdemServicoEntity;
import oficina.rodalivre.entity.enums.CategoriaServico;
import oficina.rodalivre.entity.enums.StatusOrdem;
import oficina.rodalivre.repository.CatalogoServicoRepository;
import oficina.rodalivre.repository.ClienteServicoRepository;
import oficina.rodalivre.repository.OrdemServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @Mock
    private OrdemServicoRepository ordemRepository;

    @Mock
    private ClienteServicoRepository clienteRepository;

    @Mock
    private CatalogoServicoRepository servicoRepository;

    @InjectMocks
    private OrdemServicoService service;

    private ClienteServicoEntity cliente;
    private CatalogoServicoEntity servico;
    private OrdemServicoRequestDTO pedido;

    private final LocalDateTime dataEntrada =
            LocalDateTime.of(2026, 3, 10, 9, 0);

    @BeforeEach
    void prepararDados() {

        cliente = ClienteServicoEntity.builder()
                .nome("Cliente Exemplo")
                .email("exemplo@teste.com")
                .telefone("11987654321")
                .cidade("Sao Paulo")
                .ativo(true)
                .build();

        ReflectionTestUtils.setField(cliente, "id", 1L);

        servico = CatalogoServicoEntity.builder()
                .codigo("REV001")
                .nome("Revisao completa")
                .categoria(CategoriaServico.REVISAO)
                .precoSugerido(new BigDecimal("100.00"))
                .ativo(true)
                .build();

        ReflectionTestUtils.setField(servico, "id", 2L);

        pedido = novoPedido(new BigDecimal("80.00"));
    }

    private OrdemServicoRequestDTO novoPedido(BigDecimal valor) {

        return new OrdemServicoRequestDTO(
                1L,
                2L,
                dataEntrada,
                "Mountain bike aro 29",
                "Freio dianteiro com ruido",
                valor
        );
    }

    private void prepararCadastroValido() {

        when(clienteRepository.buscarPorIdComBloqueio(1L))
                .thenReturn(Optional.of(cliente));

        when(servicoRepository.findById(2L))
                .thenReturn(Optional.of(servico));
    }

    private OrdemServicoEntity ordemExistente(StatusOrdem status) {

        OrdemServicoEntity ordem = OrdemServicoEntity.builder()
                .cliente(cliente)
                .servico(servico)
                .dataEntrada(dataEntrada)
                .modeloBicicleta("Mountain bike aro 29")
                .descricao("Freio dianteiro com ruido")
                .valor(new BigDecimal("80.00"))
                .status(status)
                .build();

        ReflectionTestUtils.setField(ordem, "id", 10L);

        return ordem;
    }

    private void verificarHttp(
            ResponseStatusException excecao,
            HttpStatus esperado) {

        assertEquals(esperado, excecao.getStatusCode());
    }

    @Test
    void deveCadastrarOrdemAbertaComDataInformada() {

        prepararCadastroValido();

        when(ordemRepository.save(any(OrdemServicoEntity.class)))
                .thenAnswer(invocacao -> {

                    OrdemServicoEntity ordem =
                            invocacao.getArgument(0);

                    ReflectionTestUtils.setField(
                            ordem, "id", 10L
                    );

                    return ordem;
                });

        OrdemServicoResponseDTO resposta =
                service.cadastrar(pedido);

        assertEquals(10L, resposta.id());
        assertEquals(1L, resposta.clienteId());
        assertEquals(2L, resposta.servicoId());
        assertEquals(StatusOrdem.ABERTA, resposta.status());
        assertEquals(dataEntrada, resposta.dataEntrada());

        assertEquals(
                0,
                new BigDecimal("80.00").compareTo(resposta.valor())
        );

        verify(ordemRepository).save(argThat(ordem ->
                ordem.getStatus() == StatusOrdem.ABERTA
                        && dataEntrada.equals(ordem.getDataEntrada())
        ));
    }

    @Test
    void deveRetornar404QuandoClienteNaoExiste() {

        when(clienteRepository.buscarPorIdComBloqueio(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrar(pedido)
        );

        verificarHttp(erro, HttpStatus.NOT_FOUND);

        verify(ordemRepository, never()).save(any());
    }

    @Test
    void deveRetornar409QuandoClienteEstaInativo() {

        cliente.setAtivo(false);

        when(clienteRepository.buscarPorIdComBloqueio(1L))
                .thenReturn(Optional.of(cliente));

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrar(pedido)
        );

        verificarHttp(erro, HttpStatus.CONFLICT);

        verify(ordemRepository, never()).save(any());
    }

    @Test
    void deveRetornar404QuandoServicoNaoExiste() {

        when(clienteRepository.buscarPorIdComBloqueio(1L))
                .thenReturn(Optional.of(cliente));

        when(servicoRepository.findById(2L))
                .thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrar(pedido)
        );

        verificarHttp(erro, HttpStatus.NOT_FOUND);

        verify(ordemRepository, never()).save(any());
    }

    @Test
    void deveRetornar409QuandoServicoEstaInativo() {

        servico.setAtivo(false);

        prepararCadastroValido();

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrar(pedido)
        );

        verificarHttp(erro, HttpStatus.CONFLICT);

        verify(ordemRepository, never()).save(any());
    }

    @Test
    void deveRetornar409QuandoClienteJaPossuiOrdemAberta() {

        prepararCadastroValido();

        when(ordemRepository.existsByCliente_IdAndStatus(
                1L, StatusOrdem.ABERTA
        )).thenReturn(true);

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrar(pedido)
        );

        verificarHttp(erro, HttpStatus.CONFLICT);

        verify(ordemRepository, never()).save(any());
    }

    @Test
    void deveRetornar400QuandoValorForZero() {

        prepararCadastroValido();

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrar(
                        novoPedido(BigDecimal.ZERO)
                )
        );

        verificarHttp(erro, HttpStatus.BAD_REQUEST);

        verify(ordemRepository, never()).save(any());
    }

    @Test
    void deveRetornar400QuandoValorForNegativo() {

        prepararCadastroValido();

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrar(
                        novoPedido(new BigDecimal("-20.00"))
                )
        );

        verificarHttp(erro, HttpStatus.BAD_REQUEST);

        verify(ordemRepository, never()).save(any());
    }

    @Test
    void deveBuscarOrdemExistente() {

        when(ordemRepository.findById(10L))
                .thenReturn(Optional.of(
                        ordemExistente(StatusOrdem.ABERTA)
                ));

        OrdemServicoResponseDTO resposta =
                service.buscarPorId(10L);

        assertEquals(10L, resposta.id());
        assertEquals(StatusOrdem.ABERTA, resposta.status());

        verify(ordemRepository).findById(10L);
    }

    @Test
    void deveRetornar404AoBuscarOrdemInexistente() {

        when(ordemRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(99L)
        );

        verificarHttp(erro, HttpStatus.NOT_FOUND);
    }

    @Test
    void deveConcluirOrdemAberta() {

        OrdemServicoEntity ordem =
                ordemExistente(StatusOrdem.ABERTA);

        when(ordemRepository.buscarPorIdComBloqueio(10L))
                .thenReturn(Optional.of(ordem));

        when(ordemRepository.save(ordem))
                .thenReturn(ordem);

        OrdemServicoResponseDTO resposta =
                service.atualizarStatus(
                        10L,
                        new OrdemServicoStatusDTO(StatusOrdem.CONCLUIDA)
                );

        assertEquals(StatusOrdem.CONCLUIDA, resposta.status());
        assertEquals(dataEntrada, resposta.dataEntrada());

        verify(ordemRepository).save(ordem);
    }

    @Test
    void deveCancelarOrdemAberta() {

        OrdemServicoEntity ordem =
                ordemExistente(StatusOrdem.ABERTA);

        when(ordemRepository.buscarPorIdComBloqueio(10L))
                .thenReturn(Optional.of(ordem));

        when(ordemRepository.save(ordem))
                .thenReturn(ordem);

        OrdemServicoResponseDTO resposta =
                service.atualizarStatus(
                        10L,
                        new OrdemServicoStatusDTO(StatusOrdem.CANCELADA)
                );

        assertEquals(StatusOrdem.CANCELADA, resposta.status());

        verify(ordemRepository).save(ordem);
    }

    @Test
    void deveRetornar409AoAlterarOrdemConcluida() {

        when(ordemRepository.buscarPorIdComBloqueio(10L))
                .thenReturn(Optional.of(
                        ordemExistente(StatusOrdem.CONCLUIDA)
                ));

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizarStatus(
                        10L,
                        new OrdemServicoStatusDTO(StatusOrdem.CANCELADA)
                )
        );

        verificarHttp(erro, HttpStatus.CONFLICT);

        verify(ordemRepository, never()).save(any());
    }

    @Test
    void deveRetornar409AoAlterarOrdemCancelada() {

        when(ordemRepository.buscarPorIdComBloqueio(10L))
                .thenReturn(Optional.of(
                        ordemExistente(StatusOrdem.CANCELADA)
                ));

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizarStatus(
                        10L,
                        new OrdemServicoStatusDTO(StatusOrdem.CONCLUIDA)
                )
        );

        verificarHttp(erro, HttpStatus.CONFLICT);

        verify(ordemRepository, never()).save(any());
    }

    @Test
    void deveRetornar409QuandoTentaManterStatusAberta() {

        when(ordemRepository.buscarPorIdComBloqueio(10L))
                .thenReturn(Optional.of(
                        ordemExistente(StatusOrdem.ABERTA)
                ));

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizarStatus(
                        10L,
                        new OrdemServicoStatusDTO(StatusOrdem.ABERTA)
                )
        );

        verificarHttp(erro, HttpStatus.CONFLICT);

        verify(ordemRepository, never()).save(any());
    }

    @Test
    void deveRetornar404AoAtualizarOrdemInexistente() {

        when(ordemRepository.buscarPorIdComBloqueio(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizarStatus(
                        99L,
                        new OrdemServicoStatusDTO(StatusOrdem.CONCLUIDA)
                )
        );

        verificarHttp(erro, HttpStatus.NOT_FOUND);

        verify(ordemRepository, never()).save(any());
    }

    @Test
    void deveRetornar400QuandoPeriodoEstaInvertido() {

        LocalDateTime inicio =
                LocalDateTime.of(2026, 4, 20, 10, 0);

        LocalDateTime fim =
                LocalDateTime.of(2026, 4, 10, 10, 0);

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.listar(
                        null,
                        null,
                        null,
                        inicio,
                        fim,
                        PageRequest.of(0, 20)
                )
        );

        verificarHttp(erro, HttpStatus.BAD_REQUEST);

        verifyNoInteractions(ordemRepository);
    }
}
