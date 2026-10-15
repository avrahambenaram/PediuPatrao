package com.umc.pediupatrao.audit;

import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.umc.pediupatrao.security.UsuarioSessao;

import jakarta.servlet.http.HttpServletRequest;

public final class ContextoAuditoria {

    private ContextoAuditoria() {
    }

    public static String usuarioAtual() {
        return UsuarioSessao.username();
    }

    public static String perfilAtual() {
        Authentication autenticacao = UsuarioSessao.autenticacaoAtual();
        if (autenticacao == null) {
            return null;
        }
        return autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    public static String ipAtual() {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        if (atributos instanceof ServletRequestAttributes servlet) {
            HttpServletRequest requisicao = servlet.getRequest();
            String encaminhado = requisicao.getHeader("X-Forwarded-For");
            if (encaminhado != null && !encaminhado.isBlank()) {
                return encaminhado.split(",")[0].trim();
            }
            return requisicao.getRemoteAddr();
        }
        return null;
    }
}
