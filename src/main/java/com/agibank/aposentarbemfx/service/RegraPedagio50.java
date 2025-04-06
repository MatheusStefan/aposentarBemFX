package com.agibank.aposentarbemfx.service;

import com.agibank.aposentarbemfx.model.Contribuicao;
import com.agibank.aposentarbemfx.model.Usuario;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RegraPedagio50 {

    private static final LocalDate DATA_CORTE_1994 = LocalDate.of(1994, 7, 1);
    private static final LocalDate DATA_REFORMA = LocalDate.of(2019, 11, 13);
    private static final double ALIQUOTA_FIXA = 0.31; // 31% fixo do cálculo previdenciário
    private ElegibilidadeService elegibilidadeService;

    public RegraPedagio50(ElegibilidadeService elegibilidadeService) {
        this.elegibilidadeService = elegibilidadeService;
    }

    public double calcularAposentadoria(Usuario usuario, List<Contribuicao> contribuicoes, double expectativaVida) {
        // 1️⃣ Verificar se o usuário é elegível para o Pedágio 50%
        if (!isElegivelPedagio50(usuario, contribuicoes)) {
            return 0; // ❌ Se não for elegível, não pode se aposentar pelo Pedágio 50%
        }

        // 2️⃣ Calcular o tempo total de contribuição até a reforma de 2019
        int mesesContribuidosAteReforma = calcularMesesContribuidosAteData(contribuicoes, DATA_REFORMA);
        int anosContribuidosAteReforma = mesesContribuidosAteReforma / 12;

        // 3️⃣ Descobrir quanto tempo faltava para a aposentadoria mínima na época (35 anos para homens, 30 para mulheres)
        int minimoContribuicao = (usuario.getGenero() == Usuario.Genero.MASCULINO) ? 35 : 30;
        int anosFaltando = minimoContribuicao - anosContribuidosAteReforma;

        if (anosFaltando <= 0) {
            return 0; // ❌ Se já atingiu o mínimo, não precisa do pedágio.
        }

        // 4️⃣ Calcular o Pedágio (50% do tempo faltando)
        int anosPedagio = (int) Math.ceil(anosFaltando * 1.5); // Pedágio de 50% a mais do que faltava
        int totalAnosContribuidos = anosContribuidosAteReforma + anosPedagio;

        // 5️⃣ Calcular a média das contribuições desde 1994
        double mediaSalarial = calcularMediaContribuicoesDesde1994(contribuicoes);

        // 6️⃣ Calcular o Fator Previdenciário (FP)
        double fatorPrevidenciario = calcularFatorPrevidenciario(totalAnosContribuidos, expectativaVida);

        // 7️⃣ Calcular o valor final da aposentadoria
        double valorAposentadoria = mediaSalarial * fatorPrevidenciario;

        return valorAposentadoria;
    }

    /**
     * Verifica se o usuário é elegível para o Pedágio 50%.
     */
    public boolean isElegivelPedagio50(Usuario usuario, List<Contribuicao> contribuicoes) {
        int mesesContribuidosAteReforma = calcularMesesContribuidosAteData(contribuicoes, DATA_REFORMA);

        // 🚨 Bloqueia se tiver contribuições após a reforma
        boolean contribuiuDepoisDaReforma = contribuicoes.stream()
                .anyMatch(c -> c.getPeriodoInicio().isAfter(DATA_REFORMA));

        if (contribuiuDepoisDaReforma) {
            return false; // 🚫 Fora da regra
        }

        int anosContribuidosAteReforma = mesesContribuidosAteReforma / 12;
        int minimoContribuicao = (usuario.getGenero() == Usuario.Genero.MASCULINO) ? 35 : 30;

        // ✅ Elegível se contribuiu antes da reforma, mas não atingiu o mínimo de anos
        return anosContribuidosAteReforma < minimoContribuicao;
    }

    /**
     * Calcula o total de meses de contribuição do usuário até uma determinada data.
     */
    private int calcularMesesContribuidosAteData(List<Contribuicao> contribuicoes, LocalDate dataLimite) {
        return contribuicoes.stream()
                .filter(c -> c.getPeriodoInicio().isBefore(dataLimite))
                .mapToInt(c -> {
                    LocalDate inicio = c.getPeriodoInicio();
                    LocalDate fim = c.getPeriodoFim().isBefore(dataLimite) ? c.getPeriodoFim() : dataLimite;
                    return (int) ChronoUnit.MONTHS.between(inicio, fim);
                })
                .sum();
    }

    /**
     * Calcula a média salarial das contribuições do usuário considerando apenas os salários desde julho de 1994.
     */
    private double calcularMediaContribuicoesDesde1994(List<Contribuicao> contribuicoes) {
        double somaSalarios = 0.0;
        int totalMeses = 0;

        for (Contribuicao c : contribuicoes) {
            // Verificar se a contribuição deve ser considerada (se inicia após 1994)
            if (!c.getPeriodoInicio().isBefore(DATA_CORTE_1994)) {
                long meses = ChronoUnit.MONTHS.between(c.getPeriodoInicio(), c.getPeriodoFim());
                somaSalarios += c.getValorSalario() * meses;
                totalMeses += meses;
            }
        }

        return (totalMeses > 0) ? (somaSalarios / totalMeses) : 0.0;
    }

    /**
     * Calcula o fator previdenciário com base no tempo de contribuição e expectativa de vida.
     */
    private double calcularFatorPrevidenciario(int anosContribuidos, double expectativaVida) {
        double fator = (anosContribuidos * ALIQUOTA_FIXA) / expectativaVida;
        return Math.max(0.6, fator); // ✅ Mantém um mínimo de 60%
    }
}