package com.umc.pediupatrao.repository;

import com.umc.pediupatrao.entity.Produto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProdutoRepository extends MongoRepository<Produto, String> {
}
