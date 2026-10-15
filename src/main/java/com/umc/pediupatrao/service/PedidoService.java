package com.umc.pediupatrao.service;

import com.umc.pediupatrao.audit.Auditavel;
import com.umc.pediupatrao.entity.AcaoAuditoria;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.util.Paginacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    @Auditavel(tipo = AcaoAuditoria.CRIACAO, acao = "CRIAR_PEDIDO",
            descricao = "Criação de pedido", entidade = "pedidos")
    public Pedido criarPedido(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    public Page<Pedido> listarPaginado(int pagina, int tamanho) {
        return pedidoRepository.findAll(Paginacao.criar(pagina, tamanho, "_id", Sort.Direction.ASC));
    }

    @Auditavel(tipo = AcaoAuditoria.ATUALIZACAO, acao = "ATUALIZAR_STATUS_PEDIDO",
            descricao = "Atualização de status de pedido", entidade = "pedidos", registrarArgumentos = true)
    public Pedido atualizarStatus(String id, String status) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow();
        pedido.setStatus(status);
        return pedidoRepository.save(pedido);
    }

}
