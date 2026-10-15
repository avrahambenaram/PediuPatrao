package com.umc.pediupatrao.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import com.umc.pediupatrao.util.Paginacao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final AuditoriaRepository auditoriaRepository;

    public void registrar(Auditoria auditoria) {
        try {
            auditoriaRepository.save(auditoria);
        } catch (Exception e) {
            log.error("Falha ao persistir auditoria da ação '{}': {}", auditoria.getAcao(), e.getMessage());
        }
    }

    public Page<Auditoria> listarPaginado(int pagina, int tamanho) {
        return auditoriaRepository.findAll(Paginacao.criar(pagina, tamanho, "dataHora", Sort.Direction.DESC));
    }
}
