package com.umc.pediupatrao.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "auditorias")
public class Auditoria {

    @Id
    private String id;
    private Instant dataHora;
    private String usuario;
    private String perfil;
    private AcaoAuditoria tipo;
    private String acao;
    private String descricao;
    private String entidade;
    private String metodo;
    private String argumentos;
    private String dadosAnteriores;
    private String dadosPosteriores;
    private boolean sucesso;
    private String mensagem;
    private String ip;
    private long duracaoMs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getDataHora() {
        return dataHora;
    }

    public void setDataHora(Instant dataHora) {
        this.dataHora = dataHora;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public AcaoAuditoria getTipo() {
        return tipo;
    }

    public void setTipo(AcaoAuditoria tipo) {
        this.tipo = tipo;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getEntidade() {
        return entidade;
    }

    public void setEntidade(String entidade) {
        this.entidade = entidade;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public String getArgumentos() {
        return argumentos;
    }

    public void setArgumentos(String argumentos) {
        this.argumentos = argumentos;
    }

    public String getDadosAnteriores() {
        return dadosAnteriores;
    }

    public void setDadosAnteriores(String dadosAnteriores) {
        this.dadosAnteriores = dadosAnteriores;
    }

    public String getDadosPosteriores() {
        return dadosPosteriores;
    }

    public void setDadosPosteriores(String dadosPosteriores) {
        this.dadosPosteriores = dadosPosteriores;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getDuracaoMs() {
        return duracaoMs;
    }

    public void setDuracaoMs(long duracaoMs) {
        this.duracaoMs = duracaoMs;
    }
}
