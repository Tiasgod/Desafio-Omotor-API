package oficina.rodalivre.api.controllers;
import oficina.rodalivre.api.dtos.responses.ClienteResponseDTO;
import oficina.rodalivre.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    private ClienteResponseDTO clienteExemplo() {
        return new ClienteResponseDTO(1L, "Pedro Pedreiro", "pedropedreiro@gmail.com", "319887654321", "Belissimo Horizonte", LocalDateTime.of(2026, 3, 1, 10, 15), true);
    }

    @Test
    void deveCadastrarClienteValido() throws Exception {
        when(clienteService.cadastrar(any()))
        .thenReturn(clienteExemplo());

        mockMvc.perform(post("/clientes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
            "nome": "Pedro Pedreiro",
            "email": "pedro@email.com",
            "telefone": "31987654321",
            "cidade": "Belissimo Horizonte"
            }
            """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nome").value("Pedro Pedreiro"))
            .andExpect(jsonPath("$.ativo").value(true))
            .andExpect(jsonPath("$.dataCadastro")
            .value("2026-03-01T10:15:00"));

        verify(clienteService).cadastrar(any());
    }

    @Test
    void deveRetornar400ParaEmailInvalido() throws Exception {
        mockMvc.perform(post("/clientes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
            "nome": "Pedro Pedreiro",
            "email": "email-invalido",
            "telefone": "31987654321",
            "cidade": "Belissimo Horizonte"
            }
            """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
            .value(org.hamcrest.Matchers.containsString("e-mail")));
            verifyNoInteractions(clienteService);
    }

    @Test
    void deveRetornar400ParaTelefoneInvalido() throws Exception {
        mockMvc.perform(post("/clientes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
            "nome": "Pedro Pedreiro",
            "email": "pedro@email.com",
            "telefone": 123,
            "cidade": "Belissimo Horizonte"
            }
            """))
            .andExpect(status().isBadRequest());
            verifyNoInteractions(clienteService);
    }

    @Test
    void deveBuscarClientePorId() throws Exception {
        when(clienteService.buscarPorId(1l))
        .thenReturn(clienteExemplo());

        mockMvc.perform(get("/clientes/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.nome").value("Pedro Pedreiro"));
        verify(clienteService).buscarPorId(1l);
    }

    @Test
    void deveRetornar404ParaClienteInexistente() throws Exception {
        when(clienteService.buscarPorId(99l))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente Não Encontrado"));

        mockMvc.perform(get("/clientes/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
    }

    
@Test
void deveListarClientesComFiltro() throws Exception {

    when(clienteService.listar(
            any(),
            any(),
            any(Pageable.class)
    )).thenReturn(
            new PageImpl<>(List.of(clienteExemplo()))
    );

    mockMvc.perform(get("/clientes")
            .param("nome", "Pedro")
            .param("cidade", "Belissimo Horizonte")
            .param("page", "0")
            .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nome")
                    .value("Pedro Pedreiro"));

    verify(clienteService).listar(
            eq("Pedro"),
            eq("Belissimo Horizonte"),
            any(Pageable.class)
    );
  }
}
