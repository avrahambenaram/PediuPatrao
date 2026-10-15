package com.umc.pediupatrao.audit;

import java.time.Instant;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.service.AuditoriaService;

/**
 * Intercepta os métodos anotados com {@link Auditavel} e registra cada execução.
 *
 * <p>O registro é gravado tanto em caso de sucesso quanto de falha, sem
 * interferir no fluxo original: exceções da operação são repropagadas.</p>
 */
@Aspect
@Component
public class AuditoriaAspect {

    private final AuditoriaService auditoriaService;

    public AuditoriaAspect(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

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
            auditoria.setDuracaoMs(System.currentTimeMillis() - inicio);
            auditoriaService.registrar(auditoria);
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
