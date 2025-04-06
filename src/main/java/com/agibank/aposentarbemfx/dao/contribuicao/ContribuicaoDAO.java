package com.agibank.aposentarbemfx.dao.contribuicao;

import com.agibank.aposentarbemfx.model.Contribuicao;
import com.agibank.aposentarbemfx.model.Usuario;

import java.util.List;

public interface ContribuicaoDAO {
    void adicionar(Contribuicao contribuicao);

    Contribuicao buscarPorId(int idContribuicao);

    List<Contribuicao> buscarPorUsuario(int idUsuario);

    void atualizar(Contribuicao contribuicao);

    void remover(int idContribuicao);
}
