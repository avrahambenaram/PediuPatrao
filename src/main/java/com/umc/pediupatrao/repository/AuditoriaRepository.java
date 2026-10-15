package com.umc.pediupatrao.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.umc.pediupatrao.entity.Auditoria;

/**
 * Acesso aos registros de auditoria (coleção {@code auditorias}).
 *
 * <p>A visualização é feita diretamente no banco de dados; estes métodos
 * existem para consultas programáticas e inspeção.</p>
 */
public interface AuditoriaRepository extends MongoRepository<Auditoria, String> {

    List<Auditoria> findByUsuario(String usuario);

    List<Auditoria> findByAcao(String acao);

    List<Auditoria> findByEntidade(String entidade);
}
