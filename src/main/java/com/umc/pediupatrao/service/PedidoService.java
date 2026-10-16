package com.umc.pediupatrao.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.umc.pediupatrao.audit.Auditavel;
import com.umc.pediupatrao.dto.ItemPedidoRequest;
import com.umc.pediupatrao.dto.PedidoRequest;
import com.umc.pediupatrao.entity.AcaoAuditoria;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.UsuarioRole;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import com.umc.pediupatrao.security.UsuarioSessao;
import com.umc.pediupatrao.util.Paginacao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final String STATUS_INICIAL = "EM_PREPARO";
    private static final String STATUS_CANCELADO = "CANCELADO";
    private static final float PERCENTUAL_TOTAL = 100f;

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    @Auditavel(tipo = AcaoAuditoria.CRIACAO, acao = "CRIAR_PEDIDO",
            descricao = "Criação de pedido", entidade = "pedidos")
    public Pedido criarPedido(PedidoRequest request) {
        exigirPerfilOperacional();
        if (request == null || request.getItens() == null || request.getItens().isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter ao menos um item.");
        }
        if (request.getClienteId() == null || request.getClienteId().isBlank()) {
            throw new IllegalArgumentException("O pedido deve informar o cliente.");
        }

        float descontoPercentual = validarDesconto(request.getDescontoPercentual());

        List<ItemPedido> itens = new ArrayList<>();
        float subtotal = 0f;
        for (ItemPedidoRequest item : request.getItens()) {
            Produto produto = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Produto não encontrado: " + item.getProdutoId()));
            if (item.getQuantidade() <= 0) {
                throw new IllegalArgumentException(
                        "Quantidade inválida para o produto " + produto.getNome() + ".");
            }
            if (Boolean.FALSE.equals(produto.getAtivo())) {
                throw new IllegalArgumentException("Produto indisponível: " + produto.getNome() + ".");
            }
            if (produto.getPreco() == null) {
                throw new IllegalArgumentException("Produto sem preço cadastrado: " + produto.getNome() + ".");
            }

            float precoUnitario = arredondar(produto.getPreco().floatValue());
            float subtotalItem = arredondar(precoUnitario * item.getQuantidade());

            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setProdutoId(produto.getId());
            itemPedido.setNome(produto.getNome());
            itemPedido.setPrecoUnitario(precoUnitario);
            itemPedido.setQuantidade(item.getQuantidade());
            itemPedido.setSubtotal(subtotalItem);
            itens.add(itemPedido);

            subtotal = arredondar(subtotal + subtotalItem);
        }

        float valorDesconto = arredondar(subtotal * descontoPercentual / PERCENTUAL_TOTAL);

        Pedido pedido = new Pedido();
        pedido.setClienteId(request.getClienteId());
        pedido.setItens(itens);
        pedido.setSubtotal(subtotal);
        pedido.setDescontoPercentual(descontoPercentual);
        pedido.setValorDesconto(valorDesconto);
        pedido.setValorTotal(arredondar(subtotal - valorDesconto));
        pedido.setRegistradoPor(UsuarioSessao.username());
        pedido.setDataHora(Instant.now());
        pedido.setStatus(STATUS_INICIAL);
        return pedidoRepository.save(pedido);
    }

    public Page<Pedido> listarPaginado(int pagina, int tamanho) {
        return pedidoRepository.findAll(Paginacao.criar(pagina, tamanho, "_id", Sort.Direction.ASC));
    }

    public Optional<Pedido> buscarPorId(String id) {
        return pedidoRepository.findById(id);
    }

    @Auditavel(tipo = AcaoAuditoria.ATUALIZACAO, acao = "ATUALIZAR_STATUS_PEDIDO",
            descricao = "Atualização de status de pedido", entidade = "pedidos", registrarArgumentos = true)
    public Pedido atualizarStatus(String id, String status) {
        exigirPerfilOperacional();
        if (STATUS_CANCELADO.equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Use a operação de cancelamento para cancelar o pedido.");
        }
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        pedido.setStatus(status);
        return pedidoRepository.save(pedido);
    }

    @Auditavel(tipo = AcaoAuditoria.ATUALIZACAO, acao = "CANCELAR_PEDIDO",
            descricao = "Cancelamento de pedido", entidade = "pedidos", registrarArgumentos = true)
    public Pedido cancelar(String id) {
        if (!UsuarioSessao.possuiPerfil(UsuarioRole.GERENTE)) {
            throw new AccessDeniedException("Apenas o gerente pode cancelar pedidos.");
        }
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        pedido.setStatus(STATUS_CANCELADO);
        return pedidoRepository.save(pedido);
    }

    @Auditavel(tipo = AcaoAuditoria.ATUALIZACAO, acao = "APLICAR_DESCONTO",
            descricao = "Aplicação de desconto em pedido", entidade = "pedidos", registrarArgumentos = true)
    public Pedido aplicarDesconto(String id, float descontoPercentual) {
        if (!UsuarioSessao.possuiPerfil(UsuarioRole.GERENTE)) {
            throw new AccessDeniedException("Apenas o gerente pode aplicar descontos.");
        }
        if (descontoPercentual < 0f || descontoPercentual > PERCENTUAL_TOTAL) {
            throw new IllegalArgumentException("O desconto deve estar entre 0 e 100%.");
        }
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        if (STATUS_CANCELADO.equalsIgnoreCase(pedido.getStatus())) {
            throw new IllegalArgumentException("Não é possível aplicar desconto em pedido cancelado.");
        }
        float valorDesconto = arredondar(pedido.getSubtotal() * descontoPercentual / PERCENTUAL_TOTAL);
        pedido.setDescontoPercentual(descontoPercentual);
        pedido.setValorDesconto(valorDesconto);
        pedido.setValorTotal(arredondar(pedido.getSubtotal() - valorDesconto));
        return pedidoRepository.save(pedido);
    }

    private void exigirPerfilOperacional() {
        if (!UsuarioSessao.possuiPerfil(UsuarioRole.GERENTE)
                && !UsuarioSessao.possuiPerfil(UsuarioRole.ATENDENTE)) {
            throw new AccessDeniedException(
                    "Apenas gerentes e atendentes podem registrar ou alterar pedidos.");
        }
    }

    private float validarDesconto(float descontoPercentual) {
        if (descontoPercentual < 0f || descontoPercentual > PERCENTUAL_TOTAL) {
            throw new IllegalArgumentException("O desconto deve estar entre 0 e 100%.");
        }
        if (descontoPercentual > 0f && !UsuarioSessao.possuiPerfil(UsuarioRole.GERENTE)) {
            throw new AccessDeniedException("Apenas o gerente pode aplicar descontos.");
        }
        return descontoPercentual;
    }

    private float arredondar(float valor) {
        return Math.round(valor * 100f) / 100f;
    }

}
