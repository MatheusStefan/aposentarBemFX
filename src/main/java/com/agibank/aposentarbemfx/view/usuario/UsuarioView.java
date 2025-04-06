package com.agibank.aposentarbemfx.view.usuario;

import com.agibank.aposentarbemfx.model.Usuario;

import java.time.LocalDate;
import java.util.InputMismatchException;
import java.util.Scanner;

public class UsuarioView {
    private final Scanner scanner = new Scanner(System.in);
    private Usuario usuario;

    public Usuario coletarDadosUsuario() {
        String nome = coletarNome();
        String dataNascimento = coletarDataNascimento();
        Usuario.Genero genero = coletarGenero();
        Usuario.Profissao profissao = coletarProfissao();
        int idadeAposentadoriaDesejada = coletarIdadeAposentadoriaDesejada();

        return new Usuario(nome, dataNascimento, genero, profissao, idadeAposentadoriaDesejada);
    }

    private String coletarNome() {
        while (true) {
            System.out.println("\n===== Calculadora de Contribuição Previdenciária =====");
            System.out.print("Digite seu nome: ");
            String nome = scanner.nextLine().trim();
            if (Usuario.validarNome(nome)) {
                return nome;
            } else {
                System.out.println("Nome inválido. Tente novamente.");
            }
        }
    }

    private String coletarDataNascimento() {
        while (true) {
            System.out.print("Digite sua data de nascimento (DD/MM/AAAA): ");
            String dataNascimento = scanner.nextLine();

            try {
                LocalDate dataValidada = Usuario.validarData(dataNascimento);
                return dataNascimento;
            } catch (IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    private Usuario.Genero coletarGenero() {
        while (true) {
            System.out.print("Qual seu gênero? (Masculino/Feminino): ");
            String entrada = scanner.nextLine().trim();
            try {
                return Usuario.Genero.fromDescricao(entrada);
            } catch (IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage() + ". Tente novamente.");
            }
        }
    }

    private Usuario.Profissao coletarProfissao() {
        while (true) {
            System.out.print("Informe sua categoria (Geral, Professor, Rural): ");
            String entrada = scanner.nextLine().trim();
            try {
                return Usuario.Profissao.fromDescricao(entrada);
            } catch (IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage() + ". Tente novamente.");
            }
        }
    }

    private int coletarIdadeAposentadoriaDesejada() {
        while (true) {
            try {
                System.out.print("Digite a idade desejada para aposentadoria: ");
                int idadeAposentadoria = scanner.nextInt();
                scanner.nextLine();
                if (Usuario.validarIdadeAposentadoria(idadeAposentadoria)) {
                    return idadeAposentadoria;
                } else {
                    System.out.println("Idade de aposentadoria inválida. Tente novamente.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida! Digite apenas números inteiros.");
                scanner.next();
            }
        }
    }

    public void exibirMensagem(String mensagem) {
        System.out.println(mensagem);
    }
}