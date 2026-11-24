package oficina.rodalivre.service;
import lombok.RequiredArgsConstructor;
import oficina.rodalivre.api.dtos.requests.ClienteRequestDTO;
import oficina.rodalivre.api.dtos.requests.ClienteUpdateDTO;
import oficina.rodalivre.api.dtos.responses.ClienteResponseDTO;
import oficina.rodalivre.entity.ClienteServicoEntity;
import oficina.rodalivre.repository.ClienteServicoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteServicoRepository clienteRepository;

    @Transactional
    public ClienteResponseDTO cadastrar(ClienteRequestDTO request) {

        String email = normalizarEmail(request.email());

        if (clienteRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Já existe um cliente cadastrado com este e-mail");
        }

        ClienteServicoEntity cliente = ClienteServicoEntity.builder()
                .nome(request.nome())
                .email(email)
                .telefone(request.telefone())
                .cidade(request.cidade())
                .ativo(true)
                .build();

        return converterParaDTO(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listar(String nome,String cidade,Pageable pageable) {

        Specification<ClienteServicoEntity> spec =
                (root, query, cb) -> cb.conjunction();

        if (nome != null && !nome.isBlank()) {

            String filtroNome =
                    "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";

            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nome")), filtroNome));
        }

        if (cidade != null && !cidade.isBlank()) {

            String filtroCidade =
                    cidade.trim().toLowerCase(Locale.ROOT);

            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("cidade")), filtroCidade));
        }

        return clienteRepository.findAll(spec, pageable)
                .map(this::converterParaDTO);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        return converterParaDTO(buscarEntidade(id));
    }

    @Transactional
    public ClienteResponseDTO atualizar(Long id,ClienteUpdateDTO request) {

        ClienteServicoEntity cliente = buscarEntidade(id);

        String email = normalizarEmail(request.email());

        if (clienteRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {

            throw new ResponseStatusException(HttpStatus.CONFLICT,"Este e-mail já pertence a outro cliente");
        }

        cliente.setNome(request.nome().trim());
        cliente.setEmail(email);
        cliente.setTelefone(request.telefone());
        cliente.setCidade(request.cidade().trim());
        cliente.setAtivo(request.ativo());
        return converterParaDTO(clienteRepository.save(cliente));
    }

    private ClienteServicoEntity buscarEntidade(Long id) {

        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Cliente não encontrado: " + id));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private ClienteResponseDTO converterParaDTO(
            ClienteServicoEntity cliente
    ) {

        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getCidade(),
                cliente.getDataCadastro(),
                cliente.getAtivo()
        );
    }
}
