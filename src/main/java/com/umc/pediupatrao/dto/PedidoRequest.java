package com.umc.pediupatrao.dto;

import java.util.List;

public class PedidoRequest {

    private String clienteId;
    private List<ItemPedidoRequest> itens;
    private float descontoPercentual;

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public List<ItemPedidoRequest> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoRequest> itens) {
        this.itens = itens;
    }

    public float getDescontoPercentual() {
        return descontoPercentual;
    }

    public void setDescontoPercentual(float descontoPercentual) {
        this.descontoPercentual = descontoPercentual;
    }
}
