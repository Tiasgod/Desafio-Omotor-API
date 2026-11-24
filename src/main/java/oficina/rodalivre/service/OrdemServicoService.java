package oficina.rodalivre.service;
import lombok.RequiredArgsConstructor;
import oficina.rodalivre.api.dtos.requests.OrdemServicoRequestDTO;
import oficina.rodalivre.api.dtos.requests.OrdemServicoStatusDTO;
import oficina.rodalivre.api.dtos.responses.OrdemServicoResponseDTO;
import oficina.rodalivre.entity.CatalogoServicoEntity;
import oficina.rodalivre.entity.ClienteServicoEntity;
import oficina.rodalivre.entity.OrdemServicoEntity;
import oficina.rodalivre.entity.enums.StatusOrdem;
import oficina.rodalivre.repository.CatalogoServicoRepository;
import oficina.rodalivre.repository.ClienteServicoRepository;
import oficina.rodalivre.repository.OrdemServicoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository ordemRepository;
    private final ClienteServicoRepository clienteRepository;
    private final CatalogoServicoRepository servicoRepository;

    @Transactional
    public OrdemServicoResponseDTO cadastrar(OrdemServicoRequestDTO request) {

        ClienteServicoEntity cliente = clienteRepository
                .buscarPorIdComBloqueio(request.clienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Cliente não encontrado: " + request.clienteId()));

        if (!Boolean.TRUE.equals(cliente.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Cliente inativo não pode abrir ordens de serviço");
        }

        CatalogoServicoEntity servico = servicoRepository
                .findById(request.servicoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Serviço não encontrado: " + request.servicoId()));

        if (!Boolean.TRUE.equals(servico.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Serviço inativo não pode ser contratado");
        }

        boolean possuiOrdemAberta =
                ordemRepository.existsByCliente_IdAndStatus(cliente.getId(),StatusOrdem.ABERTA);

        if (possuiOrdemAberta) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"O cliente já possui uma ordem de serviço aberta");
        }

        if (request.valor().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"O valor da ordem deve ser maior que zero");
        }

        OrdemServicoEntity ordem = OrdemServicoEntity.builder()
                .cliente(cliente)
                .servico(servico)
                .dataEntrada(request.dataEntrada())
                .modeloBicicleta(request.modeloBicicleta())
                .descricao(request.descricao().trim())
                .valor(request.valor())
                .status(StatusOrdem.ABERTA)
                .build();

        return converterParaDTO(ordemRepository.save(ordem));
    }

    @Transactional(readOnly = true)
    public Page<OrdemServicoResponseDTO> listar(StatusOrdem status,Long clienteId,Long servicoId,LocalDateTime dataInicio,LocalDateTime dataFim,Pageable pageable) {

        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A data inicial não pode ser posterior à data final"
            );
        }

        Specification<OrdemServicoEntity> spec =
                (root, query, cb) -> cb.conjunction();

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        if (clienteId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("cliente").get("id"), clienteId));
        }

        if (servicoId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("servico").get("id"), servicoId));
        }

        if (dataInicio != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("dataEntrada"),dataInicio));
        }

        if (dataFim != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("dataEntrada"),dataFim));
        }

        return ordemRepository.findAll(spec, pageable)
                .map(this::converterParaDTO);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponseDTO buscarPorId(Long id) {

        OrdemServicoEntity ordem = ordemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Ordem de serviço não encontrada: " + id));
        return converterParaDTO(ordem);
    }

    @Transactional
    public OrdemServicoResponseDTO atualizarStatus(Long id,OrdemServicoStatusDTO request) {

        OrdemServicoEntity ordem = ordemRepository
                .buscarPorIdComBloqueio(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Ordem de serviço não encontrada: " + id));
        StatusOrdem statusAtual = ordem.getStatus();
        StatusOrdem novoStatus = request.status();

        if (statusAtual == StatusOrdem.CONCLUIDA || statusAtual == StatusOrdem.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Ordens concluídas ou canceladas não podem mudar de status");
        }

        if (novoStatus != StatusOrdem.CONCLUIDA && novoStatus != StatusOrdem.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Transição inválida: uma ordem aberta só pode ser concluída ou cancelada");
        }

        ordem.setStatus(novoStatus);
        return converterParaDTO(ordemRepository.save(ordem));
    }

    private OrdemServicoResponseDTO converterParaDTO(
            OrdemServicoEntity ordem
    ) {

        return new OrdemServicoResponseDTO(ordem.getId(),ordem.getCliente().getId(),ordem.getCliente().getNome(),ordem.getServico().getId(),ordem.getServico().getNome(),ordem.getDataEntrada(),ordem.getModeloBicicleta(),ordem.getDescricao(),ordem.getValor(),ordem.getStatus());
    }
}
