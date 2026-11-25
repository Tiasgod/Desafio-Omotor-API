package oficina.rodalivre.api.controllers;
import oficina.rodalivre.api.dtos.responses.CatalogoServicoResponseDTO;
import oficina.rodalivre.entity.enums.CategoriaServico;
import oficina.rodalivre.service.CatalogoServicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CatalogoServicoController.class)
class CatalogoServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogoServicoService servicoService;

    private CatalogoServicoResponseDTO servicoExemplo() {
        return new CatalogoServicoResponseDTO(
                1L,
                "FRE-01",
                "Troca de pastilha",
                CategoriaServico.FREIOS,
                new BigDecimal("70.00"),
                true
        );
    }

    @Test
    void deveCadastrarServicoValido() throws Exception {

        when(servicoService.cadastrar(any()))
                .thenReturn(servicoExemplo());

        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "codigo": "FRE-01",
                      "nome": "Troca de pastilha",
                      "categoria": "FREIOS",
                      "precoSugerido": 70.00
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("FRE-01"))
                .andExpect(jsonPath("$.categoria").value("FREIOS"));

        verify(servicoService).cadastrar(any());
    }

    @Test
    void deveRetornar400ParaPrecoZero() throws Exception {

        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "codigo": "FRE-01",
                      "nome": "Troca de pastilha",
                      "categoria": "FREIOS",
                      "precoSugerido": 0.00
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servicoService);
    }

    @Test
    void deveRetornar400ParaCategoriaInvalida() throws Exception {

        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "codigo": "FRE-01",
                      "nome": "Troca de pastilha",
                      "categoria": "ELETRONICOS",
                      "precoSugerido": 70.00
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servicoService);
    }

    @Test
    void deveBuscarServicoPorId() throws Exception {

        when(servicoService.buscarPorId(1L))
                .thenReturn(servicoExemplo());

        mockMvc.perform(get("/servicos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Troca de pastilha"));
    }

    @Test
    void deveRetornar404ParaServicoInexistente() throws Exception {

        when(servicoService.buscarPorId(99L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Serviço não encontrado"
                ));

        mockMvc.perform(get("/servicos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deveRetornar409ParaCodigoDuplicado() throws Exception {

        when(servicoService.cadastrar(any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Já existe um serviço com este código"
                ));

        mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "codigo": "FRE-01",
                      "nome": "Troca de pastilha",
                      "categoria": "FREIOS",
                      "precoSugerido": 70.00
                    }
                    """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deveListarServicosPorCategoria() throws Exception {

        when(servicoService.listar(
                isNull(),
                eq(CategoriaServico.FREIOS),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(servicoExemplo())));

        mockMvc.perform(get("/servicos")
                .param("categoria", "FREIOS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].categoria")
                        .value("FREIOS"));

        verify(servicoService).listar(
                isNull(),
                eq(CategoriaServico.FREIOS),
                any(Pageable.class)
        );
    }
}
