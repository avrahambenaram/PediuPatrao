package com.umc.pediupatrao.service;

import com.umc.pediupatrao.audit.Auditavel;
import com.umc.pediupatrao.entity.AcaoAuditoria;
import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.util.Paginacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public Cliente novoCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public Page<Cliente> listarPaginado(int pagina, int tamanho) {
        return clienteRepository.findAll(Paginacao.criar(pagina, tamanho, "nome", Sort.Direction.ASC));
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"));
    }

    @Auditavel(tipo = AcaoAuditoria.EXCLUSAO, acao = "EXCLUIR_CLIENTE",
            descricao = "Exclusão de cliente", entidade = "clientes", registrarArgumentos = true)
    public void excluir(String id) {
        clienteRepository.deleteById(id);
    }

    public Optional<Cliente> buscarPorId(String id) {
        return clienteRepository.findById(id);
    }

    @Auditavel(acao = "SALVAR_CLIENTE",
            descricao = "Cadastro ou atualização de cliente", entidade = "clientes")
    public Cliente salvar(Cliente cliente) {
        if (cliente.getId() == null) {
            return clienteRepository.save(cliente);  // Cria um novo cliente
        }
        else {
            if (clienteRepository.existsById(cliente.getId())) {
                return clienteRepository.save(cliente);
            } else {
                throw new IllegalArgumentException("Cliente não encontrado para atualização.");
            }
        }
    }

}
