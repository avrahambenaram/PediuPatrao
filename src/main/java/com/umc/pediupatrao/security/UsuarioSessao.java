package com.umc.pediupatrao.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.umc.pediupatrao.entity.UsuarioRole;

public final class UsuarioSessao {

    private UsuarioSessao() {
    }

    public static String username() {
        Authentication autenticacao = autenticacaoAtual();
        return autenticacao == null ? null : autenticacao.getName();
    }

    public static boolean possuiPerfil(UsuarioRole perfil) {
        Authentication autenticacao = autenticacaoAtual();
        if (autenticacao == null) {
            return false;
        }
        String alvo = "ROLE_" + perfil.name();
        return autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(alvo::equalsIgnoreCase);
    }

    public static Authentication autenticacaoAtual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()
                || autenticacao instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return autenticacao;
    }
}
