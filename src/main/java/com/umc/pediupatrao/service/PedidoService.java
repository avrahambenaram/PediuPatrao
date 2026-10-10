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
import com.umc.pediupatrao.audit.ContextoAuditoria;
import com.umc.pediupatrao.dto.ItemPedidoRequest;
import com.umc.pediupatrao.dto.PedidoRequest;
import com.umc.pediupatrao.entity.AcaoAuditoria;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.PedidoStatus;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.UsuarioRole;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import com.umc.pediupatrao.security.UsuarioSessao;
import com.umc.pediupatrao.util.Paginacao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final String STATUS_INICIAL = PedidoStatus.RECEBIDO.name();
    private static final float PERCENTUAL_TOTAL = 100f;
    private static final float PERCENTUAL_MAXIMO_DESCONTO = 20f;

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;

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
        if (!clienteRepository.existsById(request.getClienteId())) {
            throw new IllegalArgumentException("Cliente não encontrado.");
        }

        float descontoPercentual = validarDesconto(request.getDescontoPercentual());

        List<ItemPedido> itens = new ArrayList<>();
        float subtotal = 0f;
        for (ItemPedidoRequest item : request.getItens()) {
            if (item == null || item.getProdutoId() == null || item.getProdutoId().isBlank()) {
                throw new IllegalArgumentException("Item do pedido inválido: o produto deve ser informado.");
            }
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
        Pedido salvo = pedidoRepository.save(pedido);
        ContextoAuditoria.registrarEstadoPosterior(descrever(salvo));
        return salvo;
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
        PedidoStatus destino = PedidoStatus.from(status);
        if (destino == PedidoStatus.CANCELADO) {
            throw new IllegalArgumentException("Use a operação de cancelamento para cancelar o pedido.");
        }

        Pedido pedido = buscar(id);
        PedidoStatus atual = PedidoStatus.from(pedido.getStatus());
        if (atual.isFinal()) {
            throw new IllegalArgumentException(
                    "Não é possível alterar o status de um pedido já " + atual.getDescricao().toLowerCase() + ".");
        }
        if (!atual.podeTransicionarPara(destino)) {
            throw new IllegalArgumentException("Transição inválida: de \"" + atual.getDescricao()
                    + "\" para \"" + destino.getDescricao() + "\".");
        }
        ContextoAuditoria.registrarEstadoAnterior(descrever(pedido));
        if (destino.exigeSaida()) {
            pedido.setSaidaPor(UsuarioSessao.username());
            pedido.setDataHoraSaida(Instant.now());
        }
        pedido.setStatus(destino.name());
        Pedido salvo = pedidoRepository.save(pedido);
        ContextoAuditoria.registrarEstadoPosterior(descrever(salvo));
        return salvo;
    }

    @Auditavel(tipo = AcaoAuditoria.ATUALIZACAO, acao = "CANCELAR_PEDIDO",
            descricao = "Cancelamento de pedido", entidade = "pedidos", registrarArgumentos = true)
    public Pedido cancelar(String id, String justificativa) {
        if (!UsuarioSessao.possuiPerfil(UsuarioRole.GERENTE)) {
            throw new AccessDeniedException("Apenas o gerente pode cancelar pedidos.");
        }
        if (justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("O cancelamento exige justificativa.");
        }

        Pedido pedido = buscar(id);
        PedidoStatus atual = PedidoStatus.from(pedido.getStatus());
        if (atual.isFinal()) {
            throw new IllegalArgumentException(
                    "Não é possível cancelar um pedido já " + atual.getDescricao().toLowerCase() + ".");
        }
        if (!atual.podeCancelar()) {
            throw new IllegalArgumentException(
                    "O pedido não pode ser cancelado após sair para entrega ou retirada.");
        }
        ContextoAuditoria.registrarEstadoAnterior(descrever(pedido));
        pedido.setStatus(PedidoStatus.CANCELADO.name());
        pedido.setCanceladoPor(UsuarioSessao.username());
        pedido.setDataHoraCancelamento(Instant.now());
        pedido.setJustificativaCancelamento(justificativa.trim());
        Pedido salvo = pedidoRepository.save(pedido);
        ContextoAuditoria.registrarEstadoPosterior(descrever(salvo));
        return salvo;
    }

    @Auditavel(tipo = AcaoAuditoria.ATUALIZACAO, acao = "APLICAR_DESCONTO",
            descricao = "Aplicação de desconto em pedido", entidade = "pedidos", registrarArgumentos = true)
    public Pedido aplicarDesconto(String id, float descontoPercentual) {
        if (!UsuarioSessao.possuiPerfil(UsuarioRole.GERENTE)) {
            throw new AccessDeniedException("Apenas o gerente pode aplicar descontos.");
        }
        if (descontoPercentual < 0f || descontoPercentual > PERCENTUAL_MAXIMO_DESCONTO) {
            throw new IllegalArgumentException(
                    "O desconto deve estar entre 0 e " + (int) PERCENTUAL_MAXIMO_DESCONTO + "%.");
        }
        Pedido pedido = buscar(id);
        PedidoStatus atual = PedidoStatus.from(pedido.getStatus());
        if (!atual.permiteDesconto()) {
            throw new IllegalArgumentException(
                    "O desconto só pode ser aplicado antes da saída para entrega ou retirada.");
        }
        ContextoAuditoria.registrarEstadoAnterior(descrever(pedido));
        float valorDesconto = arredondar(pedido.getSubtotal() * descontoPercentual / PERCENTUAL_TOTAL);
        pedido.setDescontoPercentual(descontoPercentual);
        pedido.setValorDesconto(valorDesconto);
        pedido.setValorTotal(arredondar(pedido.getSubtotal() - valorDesconto));
        Pedido salvo = pedidoRepository.save(pedido);
        ContextoAuditoria.registrarEstadoPosterior(descrever(salvo));
        return salvo;
    }

    private Pedido buscar(String id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
    }

    private void exigirPerfilOperacional() {
        if (!UsuarioSessao.possuiPerfil(UsuarioRole.GERENTE)
                && !UsuarioSessao.possuiPerfil(UsuarioRole.ATENDENTE)) {
            throw new AccessDeniedException(
                    "Apenas gerentes e atendentes podem registrar ou alterar pedidos.");
        }
    }

    private float validarDesconto(float descontoPercentual) {
        if (descontoPercentual < 0f || descontoPercentual > PERCENTUAL_MAXIMO_DESCONTO) {
            throw new IllegalArgumentException(
                    "O desconto deve estar entre 0 e " + (int) PERCENTUAL_MAXIMO_DESCONTO + "%.");
        }
        if (descontoPercentual > 0f && !UsuarioSessao.possuiPerfil(UsuarioRole.GERENTE)) {
            throw new AccessDeniedException("Apenas o gerente pode aplicar descontos.");
        }
        return descontoPercentual;
    }

    private String descrever(Pedido pedido) {
        return "status=" + pedido.getStatus()
                + ", descontoPercentual=" + pedido.getDescontoPercentual()
                + ", valorDesconto=" + pedido.getValorDesconto()
                + ", valorTotal=" + pedido.getValorTotal()
                + ", saidaPor=" + pedido.getSaidaPor()
                + ", dataHoraSaida=" + pedido.getDataHoraSaida()
                + ", canceladoPor=" + pedido.getCanceladoPor()
                + ", dataHoraCancelamento=" + pedido.getDataHoraCancelamento()
                + ", justificativaCancelamento=" + pedido.getJustificativaCancelamento();
    }

    private float arredondar(float valor) {
        return Math.round(valor * 100f) / 100f;
    }

}
