package com.agibank.aposentarbemfx.service;

import com.agibank.aposentarbemfx.model.Contribuicao;
import com.agibank.aposentarbemfx.model.Usuario;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.agibank.aposentarbemfx.model.Usuario.calcularIdade;

public class RegraAposReformaService {

    private static final double PERCENTUAL_BASE = 0.60; // 60%
    private static final double ACRESCIMO_ANUAL = 0.02; // 2% por ano extra
    private static final double TETO_INSS = 8157.41; // Teto do INSS

    private ElegibilidadeService elegibilidadeService;

    public RegraAposReformaService(ElegibilidadeService elegibilidadeService) {
        this.elegibilidadeService = elegibilidadeService;
    }

    public double simularAposentadoria(Usuario usuario, List<Contribuicao> contribuicoes) {
        int idadeAtual = calcularIdade(usuario.getDataNascimento());
        int idadeDesejada = usuario.getIdadeAposentadoriaDesejada();

        if (idadeDesejada <= idadeAtual) {
            throw new IllegalArgumentException("A idade de aposentadoria desejada deve ser maior que a idade atual!");
        }

        boolean contribuiuAntesDaReforma = elegibilidadeService.contribuiuAntesDaReforma(contribuicoes);

        int mesesContribuidos = calcularTotalMesesContribuidos(contribuicoes);
        int anosContribuidos = mesesContribuidos / 12;

        double mediaContribuicoes = calcularMediaContribuicoes(contribuicoes, mesesContribuidos);

        int anosFaltando = idadeDesejada - idadeAtual;
        int anosFuturos = Math.max(0, anosFaltando); // Só soma tempo futuro positivo
        int totalAnosContribuidos = anosContribuidos + anosFuturos;

        int minimoContribuicao;
        int idadeMinima;

        if (usuario.getProfissao() == Usuario.Profissao.PROFESSOR) {
            if (contribuiuAntesDaReforma) {
                // Professores que já estavam no sistema antes da reforma têm regras especiais
                minimoContribuicao = 25;
                idadeMinima = (usuario.getGenero() == Usuario.Genero.FEMININO) ? 52 : 55; // Regra de transição
            } else {
                // Após reforma
                minimoContribuicao = 25;
                idadeMinima = (usuario.getGenero() == Usuario.Genero.FEMININO) ? 57 : 60;
            }
        } else {
            // Regras padrão (não professor)
            minimoContribuicao = (usuario.getGenero() == Usuario.Genero.FEMININO) ? 15 : 20;
            idadeMinima = (usuario.getGenero() == Usuario.Genero.FEMININO) ? 62 : 65;
        }

        // Validação mínima de idade
        if (idadeDesejada < idadeMinima) {
            return 0.0; // ❌ A idade desejada ainda não permite aposentadoria
        }

        int anosExtras = Math.max(0, totalAnosContribuidos - minimoContribuicao);
        double coeficiente = PERCENTUAL_BASE + (anosExtras * ACRESCIMO_ANUAL);
        coeficiente = Math.min(coeficiente, 1.0);

        double valorAposentadoria = mediaContribuicoes * coeficiente;

        return Math.min(valorAposentadoria, TETO_INSS);
    }

    private int calcularTotalMesesContribuidos(List<Contribuicao> contribuicoes) {
        int totalMeses = 0;
        for (Contribuicao c : contribuicoes) {
            totalMeses += ChronoUnit.MONTHS.between(c.getPeriodoInicio(), c.getPeriodoFim());
        }
        return totalMeses;
    }

    private double calcularMediaContribuicoes(List<Contribuicao> contribuicoes, int totalMeses) {
        if (totalMeses == 0) return 0.0;

        double somaSalarios = 0.0;
        for (Contribuicao c : contribuicoes) {
            long mesesContribuidos = ChronoUnit.MONTHS.between(c.getPeriodoInicio(), c.getPeriodoFim());
            somaSalarios += c.getValorSalario() * mesesContribuidos;
        }

        return somaSalarios / totalMeses;
    }
}