package com.umc.pediupatrao.service;

import com.umc.pediupatrao.audit.Auditavel;
import com.umc.pediupatrao.entity.AcaoAuditoria;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.UsuarioRole;
import com.umc.pediupatrao.repository.ProdutoRepository;
import com.umc.pediupatrao.security.UsuarioSessao;
import com.umc.pediupatrao.util.Paginacao;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Auditavel(tipo = AcaoAuditoria.CRIACAO, acao = "CRIAR_PRODUTO",
            descricao = "Cadastro de produto", entidade = "produtos")
    public Produto novoProduto(Produto produto) {
        exigirAdministrador();
        return produtoRepository.save(produto);
    }

    public Page<Produto> listarPaginado(int pagina, int tamanho) {
        return produtoRepository.findAll(Paginacao.criar(pagina, tamanho, "nome", Sort.Direction.ASC));
    }

    public List<Produto> listarDisponiveis() {
        return produtoRepository.findAll(Sort.by(Sort.Direction.ASC, "nome")).stream()
                .filter(produto -> Boolean.TRUE.equals(produto.getAtivo()))
                .toList();
    }

    public Optional<Produto> buscarPorId(String id) {
        return produtoRepository.findById(id);
    }

    @Auditavel(tipo = AcaoAuditoria.EXCLUSAO, acao = "EXCLUIR_PRODUTO",
            descricao = "Exclusão de produto", entidade = "produtos", registrarArgumentos = true)
    public void excluir(String id) {
        exigirAdministrador();
        produtoRepository.deleteById(id);
    }


    @Auditavel(acao = "SALVAR_PRODUTO",
            descricao = "Cadastro ou atualização de produto", entidade = "produtos")
    public Produto salvar(Produto produto) {
        exigirAdministrador();
        if (produto.getId() == null) {
            return produtoRepository.save(produto);  // Cria um novo produto
        } 
        else {
            if (produtoRepository.existsById(produto.getId())) {
                return produtoRepository.save(produto);
            } else {
                throw new IllegalArgumentException("Produto não encontrado para atualização.");
            }
        }
    }

    private void exigirAdministrador() {
        if (!UsuarioSessao.possuiPerfil(UsuarioRole.ADMIN)) {
            throw new AccessDeniedException("Apenas administradores podem gerenciar produtos.");
        }
    }
}
