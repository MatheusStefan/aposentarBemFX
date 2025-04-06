package com.agibank.aposentarbemfx.service;

import com.agibank.aposentarbemfx.model.Contribuicao;
import com.agibank.aposentarbemfx.model.Usuario;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ElegibilidadeService {

    public static final LocalDate DATA_REFORMA = LocalDate.of(2019, 11, 13);

    // 🚩 Regra Pedágio 50%
    public boolean isElegivelPedagio50(Usuario usuario, List<Contribuicao> contribuicoes) {
        if (!contribuiuAntesDaReforma(contribuicoes)) {
            return false;
        }

        int minimoContribuicaoAntiga = (usuario.getGenero() == Usuario.Genero.FEMININO) ? 30 * 12 : 35 * 12;
        int mesesContribuidosAteReforma = calcularMesesContribuidosAteData(contribuicoes, DATA_REFORMA);
        int mesesFaltando = minimoContribuicaoAntiga - mesesContribuidosAteReforma;

        return mesesFaltando > 0 && mesesFaltando <= 24;
    }

    // 🚩 Regra Pedágio 100%
    public boolean isElegivelPedagio100(Usuario usuario, List<Contribuicao> contribuicoes) {
        int idadeNaReforma = calcularIdadeEmData(usuario, DATA_REFORMA);

        int idadeMinima;
        if (usuario.getProfissao() == Usuario.Profissao.PROFESSOR) {
            idadeMinima = (usuario.getGenero() == Usuario.Genero.FEMININO) ? 52 : 55;
        } else {
            idadeMinima = (usuario.getGenero() == Usuario.Genero.FEMININO) ? 57 : 60;
        }

        if (idadeNaReforma < idadeMinima) return false;

        int minimoContribuicaoAntiga = switch (usuario.getProfissao()) {
            case PROFESSOR -> (usuario.getGenero() == Usuario.Genero.FEMININO) ? 25 * 12 : 30 * 12;
            default -> (usuario.getGenero() == Usuario.Genero.FEMININO) ? 30 * 12 : 35 * 12;
        };

        int mesesContribuidosAteReforma = calcularMesesContribuidosAteData(contribuicoes, DATA_REFORMA);

        return mesesContribuidosAteReforma < minimoContribuicaoAntiga;
    }

    // 🛠️ Regra de Pontos com Progressão
    public boolean isElegivelPontos(Usuario usuario, List<Contribuicao> contribuicoes, int idadeAposentadoriaDesejada) {
        int tempoContribuicaoAnos = calcularTempoContribuicao(contribuicoes);
        int anoNascimento = usuario.getDataNascimento().getYear();
        int anoDesejado = anoNascimento + idadeAposentadoriaDesejada;

        int pontosProjetados = idadeAposentadoriaDesejada + tempoContribuicaoAnos;
        int pontosMinimos = getPontuacaoMinima(usuario.getGenero(), anoDesejado);

        System.out.println("🔍 [Elegibilidade Pontos] Ano desejado: " + anoDesejado);
        System.out.println("🔢 Pontos projetados: " + pontosProjetados + " | Pontuação mínima exigida: " + pontosMinimos);

        return pontosProjetados >= pontosMinimos;
    }

    // 📌 Tabela de pontos progressiva da Reforma
    private int getPontuacaoMinima(Usuario.Genero genero, int anoAposentadoria) {
        int anoBase = 2020;
        int pontosBase = (genero == Usuario.Genero.MASCULINO) ? 97 : 87;
        int pontosMax = (genero == Usuario.Genero.MASCULINO) ? 105 : 100;

        int incremento = Math.max(0, anoAposentadoria - anoBase);
        return Math.min(pontosBase + incremento, pontosMax);
    }

    // 🧮 Utils

    public boolean contribuiuAntesDaReforma(List<Contribuicao> contribuicoes) {
        return contribuicoes.stream()
                .anyMatch(c -> c.getPeriodoInicio().isBefore(DATA_REFORMA) || c.getPeriodoFim().isBefore(DATA_REFORMA));
    }

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

    public int calcularIdadeEmData(Usuario usuario, LocalDate dataReferencia) {
        if (usuario.getDataNascimento() == null || dataReferencia == null) {
            throw new IllegalArgumentException("Data de nascimento ou data de referência não podem ser nulas.");
        }
        return Period.between(usuario.getDataNascimento(), dataReferencia).getYears();
    }

    public int calcularIdadeMinima(Usuario usuario) {
        return (usuario.getGenero() == Usuario.Genero.MASCULINO) ? 65 : 62;
    }

    // ✅ AQUI ESTÁ O NOVO MÉTODO!
    public int calcularTempoContribuicao(List<Contribuicao> contribuicoes) {
        int totalMeses = 0;
        for (Contribuicao c : contribuicoes) {
            LocalDate inicio = c.getPeriodoInicio();
            LocalDate fim = c.getPeriodoFim();
            if (inicio != null && fim != null && !fim.isBefore(inicio)) {
                totalMeses += (int) ChronoUnit.MONTHS.between(inicio, fim);
            }
        }
        return totalMeses / 12; // Retorna em anos completos
    }

    public LocalDate calcularDataElegivel(Usuario usuario, int tempoContribuido) {
        int idadeAtual = Period.between(usuario.getDataNascimento(), LocalDate.now()).getYears();
        int idadeMinima = calcularIdadeMinima(usuario);
        int anosFaltando = Math.max(idadeMinima - idadeAtual, 0);
        return LocalDate.now().plusYears(anosFaltando);
    }
}