package com.agibank.aposentarbemfx.service;

import com.agibank.aposentarbemfx.model.Contribuicao;
import com.agibank.aposentarbemfx.model.Usuario;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RegraPedagio100 {

    private static final LocalDate DATA_CORTE_1994 = LocalDate.of(1994, 7, 1);
    private static final LocalDate DATA_REFORMA = LocalDate.of(2019, 11, 13);
    private ElegibilidadeService elegibilidadeService;

    public RegraPedagio100(ElegibilidadeService elegibilidadeService) {
        this.elegibilidadeService = elegibilidadeService;
    }

    public double calcularAposentadoria(Usuario usuario, List<Contribuicao> contribuicoes, int idadeAposentadoriaDesejada) {
        // ❌ Se o usuário não for elegível para o Pedágio 100%, retorna 0
        if (!elegibilidadeService.isElegivelPedagio100(usuario, contribuicoes)) {
            return 0;
        }

        // ✅ Calcular tempo contribuído até a idade desejada
        int mesesContribuidosAteReforma = calcularMesesContribuidosAteData(contribuicoes, DATA_REFORMA);

        // ✅ Determinar tempo mínimo de contribuição com Pedágio 100%
        int minimoContribuicaoAntiga = (usuario.getGenero() == Usuario.Genero.FEMININO) ? 30 * 12 : 35 * 12;
        if (usuario.getProfissao() == Usuario.Profissao.PROFESSOR) {
            minimoContribuicaoAntiga = (usuario.getGenero() == Usuario.Genero.FEMININO) ? 25 * 12 : 30 * 12;
        }

        int mesesFaltando = minimoContribuicaoAntiga - mesesContribuidosAteReforma;
        int tempoPedagio100 = mesesFaltando * 2;
        int totalMesesNecessarios = mesesContribuidosAteReforma + tempoPedagio100;

        // ✅ Calcular a idade de aposentadoria com o tempo necessário
        int idadeMinimaAposentadoria = usuario.getIdade() + (totalMesesNecessarios / 12);

        // ✅ Se já atingiu a idade mínima e o tempo necessário, usa o cálculo normal
        if (idadeMinimaAposentadoria <= idadeAposentadoriaDesejada) {
            return calcularMediaContribuicoesDesde1994(contribuicoes);
        }

        // ✅ Se ainda não atingiu o tempo necessário, simular contribuições futuras
        return calcularAposentadoriaComSimulacao(usuario, contribuicoes, idadeAposentadoriaDesejada, totalMesesNecessarios);
    }

    private double calcularMediaContribuicoesDesde1994(List<Contribuicao> contribuicoes) {
        double somaSalarios = 0.0;
        int totalMeses = 0;

        for (Contribuicao c : contribuicoes) {
            // Ajusta a data de início para não considerar contribuições antes de 1994
            LocalDate periodoInicio = c.getPeriodoInicio().isBefore(DATA_CORTE_1994) ? DATA_CORTE_1994 : c.getPeriodoInicio();
            LocalDate periodoFim = c.getPeriodoFim();

            if (periodoInicio.isAfter(periodoFim)) continue; // Ignorar períodos inválidos

            long meses = ChronoUnit.MONTHS.between(periodoInicio, periodoFim) + 1;

            if (meses > 0) {
                somaSalarios += c.getValorSalario() * meses;
                totalMeses += (int) meses;
            }
        }

        return (totalMeses > 0) ? (somaSalarios / totalMeses) : 0.0;
    }

    private int calcularMesesContribuidosAteData(List<Contribuicao> contribuicoes, LocalDate dataCorte) {
        int totalMeses = 0;

        for (Contribuicao c : contribuicoes) {
            if (c.getPeriodoFim().isBefore(dataCorte)) {
                long meses = ChronoUnit.MONTHS.between(c.getPeriodoInicio(), c.getPeriodoFim()) + 1;
                totalMeses += (int) meses;
            } else if (c.getPeriodoInicio().isBefore(dataCorte)) {
                long meses = ChronoUnit.MONTHS.between(c.getPeriodoInicio(), dataCorte) + 1;
                totalMeses += (int) meses;
            }
        }
        return totalMeses;
    }

    private double calcularAposentadoriaComSimulacao(Usuario usuario, List<Contribuicao> contribuicoes, int idadeAposentadoriaDesejada, int totalMesesNecessarios) {
        // ✅ Média salarial até o momento
        double mediaSalarial = calcularMediaContribuicoesDesde1994(contribuicoes);

        // ✅ Número de meses que faltam para atingir a idade desejada
        int mesesFaltandoAteIdadeDesejada = (idadeAposentadoriaDesejada - usuario.getIdade()) * 12;

        // ✅ Se o tempo total necessário ultrapassa a idade desejada, ajustamos os meses simulados
        int mesesASeremSimulados = Math.max(0, totalMesesNecessarios - calcularMesesContribuidosAteData(contribuicoes, LocalDate.now()));

        // ✅ Se ainda falta tempo para atingir o mínimo necessário, estimamos os meses simulados
        if (mesesASeremSimulados > 0) {
            double salarioSimulado = mediaSalarial * mesesASeremSimulados;
            double novoSalarioTotal = (mediaSalarial * calcularMesesContribuidosAteData(contribuicoes, LocalDate.now())) + salarioSimulado;
            int novoTotalMeses = calcularMesesContribuidosAteData(contribuicoes, LocalDate.now()) + mesesASeremSimulados;

            return novoSalarioTotal / novoTotalMeses;
        }

        // ✅ Retorna a média calculada se já atingiu o tempo necessário
        return mediaSalarial;
    }
}