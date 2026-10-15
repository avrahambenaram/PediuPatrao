package com.umc.pediupatrao.controller;

import org.springframework.web.bind.annotation.*;

import com.umc.pediupatrao.dto.PaginaResponse;
import com.umc.pediupatrao.dto.PedidoRequest;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.service.PedidoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public Pedido criarPedido(@RequestBody PedidoRequest request) {
        return pedidoService.criarPedido(request);
    }

    @GetMapping
    public PaginaResponse<Pedido> listarPedidos(
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanho", defaultValue = "20") int tamanho) {
        return PaginaResponse.de(pedidoService.listarPaginado(pagina, tamanho));
    }

    @PutMapping("/{id}/status")
    public Pedido atualizarStatus(@PathVariable String id, @RequestParam String status) {
        return pedidoService.atualizarStatus(id, status);
    }

    @PutMapping("/{id}/cancelar")
    public Pedido cancelarPedido(@PathVariable String id) {
        return pedidoService.cancelar(id);
    }
}
