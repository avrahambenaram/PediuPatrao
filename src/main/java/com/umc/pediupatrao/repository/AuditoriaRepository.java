package com.umc.pediupatrao.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.umc.pediupatrao.entity.Auditoria;

public interface AuditoriaRepository extends MongoRepository<Auditoria, String> {
}
