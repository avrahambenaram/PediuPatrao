package com.umc.pediupatrao.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pedidos")
public class Pedido {

    @Id
    private String id;
    private String clienteId;
    private List<ItemPedido> itens = new ArrayList<>();
    private float subtotal;
    private float descontoPercentual;
    private float valorDesconto;
    private float valorTotal;
    private String registradoPor;
    private Instant dataHora;
    private String status;
    private String saidaPor;
    private Instant dataHoraSaida;
    private String canceladoPor;
    private Instant dataHoraCancelamento;
    private String justificativaCancelamento;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    public float getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(float subtotal) {
        this.subtotal = subtotal;
    }

    public float getDescontoPercentual() {
        return descontoPercentual;
    }

    public void setDescontoPercentual(float descontoPercentual) {
        this.descontoPercentual = descontoPercentual;
    }

    public float getValorDesconto() {
        return valorDesconto;
    }

    public void setValorDesconto(float valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public float getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(float valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(String registradoPor) {
        this.registradoPor = registradoPor;
    }

    public Instant getDataHora() {
        return dataHora;
    }

    public void setDataHora(Instant dataHora) {
        this.dataHora = dataHora;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCancelavel() {
        try {
            return PedidoStatus.from(status).podeCancelar();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String getSaidaPor() {
        return saidaPor;
    }

    public void setSaidaPor(String saidaPor) {
        this.saidaPor = saidaPor;
    }

    public Instant getDataHoraSaida() {
        return dataHoraSaida;
    }

    public void setDataHoraSaida(Instant dataHoraSaida) {
        this.dataHoraSaida = dataHoraSaida;
    }

    public String getCanceladoPor() {
        return canceladoPor;
    }

    public void setCanceladoPor(String canceladoPor) {
        this.canceladoPor = canceladoPor;
    }

    public Instant getDataHoraCancelamento() {
        return dataHoraCancelamento;
    }

    public void setDataHoraCancelamento(Instant dataHoraCancelamento) {
        this.dataHoraCancelamento = dataHoraCancelamento;
    }

    public String getJustificativaCancelamento() {
        return justificativaCancelamento;
    }

    public void setJustificativaCancelamento(String justificativaCancelamento) {
        this.justificativaCancelamento = justificativaCancelamento;
    }
}
