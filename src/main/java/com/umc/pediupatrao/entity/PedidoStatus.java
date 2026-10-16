package com.umc.pediupatrao.entity;

import java.util.List;
import java.util.Map;

public enum PedidoStatus {

    RECEBIDO("Recebido"),
    EM_PREPARACAO("Em preparação"),
    PRONTO("Pronto"),
    SAIU_PARA_ENTREGA("Saiu para entrega"),
    RETIRADO("Retirado"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");

    private static final Map<PedidoStatus, List<PedidoStatus>> FLUXO = Map.of(
            RECEBIDO, List.of(EM_PREPARACAO),
            EM_PREPARACAO, List.of(PRONTO),
            PRONTO, List.of(SAIU_PARA_ENTREGA, RETIRADO),
            SAIU_PARA_ENTREGA, List.of(FINALIZADO),
            RETIRADO, List.of(FINALIZADO),
            FINALIZADO, List.of(),
            CANCELADO, List.of());

    private static final List<PedidoStatus> CANCELAVEIS = List.of(RECEBIDO, EM_PREPARACAO, PRONTO);

    private final String descricao;

    PedidoStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public List<PedidoStatus> getProximos() {
        return FLUXO.get(this);
    }

    public boolean podeTransicionarPara(PedidoStatus destino) {
        return destino != null && getProximos().contains(destino);
    }

    public boolean podeCancelar() {
        return CANCELAVEIS.contains(this);
    }

    public boolean isFinal() {
        return this == FINALIZADO || this == CANCELADO;
    }

    public boolean exigeSaida() {
        return this == SAIU_PARA_ENTREGA || this == RETIRADO;
    }

    public static PedidoStatus from(String valor) {
        if (valor == null || valor.isBlank()) {
            return RECEBIDO;
        }
        String normalizado = valor.trim().toUpperCase();
        if ("EM_PREPARO".equals(normalizado)) {
            normalizado = EM_PREPARACAO.name();
        } else if ("ENTREGUE".equals(normalizado)) {
            normalizado = FINALIZADO.name();
        }
        try {
            return PedidoStatus.valueOf(normalizado);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status de pedido inválido: " + valor + ".");
        }
    }
}
