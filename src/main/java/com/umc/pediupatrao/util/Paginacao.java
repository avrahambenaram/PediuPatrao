package com.umc.pediupatrao.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Paginacao {

    public static final int TAMANHO_PADRAO = 20;
    public static final int TAMANHO_MAXIMO = 100;

    private Paginacao() {
    }

    public static Pageable criar(int pagina, int tamanho, String campo, Sort.Direction direcao) {
        return PageRequest.of(Math.max(pagina, 0), normalizarTamanho(tamanho), Sort.by(direcao, campo));
    }

    private static int normalizarTamanho(int tamanho) {
        if (tamanho < 1) {
            return TAMANHO_PADRAO;
        }
        return Math.min(tamanho, TAMANHO_MAXIMO);
    }
}
