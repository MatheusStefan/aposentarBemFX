package com.agibank.aposentarbemfx.view.simulador;

import com.agibank.aposentarbemfx.model.Contribuicao;
import com.agibank.aposentarbemfx.model.Usuario;
import com.agibank.aposentarbemfx.service.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class SimuladorView {

    private final ElegibilidadeService elegibilidadeService;
    private final RegraAposReformaService regraAposReformaService;
    private final RegraPedagio50 regraPedagio50;
    private final RegraPedagio100 regraPedagio100;

    public SimuladorView() {
        this.elegibilidadeService = new ElegibilidadeService();
        this.regraAposReformaService = new RegraAposReformaService(elegibilidadeService); // 🔧 Corrigido
        this.regraPedagio50 = new RegraPedagio50(elegibilidadeService);
        this.regraPedagio100 = new RegraPedagio100(elegibilidadeService);
    }

    public void exibirSimulacao(Usuario usuario, List<Contribuicao> contribuicoes) {
        System.out.println("\n🔹 🔹 🔹 Simulação de Aposentadoria 🔹 🔹 🔹\n");
        System.out.println("👤 Nome: " + usuario.getNome());
        System.out.println("📅 Data de Nascimento: " + usuario.getDataNascimento());
        System.out.println("👷 Profissão: " + usuario.getProfissao());
        System.out.println("🎯 Idade desejada para aposentadoria: " + usuario.getIdadeAposentadoriaDesejada());
        System.out.println("📊 Total de contribuições registradas: " + contribuicoes.size() + "\n");

        // 🔹🔹🔹 NOVAS INFORMAÇÕES 🔹🔹🔹
        int idadeMinimaNecessaria = elegibilidadeService.calcularIdadeMinima(usuario);
        int tempoTotalContribuicao = elegibilidadeService.calcularTempoContribuicao(contribuicoes);
        LocalDate dataElegivel = elegibilidadeService.calcularDataElegivel(usuario, tempoTotalContribuicao);

        System.out.println("📌 Idade mínima necessária para aposentadoria: " + idadeMinimaNecessaria + " anos");
        System.out.println("⏳ Tempo total de contribuição: " + tempoTotalContribuicao + " anos");
        System.out.println("📆 Data estimada para aposentadoria: " + dataElegivel + "\n");

        // 🟢 Regra do Pedágio 50%
        if (elegibilidadeService.isElegivelPedagio50(usuario, contribuicoes)) {
            double valorPedagio50 = regraPedagio50.calcularAposentadoria(usuario, contribuicoes, 75.0);
            System.out.printf("✅ Pedágio 50%%: Elegível\n   💰 Valor estimado: R$ %.2f\n\n", valorPedagio50);
        } else {
            System.out.println("❌ Pedágio 50%: Não elegível\n");
        }

        // 🟢 Regra do Pedágio 100%
        if (elegibilidadeService.isElegivelPedagio100(usuario, contribuicoes)) {
            double valorPedagio100 = regraPedagio100.calcularAposentadoria(usuario, contribuicoes, usuario.getIdadeAposentadoriaDesejada());
            System.out.printf("✅ Pedágio 100%%: Elegível\n   💰 Valor estimado: R$ %.2f\n\n", valorPedagio100);
        } else {
            System.out.println("❌ Pedágio 100%: Não elegível\n");
        }

        // 🟢 Regra Pós-Reforma
        try {
            double valorAposReforma = regraAposReformaService.simularAposentadoria(usuario, contribuicoes);
            if (valorAposReforma > 0) {
                System.out.printf("✅ Regra Pós-Reforma: Elegível\n   💰 Valor estimado: R$ %.2f\n\n", valorAposReforma);
            } else {
                System.out.println("❌ Regra Pós-Reforma: Não elegível\n");
            }
        } catch (Exception e) {
            System.out.println("❌ Erro ao calcular Regra Pós-Reforma: " + e.getMessage());
        }

        RegraPontos regraPontos = new RegraPontos(elegibilidadeService);
        Map<String, Object> resultadoPontos = regraPontos.calcularRegraPontos(usuario, contribuicoes, usuario.getIdadeAposentadoriaDesejada());

        boolean elegivelPontos = (boolean) resultadoPontos.get("elegivel");
        if (elegivelPontos) {
            double valor = (double) resultadoPontos.get("valorEstimado");
            int ano = (int) resultadoPontos.get("anoElegivel");

            System.out.printf("✅ Regra por Pontos: Elegível\n   📅 Ano da aposentadoria: %d\n   💰 Valor estimado: R$ %.2f\n\n", ano, valor);
        } else {
            int idadeEstimativa = (int) resultadoPontos.get("idadeElegivel");
            int anoEstimativa = (int) resultadoPontos.get("anoElegivel");
            double valorEstimado = (double) resultadoPontos.get("valorEstimado");

            System.out.printf("❌ Regra por Pontos: Ainda não elegível\n");
            System.out.printf("   📅 Estimativa de elegibilidade: Ano %d, com %d anos de idade\n", anoEstimativa, idadeEstimativa);
            System.out.printf("   💰 Valor estimado na data: R$ %.2f\n\n", valorEstimado);
        }

        System.out.println("🔹 🔹 🔹 Fim da Simulação 🔹 🔹 🔹");
    }
}