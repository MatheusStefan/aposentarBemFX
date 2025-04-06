package com.agibank.aposentarbemfx.view.contribuicao;

import com.agibank.aposentarbemfx.controller.ContribuicaoController;
import com.agibank.aposentarbemfx.model.Contribuicao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class ContribuicaoView {

    private final ContribuicaoController contribuicaoController;
    private final Scanner scanner;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final List<Contribuicao> contribuiçõesTemporarias = new ArrayList<>();

    public ContribuicaoView(ContribuicaoController contribuicaoController) {
        this.contribuicaoController = contribuicaoController;
        this.scanner = new Scanner(System.in);
    }

    public void iniciarCadastroContribuicoes(int idUsuario) {
        System.out.println("\n=== Cadastro de Contribuições ===");
        System.out.println("💡 Uma contribuição representa um período em que você trabalhou e contribuiu para a previdência.");
        System.out.println("⚠️ As contribuições devem ser inseridas **em ordem cronológica**, da mais antiga para a mais recente.");
        System.out.println("📌 Informe as contribuições uma por uma. No final, você poderá revisar e corrigir os dados antes de salvar.");

        while (true) {
            LocalDate periodoInicio = solicitarData("Informe a data de início da contribuição (DD/MM/AAAA): ");
            LocalDate periodoFim = solicitarData("Informe a data de fim da contribuição (DD/MM/AAAA): ");
            double valorSalario = solicitarValorSalario();

            // Validações antes de adicionar a contribuição
            if (!validarOrdemDatas(periodoInicio, periodoFim)) continue;
            if (!validarDataFutura(periodoInicio)) continue;
            //if (!validarIdadeMinima(periodoInicio)) continue;
            if (!validarDuplicacaoPeriodo(periodoInicio, periodoFim)) continue;
            if (!validarOrdemCronologica(periodoInicio)) continue;

            // Adicionar contribuição temporária
            Contribuicao contribuicao = new Contribuicao(idUsuario, valorSalario, periodoInicio, periodoFim);
            contribuiçõesTemporarias.add(contribuicao);

            System.out.print("Deseja adicionar outra contribuição? (S/N): ");
            String resposta = scanner.next().trim().toUpperCase();
            if (!resposta.equals("S")) break;
        }

        // Exibir contribuições antes do envio
        exibirContribuicoes();

        // Permitir edição antes do envio
        revisarContribuicoes();

        // Enviar para o banco
        salvarContribuicoes();
    }

    private void exibirContribuicoes() {
        System.out.println("\n📌 Lista de Contribuições Registradas (Ordem Cronológica):");
        contribuiçõesTemporarias.sort(Comparator.comparing(Contribuicao::getPeriodoInicio));
        for (int i = 0; i < contribuiçõesTemporarias.size(); i++) {
            Contribuicao c = contribuiçõesTemporarias.get(i);
            System.out.printf("%d - Início: %s | Fim: %s | Salário: R$%.2f\n",
                    i + 1, c.getPeriodoInicio().format(formatter), c.getPeriodoFim().format(formatter), c.getValorSalario());
        }
    }

    private void revisarContribuicoes() {
        while (true) {
            System.out.print("\nDeseja alterar alguma contribuição? (S/N): ");
            String resposta = scanner.next().trim().toUpperCase();
            if (!resposta.equals("S")) break;

            System.out.print("Digite o número da contribuição que deseja alterar: ");
            int escolha = scanner.nextInt() - 1;

            if (escolha < 0 || escolha >= contribuiçõesTemporarias.size()) {
                System.out.println("❌ Número inválido! Tente novamente.");
                continue;
            }

            System.out.println("🔄 Alterando a Contribuição #" + (escolha + 1));
            LocalDate novoInicio = solicitarData("Nova data de início (DD/MM/AAAA): ");
            LocalDate novoFim = solicitarData("Nova data de fim (DD/MM/AAAA): ");
            double novoSalario = solicitarValorSalario();

            if (!validarOrdemDatas(novoInicio, novoFim)) continue;
            if (!validarDataFutura(novoInicio)) continue;

            // Atualizar a contribuição
            Contribuicao c = contribuiçõesTemporarias.get(escolha);
            c.setPeriodoInicio(novoInicio);
            c.setPeriodoFim(novoFim);
            c.setValorSalario(novoSalario);

            System.out.println("✅ Contribuição atualizada com sucesso!");
            exibirContribuicoes();
        }
    }

    private void salvarContribuicoes() {
        System.out.print("\nDeseja salvar as contribuições ? (S/N): ");
        String resposta = scanner.next().trim().toUpperCase();
        if (resposta.equals("S")) {
            for (Contribuicao c : contribuiçõesTemporarias) {
                contribuicaoController.adicionarContribuicao(
                        c.getIdUsuario(), c.getValorSalario(), c.getPeriodoInicio(), c.getPeriodoFim());
            }
            System.out.println("✅ Todas as contribuições foram salvas com sucesso!");
        } else {
            System.out.println("⚠️ As contribuições **NÃO** foram salvas.");
        }
    }

    // Métodos auxiliares

    private double solicitarValorSalario() {
        System.out.print("Informe o valor do salário: ");
        double salario = scanner.nextDouble();
        if (salario <= 0) {
            System.out.println("❌ O salário deve ser maior que zero!");
            return solicitarValorSalario();
        }
        return salario;
    }

    private LocalDate solicitarData(String mensagem) {
        System.out.print(mensagem);
        String dataStr = scanner.next();
        try {
            return LocalDate.parse(dataStr, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("❌ Formato de data inválido! Use DD/MM/AAAA.");
            return solicitarData(mensagem);
        }
    }

    private boolean validarOrdemDatas(LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            System.out.println("❌ A data de início não pode ser depois da data de fim.");
            return false;
        }
        return true;
    }

    private boolean validarDataFutura(LocalDate inicio) {
        if (inicio.isAfter(LocalDate.now())) {
            System.out.println("❌ A data de início não pode estar no futuro.");
            return false;
        }
        return true;
    }

//    private boolean validarIdadeMinima(LocalDate inicio, int idUsuario) {
//        Usuario usuario = usuarioController.buscarUsuarioPorId(idUsuario);
//        int idadeUsuario = usuario.getIdade(); // Agora pegamos a idade real do usuário
//
//        if (idadeUsuario < 16) {
//            System.out.println("❌ O contribuinte deve ter pelo menos 16 anos para contribuir.");
//            return false;
//        }
//        return true;
//    }

    private boolean validarDuplicacaoPeriodo(LocalDate inicio, LocalDate fim) {
        for (Contribuicao c : contribuiçõesTemporarias) {
            if (!(fim.isBefore(c.getPeriodoInicio()) || inicio.isAfter(c.getPeriodoFim()))) {
                System.out.println("❌ O período informado se sobrepõe a um já cadastrado.");
                return false;
            }
        }
        return true;
    }

    private boolean validarOrdemCronologica(LocalDate novaData) {
        if (!contribuiçõesTemporarias.isEmpty()) {
            LocalDate ultimaData = contribuiçõesTemporarias.get(contribuiçõesTemporarias.size() - 1).getPeriodoInicio();
            if (novaData.isBefore(ultimaData)) {
                System.out.println("❌ A nova contribuição deve ser posterior à última adicionada.");
                return false;
            }
        }
        return true;
    }
}