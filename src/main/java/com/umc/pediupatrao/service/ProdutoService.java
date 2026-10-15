package com.umc.pediupatrao.service;

import com.umc.pediupatrao.audit.Auditavel;
import com.umc.pediupatrao.entity.AcaoAuditoria;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Auditavel(tipo = AcaoAuditoria.CRIACAO, acao = "CRIAR_PRODUTO",
            descricao = "Cadastro de produto", entidade = "produtos")
    public Produto novoProduto(Produto produto) {
        return produtoRepository.save(produto);
    }

    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }

    @Auditavel(tipo = AcaoAuditoria.EXCLUSAO, acao = "EXCLUIR_PRODUTO",
            descricao = "Exclusão de produto", entidade = "produtos", registrarArgumentos = true)
    public void excluir(String id) {
        produtoRepository.deleteById(id);
    }


    @Auditavel(acao = "SALVAR_PRODUTO",
            descricao = "Cadastro ou atualização de produto", entidade = "produtos")
    public Produto salvar(Produto produto) {
        if (produto.getId() == null) {
            return produtoRepository.save(produto);  // Cria um novo cliente
        } 
        else {
            if (produtoRepository.existsById(produto.getId())) {
                return produtoRepository.save(produto);
            } else {
                throw new IllegalArgumentException("Cliente não encontrado para atualização.");
            }
        }
    }
}
