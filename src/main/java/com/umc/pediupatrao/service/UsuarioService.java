package com.umc.pediupatrao.service;

import com.umc.pediupatrao.audit.Auditavel;
import com.umc.pediupatrao.entity.AcaoAuditoria;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import com.umc.pediupatrao.util.Paginacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Auditavel(tipo = AcaoAuditoria.CRIACAO, acao = "CRIAR_USUARIO",
            descricao = "Cadastro de usuário", entidade = "usuarios")
    public void salvarUsuario(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword())); // Criptografa a senha
        usuarioRepository.save(usuario);
    }

    // Deleta um usuário pelo ID
    @Auditavel(tipo = AcaoAuditoria.EXCLUSAO, acao = "DELETAR_USUARIO",
            descricao = "Exclusão de usuário", entidade = "usuarios", registrarArgumentos = true)
    public void deletarUsuario(String id) {
        usuarioRepository.deleteById(id);
    }

    public Page<Usuario> listarPaginado(int pagina, int tamanho) {
        return usuarioRepository.findAll(Paginacao.criar(pagina, tamanho, "username", Sort.Direction.ASC));
    }

    // Atualiza o usuário usuários
    @Auditavel(tipo = AcaoAuditoria.ATUALIZACAO, acao = "ATUALIZAR_USUARIO",
            descricao = "Atualização de usuário", entidade = "usuarios")
    public void atualizarUsuario(String id, Usuario usuarioAtualizado) {
        usuarioRepository.findById(id).ifPresent(usuarioExistente -> {
            usuarioExistente.setUsername(usuarioAtualizado.getUsername());
            usuarioExistente.setRole(usuarioAtualizado.getRole());

            // Só atualiza a senha se uma nova foi informada
            if (usuarioAtualizado.getPassword() != null && !usuarioAtualizado.getPassword().isBlank()) {
                usuarioExistente.setPassword(passwordEncoder.encode(usuarioAtualizado.getPassword()));
            }

            usuarioRepository.save(usuarioExistente);
        });
    }
}
