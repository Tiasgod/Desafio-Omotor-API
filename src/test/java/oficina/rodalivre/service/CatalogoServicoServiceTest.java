package oficina.rodalivre.service;
import oficina.rodalivre.api.dtos.requests.CatalogoServicoRequestDTO;
import oficina.rodalivre.api.dtos.requests.CatalogoServicoUpdateDTO;
import oficina.rodalivre.api.dtos.responses.CatalogoServicoResponseDTO;
import oficina.rodalivre.entity.CatalogoServicoEntity;
import oficina.rodalivre.entity.enums.CategoriaServico;
import oficina.rodalivre.repository.CatalogoServicoRepository;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogoServicoServiceTest {

    @Mock
    private CatalogoServicoRepository servicoRepository;

    @InjectMocks
    private CatalogoServicoService servicoService;

    private CatalogoServicoEntity servico;

    @BeforeEach
    void prepararDados() {

        servico = CatalogoServicoEntity.builder()
                .codigo("FRE-01")
                .nome("Troca de pastilha")
                .categoria(CategoriaServico.FREIOS)
                .precoSugerido(new BigDecimal("70.00"))
                .ativo(true)
                .build();

        ReflectionTestUtils.setField(servico, "id", 1L);
    }

    @Test
    void deveCadastrarServicoValido() {

        CatalogoServicoRequestDTO request =
                new CatalogoServicoRequestDTO(
                        "FRE-01",
                        "Troca de pastilha",
                        CategoriaServico.FREIOS,
                        new BigDecimal("70.00")
                );

        when(servicoRepository.save(any(CatalogoServicoEntity.class)))
                .thenReturn(servico);

        CatalogoServicoResponseDTO resposta =
                servicoService.cadastrar(request);

        assertEquals(1L, resposta.id());
        assertEquals("FRE-01", resposta.codigo());
        assertEquals("Troca de pastilha", resposta.nome());
        assertEquals(CategoriaServico.FREIOS, resposta.categoria());
        assertTrue(resposta.ativo());

        verify(servicoRepository).save(
                any(CatalogoServicoEntity.class)
        );
    }

    @Test
    void deveRemoverEspacosDoCodigo() {

        CatalogoServicoRequestDTO request =
                new CatalogoServicoRequestDTO(
                        "  FRE-01  ",
                        "Troca de pastilha",
                        CategoriaServico.FREIOS,
                        new BigDecimal("70.00")
                );

        when(servicoRepository.save(any(CatalogoServicoEntity.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        CatalogoServicoResponseDTO resposta =
                servicoService.cadastrar(request);

        assertEquals("FRE-01", resposta.codigo());

        verify(servicoRepository).existsByCodigo("FRE-01");
    }

    @Test
    void deveRetornar409QuandoCodigoJaExiste() {

        CatalogoServicoRequestDTO request =
                new CatalogoServicoRequestDTO(
                        "FRE-01",
                        "Outro servico de freios",
                        CategoriaServico.FREIOS,
                        new BigDecimal("90.00")
                );

        when(servicoRepository.existsByCodigo("FRE-01"))
                .thenReturn(true);

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> servicoService.cadastrar(request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                erro.getStatusCode()
        );

        verify(servicoRepository, never()).save(any());
    }

    @Test
    void deveBuscarServicoPorId() {

        when(servicoRepository.findById(1L))
                .thenReturn(Optional.of(servico));

        CatalogoServicoResponseDTO resposta =
                servicoService.buscarPorId(1L);

        assertEquals(1L, resposta.id());
        assertEquals("FRE-01", resposta.codigo());
        assertEquals(CategoriaServico.FREIOS, resposta.categoria());

        verify(servicoRepository).findById(1L);
    }

    @Test
    void deveRetornar404QuandoServicoNaoExiste() {

        when(servicoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> servicoService.buscarPorId(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                erro.getStatusCode()
        );
    }

    @Test
    void deveAtualizarServico() {

        CatalogoServicoUpdateDTO request =
                new CatalogoServicoUpdateDTO(
                        "REV-02",
                        "Revisao completa",
                        CategoriaServico.REVISAO,
                        new BigDecimal("150.00"),
                        false
                );

        when(servicoRepository.findById(1L))
                .thenReturn(Optional.of(servico));

        when(servicoRepository.save(any(CatalogoServicoEntity.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        CatalogoServicoResponseDTO resposta =
                servicoService.atualizar(1L, request);

        assertEquals("REV-02", resposta.codigo());
        assertEquals("Revisao completa", resposta.nome());
        assertEquals(CategoriaServico.REVISAO, resposta.categoria());

        assertEquals(
                0,
                new BigDecimal("150.00")
                        .compareTo(resposta.precoSugerido())
        );

        assertFalse(resposta.ativo());

        verify(servicoRepository).save(servico);
    }

    @Test
    void deveRetornar409AoAtualizarComCodigoDuplicado() {

        CatalogoServicoUpdateDTO request =
                new CatalogoServicoUpdateDTO(
                        "REV-02",
                        "Revisao completa",
                        CategoriaServico.REVISAO,
                        new BigDecimal("150.00"),
                        true
                );

        when(servicoRepository.findById(1L))
                .thenReturn(Optional.of(servico));

        when(servicoRepository.existsByCodigoAndIdNot(
                "REV-02",
                1L
        )).thenReturn(true);

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> servicoService.atualizar(1L, request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                erro.getStatusCode()
        );

        verify(servicoRepository, never()).save(any());
    }

    @Test
    void deveListarServicosComPaginacao() {

        Pageable pageable = PageRequest.of(0, 20);

        Page<CatalogoServicoEntity> pagina =
                new PageImpl<>(
                        List.of(servico),
                        pageable,
                        1
                );

        when(servicoRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(pagina);

        Page<CatalogoServicoResponseDTO> resultado =
                servicoService.listar(
                        "pastilha",
                        CategoriaServico.FREIOS,
                        pageable
                );

        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());

        assertEquals(
                "Troca de pastilha",
                resultado.getContent().get(0).nome()
        );

        verify(servicoRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }
}
