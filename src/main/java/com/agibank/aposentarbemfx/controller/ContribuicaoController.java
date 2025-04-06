package com.agibank.aposentarbemfx.controller;

import com.agibank.aposentarbemfx.dao.contribuicao.ContribuicaoDAOImpl;
import com.agibank.aposentarbemfx.model.Contribuicao;

import java.time.LocalDate;
import java.util.List;

public class ContribuicaoController {

    private final ContribuicaoDAOImpl contribuicaoDAO;

    public ContribuicaoController(ContribuicaoDAOImpl contribuicaoDAO) {
        this.contribuicaoDAO = contribuicaoDAO;
    }

    public void adicionarContribuicao(int idUsuario, double valorSalario, LocalDate periodoInicio, LocalDate periodoFim) {
        validarDados(valorSalario, periodoInicio, periodoFim);

        Contribuicao contribuicao = new Contribuicao(idUsuario, valorSalario, periodoInicio, periodoFim);
        contribuicaoDAO.adicionar(contribuicao);
    }

    public Contribuicao buscarContribuicaoPorId(int idContribuicao) {
        return contribuicaoDAO.buscarPorId(idContribuicao);
    }

    public List<Contribuicao> buscarContribuicoesPorUsuario(int idUsuario) {
        return contribuicaoDAO.buscarPorUsuario(idUsuario);
    }

    public void atualizarContribuicao(int idContribuicao, double novoSalario, LocalDate novoInicio, LocalDate novoFim) {
        validarDados(novoSalario, novoInicio, novoFim);

        Contribuicao contribuicaoExistente = contribuicaoDAO.buscarPorId(idContribuicao);
        if (contribuicaoExistente == null) {
            throw new IllegalArgumentException("Contribuição não encontrada!");
        }

        contribuicaoExistente.setValorSalario(novoSalario);
        contribuicaoExistente.setPeriodoInicio(novoInicio);
        contribuicaoExistente.setPeriodoFim(novoFim);

        contribuicaoDAO.atualizar(contribuicaoExistente);
    }

    public void removerContribuicao(int idContribuicao) {
        if (contribuicaoDAO.buscarPorId(idContribuicao) == null) {
            throw new IllegalArgumentException("Contribuição não encontrada!");
        }

        contribuicaoDAO.remover(idContribuicao);
    }

    private void validarDados(double valorSalario, LocalDate periodoInicio, LocalDate periodoFim) {
        if (valorSalario <= 0) {
            throw new IllegalArgumentException("O salário deve ser maior que zero!");
        }
        if (periodoInicio.isAfter(periodoFim)) {
            throw new IllegalArgumentException("A data de início não pode ser depois da data de fim!");
        }
    }
}