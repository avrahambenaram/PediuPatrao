package com.umc.pediupatrao.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.repository.AuditoriaRepository;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    public void registrar(Auditoria auditoria) {
        try {
            auditoriaRepository.save(auditoria);
        } catch (Exception e) {
            log.error("Falha ao persistir auditoria da ação '{}': {}", auditoria.getAcao(), e.getMessage());
        }
    }

    public List<Auditoria> listarTodas() {
        return auditoriaRepository.findAll();
    }
}
