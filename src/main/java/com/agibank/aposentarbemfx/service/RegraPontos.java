package com.agibank.aposentarbemfx.service;

import com.agibank.aposentarbemfx.model.Contribuicao;
import com.agibank.aposentarbemfx.model.Usuario;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegraPontos {

    private static final LocalDate DATA_REFORMA = LocalDate.of(2019, 11, 13);
    private static final LocalDate DATA_CORTE_1994 = LocalDate.of(1994, 7, 1);
    private static final double VALOR_MINIMO_APOSENTADORIA = 1518.00;

    private final ElegibilidadeService elegibilidadeService;

    public RegraPontos(ElegibilidadeService elegibilidadeService) {
        this.elegibilidadeService = elegibilidadeService;
    }

    public Map<String, Object> calcularRegraPontos(Usuario usuario, List<Contribuicao> contribuicoes, int idadeAtual) {
        Map<String, Object> resultado = new HashMap<>();

        boolean elegivel = elegibilidadeService.isElegivelPontos(usuario, contribuicoes, idadeAtual);

        if (elegivel) {
            int anoAtual = usuario.getDataNascimento().plusYears(idadeAtual).getYear();
            double valor = calcularValor(usuario, contribuicoes, idadeAtual);

            resultado.put("elegivel", true);
            resultado.put("anoElegivel", anoAtual);
            resultado.put("valorEstimado", valor);
        } else {
            // Simular até encontrar idade que atinja os pontos mínimos
            int idadeSimulada = idadeAtual + 1;
            while (true) {
                if (elegibilidadeService.isElegivelPontos(usuario, contribuicoes, idadeSimulada)) {
                    int anoSimulado = usuario.getDataNascimento().plusYears(idadeSimulada).getYear();
                    double valorSimulado = calcularValor(usuario, contribuicoes, idadeSimulada);

                    resultado.put("elegivel", false);
                    resultado.put("anoElegivel", anoSimulado);
                    resultado.put("idadeElegivel", idadeSimulada);
                    resultado.put("valorEstimado", valorSimulado);
                    break;
                }
                idadeSimulada++;
            }
        }

        return resultado;
    }

    private double calcularValor(Usuario usuario, List<Contribuicao> contribuicoes, int idadeNaAposentadoria) {
        int mesesContribuidos = calcularMesesContribuidosAteIdade(usuario, contribuicoes, idadeNaAposentadoria);
        int anosContribuidos = mesesContribuidos / 12;

        double mediaSalarial = calcularMediaSalariosDesde1994(contribuicoes);

        int minimoContribuicao = getTempoMinimoContribuicao(usuario);
        int anosExtras = Math.max(0, anosContribuidos - minimoContribuicao);

        double coeficiente = 0.60 + (anosExtras * 0.02);
        coeficiente = Math.min(coeficiente, 1.0);

        double aposentadoria = mediaSalarial * coeficiente;
        return Math.max(aposentadoria, VALOR_MINIMO_APOSENTADORIA);
    }

    private int calcularMesesContribuidosAteIdade(Usuario usuario, List<Contribuicao> contribuicoes, int idadeDesejada) {
        LocalDate dataNascimento = usuario.getDataNascimento();
        LocalDate dataAposentadoria = dataNascimento.plusYears(idadeDesejada);

        int totalMeses = 0;
        for (Contribuicao c : contribuicoes) {
            LocalDate inicio = c.getPeriodoInicio();
            LocalDate fim = c.getPeriodoFim().isAfter(dataAposentadoria) ? dataAposentadoria : c.getPeriodoFim();
            if (fim.isBefore(inicio)) continue;
            totalMeses += (int) ChronoUnit.MONTHS.between(inicio, fim);
        }
        return totalMeses;
    }

    private double calcularMediaSalariosDesde1994(List<Contribuicao> contribuicoes) {
        double somaSalarios = 0;
        int totalMeses = 0;
        for (Contribuicao c : contribuicoes) {
            LocalDate inicio = c.getPeriodoInicio().isBefore(DATA_CORTE_1994) ? DATA_CORTE_1994 : c.getPeriodoInicio();
            LocalDate fim = c.getPeriodoFim();
            if (fim.isBefore(inicio)) continue;
            long meses = ChronoUnit.MONTHS.between(inicio, fim);
            somaSalarios += c.getValorSalario() * meses;
            totalMeses += meses;
        }
        return (totalMeses > 0) ? (somaSalarios / totalMeses) : 0.0;
    }

    private int getTempoMinimoContribuicao(Usuario usuario) {
        if (usuario.getProfissao() == Usuario.Profissao.PROFESSOR) {
            return (usuario.getGenero() == Usuario.Genero.FEMININO) ? 25 : 30;
        }
        return (usuario.getGenero() == Usuario.Genero.FEMININO) ? 15 : 20;
    }

    public int calcularTempoContribuicao(List<Contribuicao> contribuicoes) {
        long totalMeses = 0;
        for (Contribuicao c : contribuicoes) {
            long meses = ChronoUnit.MONTHS.between(c.getPeriodoInicio(), c.getPeriodoFim());
            if (meses > 0) {
                totalMeses += meses;
            }
        }
        return (int) (totalMeses / 12);
    }

    public double calcularMediaSalarial(List<Contribuicao> contribuicoes) {
        double totalSalario = 0.0;
        int totalMeses = 0;
        for (Contribuicao c : contribuicoes) {
            long meses = ChronoUnit.MONTHS.between(c.getPeriodoInicio(), c.getPeriodoFim());
            if (meses > 0) {
                totalSalario += c.getValorSalario() * meses;
                totalMeses += meses;
            }
        }
        return totalMeses > 0 ? totalSalario / totalMeses : 0.0;
    }
}