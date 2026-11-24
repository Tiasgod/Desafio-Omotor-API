package oficina.rodalivre.service;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import oficina.rodalivre.api.dtos.requests.CatalogoServicoRequestDTO;
import oficina.rodalivre.api.dtos.requests.CatalogoServicoUpdateDTO;
import oficina.rodalivre.api.dtos.responses.CatalogoServicoResponseDTO;
import oficina.rodalivre.entity.CatalogoServicoEntity;
import oficina.rodalivre.entity.enums.CategoriaServico;
import oficina.rodalivre.repository.CatalogoServicoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;

@Builder
@Service
@RequiredArgsConstructor
public class CatalogoServicoService {

    private final CatalogoServicoRepository servicoRepository;

    @Transactional
    public CatalogoServicoResponseDTO cadastrar(
            CatalogoServicoRequestDTO request
    ) {

        String codigo = request.codigo().trim();

        if (servicoRepository.existsByCodigo(codigo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Já existe um serviço com este código");
        }

        CatalogoServicoEntity servico = CatalogoServicoEntity.builder()
                .codigo(codigo)
                .nome(request.nome())
                .categoria(request.categoria())
                .precoSugerido(request.precoSugerido())
                .ativo(true)
                .build();

        return converterParaDTO(servicoRepository.save(servico));
    }

    @Transactional(readOnly = true)
    public Page<CatalogoServicoResponseDTO> listar(String nome,CategoriaServico categoria,Pageable pageable) {

        Specification<CatalogoServicoEntity> spec =
                (root, query, cb) -> cb.conjunction();

        if (nome != null && !nome.isBlank()) {

            String filtroNome =
                    "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";

            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nome")), filtroNome));
        }

        if (categoria != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("categoria"), categoria));
        }

        return servicoRepository.findAll(spec, pageable)
                .map(this::converterParaDTO);
    }

    @Transactional(readOnly = true)
    public CatalogoServicoResponseDTO buscarPorId(Long id) {
        return converterParaDTO(buscarEntidade(id));
    }

    @Transactional
    public CatalogoServicoResponseDTO atualizar(Long id,CatalogoServicoUpdateDTO request) {
        CatalogoServicoEntity servico = buscarEntidade(id);
        String codigo = request.codigo().trim();
        if (servicoRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Este código já pertence a outro serviço");
        }
        servico.setCodigo(codigo);
        servico.setNome(request.nome().trim());
        servico.setCategoria(request.categoria());
        servico.setPrecoSugerido(request.precoSugerido());
        servico.setAtivo(request.ativo());
        return converterParaDTO(servicoRepository.save(servico));
    }

    private CatalogoServicoEntity buscarEntidade(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Serviço não encontrado: " + id));
    }

    private CatalogoServicoResponseDTO converterParaDTO(CatalogoServicoEntity servico) {
        return new CatalogoServicoResponseDTO(
                servico.getId(),
                servico.getCodigo(),
                servico.getNome(),
                servico.getCategoria(),
                servico.getPrecoSugerido(),
                servico.getAtivo()
        );
    }
}
