package com.umc.pediupatrao.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.repository.AuditoriaRepository;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private static final int TAMANHO_PADRAO = 20;
    private static final int TAMANHO_MAXIMO = 100;

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public void registrar(Auditoria auditoria) {
        try {
            auditoriaRepository.save(auditoria);
        } catch (Exception e) {
            log.error("Falha ao persistir auditoria da ação '{}': {}", auditoria.getAcao(), e.getMessage());
        }
    }

    public Page<Auditoria> listarPaginado(int pagina, int tamanho) {
        Pageable paginacao = PageRequest.of(
                Math.max(pagina, 0),
                normalizarTamanho(tamanho),
                Sort.by(Sort.Direction.DESC, "dataHora"));
        return auditoriaRepository.findAll(paginacao);
    }

    private int normalizarTamanho(int tamanho) {
        if (tamanho < 1) {
            return TAMANHO_PADRAO;
        }
        return Math.min(tamanho, TAMANHO_MAXIMO);
    }
}
