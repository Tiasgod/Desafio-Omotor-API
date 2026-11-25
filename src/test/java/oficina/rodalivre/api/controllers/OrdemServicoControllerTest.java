package oficina.rodalivre.api.controllers;
import oficina.rodalivre.api.dtos.responses.OrdemServicoResponseDTO;
import oficina.rodalivre.entity.enums.StatusOrdem;
import oficina.rodalivre.service.OrdemServicoService;
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
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrdemServicoController.class)
class OrdemServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdemServicoService ordemService;

    private OrdemServicoResponseDTO ordemExemplo(StatusOrdem status) {

        return new OrdemServicoResponseDTO(
                1L,
                1L,
                "Pedro Almeida",
                2L,
                "Troca de pastilha",
                LocalDateTime.of(2026, 3, 10, 9, 0),
                "Mountain bike aro 29",
                "Freio dianteiro chiando na descida",
                new BigDecimal("80.00"),
                status
        );
    }

    @Test
    void deveCriarOrdemComDataISO8601() throws Exception {

        when(ordemService.cadastrar(any()))
                .thenReturn(ordemExemplo(StatusOrdem.ABERTA));

        mockMvc.perform(post("/ordens-servico")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "clienteId": 1,
                      "servicoId": 2,
                      "dataEntrada": "2026-03-10T09:00:00",
                      "modeloBicicleta": "Mountain bike aro 29",
                      "descricao": "Freio dianteiro chiando na descida",
                      "valor": 80.00
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.dataEntrada")
                        .value("2026-03-10T09:00:00"));

        verify(ordemService).cadastrar(argThat(request ->
                LocalDateTime.of(2026, 3, 10, 9, 0)
                        .equals(request.dataEntrada())
        ));
    }

    @Test
    void deveRetornar400SemDataEntrada() throws Exception {

        mockMvc.perform(post("/ordens-servico")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "clienteId": 1,
                      "servicoId": 2,
                      "modeloBicicleta": "Mountain bike aro 29",
                      "descricao": "Freio dianteiro chiando na descida",
                      "valor": 80.00
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordemService);
    }

    @Test
    void deveRetornar400ComDataInvalida() throws Exception {

        mockMvc.perform(post("/ordens-servico")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "clienteId": 1,
                      "servicoId": 2,
                      "dataEntrada": "10/03/2026",
                      "modeloBicicleta": "Mountain bike aro 29",
                      "descricao": "Freio dianteiro chiando na descida",
                      "valor": 80.00
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordemService);
    }

    @Test
    void deveRetornar400ComValorNegativo() throws Exception {

        mockMvc.perform(post("/ordens-servico")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "clienteId": 1,
                      "servicoId": 2,
                      "dataEntrada": "2026-03-10T09:00:00",
                      "modeloBicicleta": "Mountain bike aro 29",
                      "descricao": "Freio dianteiro chiando na descida",
                      "valor": -30.00
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordemService);
    }

    @Test
    void deveBuscarOrdemPorId() throws Exception {

        when(ordemService.buscarPorId(1L))
                .thenReturn(ordemExemplo(StatusOrdem.ABERTA));

        mockMvc.perform(get("/ordens-servico/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteNome")
                        .value("Pedro Almeida"))
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    void deveConcluirOrdem() throws Exception {

        when(ordemService.atualizarStatus(eq(1L), any()))
                .thenReturn(ordemExemplo(StatusOrdem.CONCLUIDA));

        mockMvc.perform(patch("/ordens-servico/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "CONCLUIDA"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("CONCLUIDA"));

        verify(ordemService).atualizarStatus(eq(1L), argThat(
                request -> request.status() == StatusOrdem.CONCLUIDA
        ));
    }

    @Test
    void deveRetornar409ParaOrdemFinalizada() throws Exception {

        when(ordemService.atualizarStatus(eq(1L), any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ordens concluídas não podem mudar de status"
                ));

        mockMvc.perform(patch("/ordens-servico/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "CANCELADA"
                    }
                    """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deveListarOrdensAbertas() throws Exception {

        when(ordemService.listar(
                eq(StatusOrdem.ABERTA),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(
                List.of(ordemExemplo(StatusOrdem.ABERTA))
        ));

        mockMvc.perform(get("/ordens-servico")
                .param("status", "ABERTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status")
                        .value("ABERTA"));
    }
}
