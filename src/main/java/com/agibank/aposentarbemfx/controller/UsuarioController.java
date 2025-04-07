package com.agibank.aposentarbemfx.controller;

import com.agibank.aposentarbemfx.dao.usuario.UsuarioDAOImpl;
import com.agibank.aposentarbemfx.model.Usuario;
import com.agibank.aposentarbemfx.view.usuario.UsuarioView;

public class UsuarioController {
    private final UsuarioDAOImpl usuarioDAO;
    private final UsuarioView usuarioView;
    private Usuario usuario;

    public UsuarioController(UsuarioDAOImpl usuarioDAO, UsuarioView usuarioView) {
        this.usuarioDAO = usuarioDAO;
        this.usuarioView = usuarioView;
    }

    public void criarUsuario(Usuario usuario) {
        boolean sucesso = usuarioDAO.criarUsuario(usuario);

        if (sucesso) {
            this.usuario = usuario;
            usuarioView.exibirMensagem("Usuário cadastrado com sucesso!\n");
        } else {
            usuarioView.exibirMensagem("Erro ao cadastrar usuário.");
        }
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
