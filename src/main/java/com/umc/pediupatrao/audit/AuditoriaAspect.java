package com.umc.pediupatrao.audit;

import java.time.Instant;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.service.AuditoriaService;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditoriaAspect {

    private final AuditoriaService auditoriaService;

    @Around("@annotation(auditavel)")
    public Object auditar(ProceedingJoinPoint ponto, Auditavel auditavel) throws Throwable {
        long inicio = System.currentTimeMillis();
        Auditoria auditoria = construirRegistro(ponto, auditavel);

        try {
            Object resultado = ponto.proceed();
            auditoria.setSucesso(true);
            return resultado;
        } catch (Throwable erro) {
            auditoria.setSucesso(false);
            auditoria.setMensagem(erro.getMessage());
            throw erro;
        } finally {
            auditoria.setDadosAnteriores(ContextoAuditoria.estadoAnterior());
            auditoria.setDadosPosteriores(ContextoAuditoria.estadoPosterior());
            auditoria.setDuracaoMs(System.currentTimeMillis() - inicio);
            auditoriaService.registrar(auditoria);
            ContextoAuditoria.limparEstados();
        }
    }

    private Auditoria construirRegistro(ProceedingJoinPoint ponto, Auditavel anotacao) {
        MethodSignature assinatura = (MethodSignature) ponto.getSignature();

        Auditoria auditoria = new Auditoria();
        auditoria.setDataHora(Instant.now());
        auditoria.setUsuario(ContextoAuditoria.usuarioAtual());
        auditoria.setPerfil(ContextoAuditoria.perfilAtual());
        auditoria.setIp(ContextoAuditoria.ipAtual());
        auditoria.setTipo(anotacao.tipo());
        auditoria.setAcao(resolverAcao(anotacao, assinatura));
        auditoria.setDescricao(anotacao.descricao());
        auditoria.setEntidade(anotacao.entidade());
        auditoria.setMetodo(assinatura.getDeclaringType().getSimpleName() + "." + assinatura.getName());

        if (anotacao.registrarArgumentos()) {
            auditoria.setArgumentos(
                    FormatadorArgumentos.formatar(ponto.getArgs(), assinatura.getParameterNames()));
        }
        return auditoria;
    }

    private String resolverAcao(Auditavel anotacao, MethodSignature assinatura) {
        if (anotacao.acao() == null || anotacao.acao().isBlank()) {
            return assinatura.getName();
        }
        return anotacao.acao();
    }
}
