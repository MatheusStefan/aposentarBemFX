package com.agibank.aposentarbemfx.dao.usuario;

import com.agibank.aposentarbemfx.model.Usuario;

public interface UsuarioDAO {
    public boolean criarUsuario(Usuario usuario);
    Usuario buscarPorId(int id);
}
