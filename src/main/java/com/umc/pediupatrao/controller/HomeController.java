package com.umc.pediupatrao.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.umc.pediupatrao.dto.ItemPedidoRequest;
import com.umc.pediupatrao.dto.PedidoRequest;
import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.entity.UsuarioRole;
import com.umc.pediupatrao.service.AuditoriaService;
import com.umc.pediupatrao.service.ClienteService;
import com.umc.pediupatrao.service.PedidoService;
import com.umc.pediupatrao.service.ProdutoService;
import com.umc.pediupatrao.service.UsuarioService;
import com.umc.pediupatrao.security.UsuarioSessao;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final UsuarioService usuarioService;
    private final AuditoriaService auditoriaService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("content", "home :: content");
        return "layout";
    }

    // ========================
    // USUÁRIOS
    // ========================
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanho", defaultValue = "20") int tamanho) {
        Page<Usuario> resultados = usuarioService.listarPaginado(pagina, tamanho);
        preencherPaginacao(model, resultados);
        model.addAttribute("usuarios", resultados.getContent());
        model.addAttribute("content", "usuarios/lista :: content");
        log.info("Carregando fragmento: usuarios/lista :: content");
        return "layout";
    }

    @GetMapping("/usuarios/novo")
    public String novoUsuarioForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("content", "usuarios/form :: content");
        return "layout";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuarioForm(@PathVariable String id, Model model,
            RedirectAttributes redirectAttributes) {
        boolean encontrado = usuarioService.buscarPorId(id).map(u -> {
            model.addAttribute("usuario", u);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado.");
            return "redirect:/usuarios";
        }

        model.addAttribute("content", "usuarios/form :: content");
        return "layout";
    }

    @PostMapping("/usuarios/salvar")
    public String salvarUsuario(@ModelAttribute Usuario usuario,
            RedirectAttributes redirectAttributes) {
        usuarioService.salvarUsuario(usuario);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário criado com sucesso!");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/editar/{id}")
    public String atualizarUsuario(@PathVariable String id,
            @ModelAttribute Usuario usuario,
            RedirectAttributes redirectAttributes) {
        usuarioService.atualizarUsuario(id, usuario);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário atualizado com sucesso!");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/deletar/{id}")
    public String deletarUsuario(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        usuarioService.deletarUsuario(id);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário removido com sucesso!");
        return "redirect:/usuarios";
    }

    // ========================
    // PEDIDOS
    // ========================
    @GetMapping("/pedidos")
    public String pedidos(Model model,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanho", defaultValue = "20") int tamanho) {
        Page<Pedido> resultados = pedidoService.listarPaginado(pagina, tamanho);
        preencherPaginacao(model, resultados);
        model.addAttribute("pedidos", resultados.getContent());
        model.addAttribute("podeRegistrar", podeRegistrarPedido());
        model.addAttribute("podeGerenciar", UsuarioSessao.possuiPerfil(UsuarioRole.GERENTE));
        model.addAttribute("content", "pedidos/lista :: content");
        log.info("Carregando fragmento: pedidos/lista :: content");
        return "layout";
    }

    @GetMapping("/pedidos/novo")
    public String novoPedidoForm(Model model, RedirectAttributes redirectAttributes) {
        if (!podeRegistrarPedido()) {
            redirectAttributes.addFlashAttribute("erro", "Seu perfil não pode registrar pedidos.");
            return "redirect:/pedidos";
        }
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("produtos", produtoService.listarDisponiveis());
        model.addAttribute("podeGerenciar", UsuarioSessao.possuiPerfil(UsuarioRole.GERENTE));
        model.addAttribute("content", "pedidos/form :: content");
        return "layout";
    }

    @PostMapping("/pedidos/salvar")
    public String salvarPedido(@RequestParam String clienteId,
            @RequestParam(name = "produtoId", required = false) List<String> produtoIds,
            @RequestParam(name = "quantidade", required = false) List<Integer> quantidades,
            @RequestParam(name = "descontoPercentual", defaultValue = "0") float descontoPercentual,
            RedirectAttributes redirectAttributes) {
        try {
            pedidoService.criarPedido(
                    montarPedidoRequest(clienteId, produtoIds, quantidades, descontoPercentual));
            redirectAttributes.addFlashAttribute("sucesso", "Pedido registrado com sucesso!");
            return "redirect:/pedidos";
        } catch (IllegalArgumentException | AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/pedidos/novo";
        }
    }

    @GetMapping("/pedidos/{id}")
    public String detalhePedido(@PathVariable String id, Model model,
            RedirectAttributes redirectAttributes) {
        boolean encontrado = pedidoService.buscarPorId(id).map(pedido -> {
            model.addAttribute("pedido", pedido);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Pedido não encontrado.");
            return "redirect:/pedidos";
        }

        model.addAttribute("podeRegistrar", podeRegistrarPedido());
        model.addAttribute("podeGerenciar", UsuarioSessao.possuiPerfil(UsuarioRole.GERENTE));
        model.addAttribute("content", "pedidos/detalhe :: content");
        return "layout";
    }

    @PostMapping("/pedidos/status/{id}")
    public String atualizarStatusPedido(@PathVariable String id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        try {
            pedidoService.atualizarStatus(id, status);
            redirectAttributes.addFlashAttribute("sucesso", "Status atualizado com sucesso!");
        } catch (IllegalArgumentException | AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @PostMapping("/pedidos/desconto/{id}")
    public String aplicarDescontoPedido(@PathVariable String id,
            @RequestParam float descontoPercentual,
            RedirectAttributes redirectAttributes) {
        try {
            pedidoService.aplicarDesconto(id, descontoPercentual);
            redirectAttributes.addFlashAttribute("sucesso", "Desconto aplicado com sucesso!");
        } catch (IllegalArgumentException | AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @PostMapping("/pedidos/cancelar/{id}")
    public String cancelarPedido(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        try {
            pedidoService.cancelar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido cancelado com sucesso!");
        } catch (IllegalArgumentException | AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pedidos";
    }

    private boolean podeRegistrarPedido() {
        return UsuarioSessao.possuiPerfil(UsuarioRole.GERENTE)
                || UsuarioSessao.possuiPerfil(UsuarioRole.ATENDENTE);
    }

    private PedidoRequest montarPedidoRequest(String clienteId, List<String> produtoIds,
            List<Integer> quantidades, float descontoPercentual) {
        PedidoRequest request = new PedidoRequest();
        request.setClienteId(clienteId);
        request.setDescontoPercentual(descontoPercentual);

        List<ItemPedidoRequest> itens = new ArrayList<>();
        if (produtoIds != null) {
            for (int i = 0; i < produtoIds.size(); i++) {
                if (produtoIds.get(i) == null || produtoIds.get(i).isBlank()) {
                    continue;
                }
                ItemPedidoRequest item = new ItemPedidoRequest();
                item.setProdutoId(produtoIds.get(i));
                Integer quantidade = (quantidades != null && i < quantidades.size()) ? quantidades.get(i) : null;
                item.setQuantidade(quantidade == null ? 0 : quantidade);
                itens.add(item);
            }
        }
        request.setItens(itens);
        return request;
    }

    // ========================
    // PRODUTOS
    // ========================
    @GetMapping("/produtos")
    public String produtos(Model model,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanho", defaultValue = "20") int tamanho) {
        Page<Produto> resultados = produtoService.listarPaginado(pagina, tamanho);
        preencherPaginacao(model, resultados);
        model.addAttribute("produtos", resultados.getContent());
        model.addAttribute("content", "produtos/lista :: content");
        log.info("Carregando fragmento: produtos/lista :: content");
        return "layout";
    }

    @GetMapping("/produtos/novo")
    public String novoProdutoForm(Model model) {
        model.addAttribute("produto", new Produto());
        model.addAttribute("content", "produtos/form :: content");
        return "layout";
    }

    @GetMapping("/produtos/editar/{id}")
    public String editarProdutoForm(@PathVariable String id, Model model,
            RedirectAttributes redirectAttributes) {
        boolean encontrado = produtoService.buscarPorId(id).map(p -> {
            model.addAttribute("produto", p);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Produto não encontrado.");
            return "redirect:/produtos";
        }

        model.addAttribute("content", "produtos/form :: content");
        return "layout";
    }

    @PostMapping("/produtos/salvar")
    public String salvarProduto(@ModelAttribute Produto produto,
            RedirectAttributes redirectAttributes) {
        try {
            produtoService.salvar(produto);
            redirectAttributes.addFlashAttribute("sucesso", "Produto criado com sucesso!");
        } catch (IllegalArgumentException | AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/produtos";
    }

    @PostMapping("/produtos/editar/{id}")
    public String atualizarProduto(@PathVariable String id,
            @ModelAttribute Produto produto,
            RedirectAttributes redirectAttributes) {
        produto.setId(id);
        try {
            produtoService.salvar(produto);
            redirectAttributes.addFlashAttribute("sucesso", "Produto atualizado com sucesso!");
        } catch (IllegalArgumentException | AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/produtos";
    }

    @PostMapping("/produtos/excluir/{id}")
    public String excluirProduto(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        try {
            produtoService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Produto excluído com sucesso!");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/produtos";
    }

    // ========================
    // CLIENTES
    // ========================
    @GetMapping("/clientes")
    public String clientes(Model model) {
        model.addAttribute("podeGerenciar", UsuarioSessao.possuiPerfil(UsuarioRole.ATENDENTE));
        model.addAttribute("content", "clientes/lista :: content");
        log.info("Carregando fragmento: clientes/lista :: content");
        return "layout";
    }

    @GetMapping("/clientes/novo")
    public String novoClienteForm(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("content", "clientes/form :: content");
        return "layout";
    }

    @PostMapping("/clientes/salvar")
    public String salvarCliente(@ModelAttribute Cliente cliente,
            RedirectAttributes redirectAttributes) {
        clienteService.salvar(cliente);
        redirectAttributes.addFlashAttribute("sucesso", "Cliente criado com sucesso!");
        return "redirect:/clientes";
    }

    @GetMapping("/clientes/editar/{id}")
    public String editarClienteForm(@PathVariable String id, Model model,
            RedirectAttributes redirectAttributes) {
        boolean encontrado = clienteService.buscarPorId(id).map(c -> {
            model.addAttribute("cliente", c);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Cliente não encontrado.");
            return "redirect:/clientes";
        }

        model.addAttribute("content", "clientes/form :: content");
        return "layout";
    }

    @PostMapping("/clientes/editar/{id}")
    public String atualizarCliente(@PathVariable String id,
            @ModelAttribute Cliente cliente,
            RedirectAttributes redirectAttributes) {
        cliente.setId(id);
        clienteService.salvar(cliente);
        redirectAttributes.addFlashAttribute("sucesso", "Cliente atualizado com sucesso!");
        return "redirect:/clientes";
    }

    // ========================
    // AUDITORIA
    // ========================
    @GetMapping("/auditoria")
    public String auditoria(Model model,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanho", defaultValue = "20") int tamanho) {
        Page<Auditoria> resultados = auditoriaService.listarPaginado(pagina, tamanho);
        preencherPaginacao(model, resultados);
        model.addAttribute("auditorias", resultados.getContent());
        model.addAttribute("content", "auditoria/lista :: content");
        return "layout";
    }

    // ========================
    // OUTRAS ROTAS
    // ========================
    @GetMapping("/configuracoes")
    public String configuracoes(Model model) {
        model.addAttribute("content", "configuracoes :: content");
        return "layout";
    }

    @GetMapping("/teste")
    public String teste() {
        return "teste";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    private void preencherPaginacao(Model model, Page<?> pagina) {
        int atual = pagina.getNumber();
        int total = pagina.getTotalPages();

        int janela = 5;
        int inicio = Math.max(0, atual - janela / 2);
        int fim = Math.min(Math.max(total - 1, 0), inicio + janela - 1);
        inicio = Math.max(0, fim - janela + 1);

        long registroInicial = pagina.getTotalElements() == 0
                ? 0
                : (long) pagina.getNumber() * pagina.getSize() + 1;
        long registroFinal = Math.min((long) (pagina.getNumber() + 1) * pagina.getSize(),
                pagina.getTotalElements());

        model.addAttribute("paginaAtual", atual);
        model.addAttribute("totalPaginas", total);
        model.addAttribute("totalElementos", pagina.getTotalElements());
        model.addAttribute("tamanho", pagina.getSize());
        model.addAttribute("paginaInicial", inicio);
        model.addAttribute("paginaFinal", fim);
        model.addAttribute("registroInicial", registroInicial);
        model.addAttribute("registroFinal", registroFinal);
    }
}
