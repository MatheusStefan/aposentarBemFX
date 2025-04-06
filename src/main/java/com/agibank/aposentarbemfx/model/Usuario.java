package com.agibank.aposentarbemfx.model;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Usuario {
    private int id;
    private String nome;
    private LocalDate dataNascimento;
    private int idade;
    private Profissao profissao;
    private Genero genero;
    private int idadeAposentadoriaDesejada;

    public Usuario(String nome, String dataNascimento,
                   Genero genero, Profissao profissao,
                   int idadeAposentadoriaDesejada) {
        this.nome = nome;
        this.genero = genero;
        this.profissao = profissao;
        this.idadeAposentadoriaDesejada = idadeAposentadoriaDesejada;
        setDataNascimento(dataNascimento);
    }

    //Enum Profissão
    public enum Profissao {
        GERAL("Geral"),
        PROFESSOR("Professor"),
        RURAL("Rural");
        private final String descricao;

        Profissao(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }

        // Método para converter de String para Enum
        public static Profissao fromDescricao(String descricao) {
            for (Profissao profissao : Profissao.values()) {
                if (profissao.getDescricao().equalsIgnoreCase(descricao)) {
                    return profissao;
                }
            }
            throw new IllegalArgumentException("Profissão inválida: " + descricao);
        }
    }

    //Enum Genero
    public enum Genero {
        MASCULINO("Masculino"),
        FEMININO("Feminino");

        private final String descricao;

        Genero(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }

        // Método para converter de String para Enum
        public static Genero fromDescricao(String descricao) {
            for (Genero genero : Genero.values()) {
                if (genero.getDescricao().equalsIgnoreCase(descricao)) {
                    return genero;
                }
            }
            throw new IllegalArgumentException("Gênero inválido: " + descricao);
        }
    }

    //Validar Data de nascimento

    public static LocalDate validarData(String dataNascimentoStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            LocalDate dataConvertida = LocalDate.parse(dataNascimentoStr, formatter);
            LocalDate dataAtual = LocalDate.now();

            if (dataConvertida.isAfter(dataAtual)) {
                throw new IllegalArgumentException("A data de nascimento não pode ser no futuro.");
            }
            int idade = calcularIdade(dataConvertida);
            if (idade < 15) {
                throw new IllegalArgumentException("A idade mínima permitida para simulação é 15 anos.");
            }
            return dataConvertida;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data inválido! Use o formato DD/MM/AAAA.");
        }
    }

    public static int calcularIdade(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new IllegalStateException("Data de nascimento não foi definida.");
        }
        LocalDate dataAtual = LocalDate.now();
        return Period.between(dataNascimento, dataAtual).getYears();
    }

    //Validar Nome
    public static boolean validarNome(String nome) {
        return !nome.isEmpty() && nome.matches("[a-zA-ZÀ-ÿ\\s]+");
    }

    //Validar Aposentadoria
    public static boolean validarIdadeAposentadoria(int idadeAposentadoria) {
        int idadeMinima = 40;
        return idadeAposentadoria >= idadeMinima && idadeAposentadoria < 90;
    }


    //Getters e Setter
    //Nome
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    //Data Nascimento
    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimentoStr) {
        this.dataNascimento = validarData(dataNascimentoStr);
    }

    //Genero
    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    //Profissão
    public Profissao getProfissao() {
        return profissao;
    }

    public void setProfissao(Profissao profissao) {
        this.profissao = profissao;
    }

    //Idade
    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    //Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    //Aposentadoria Desejada
    public int getIdadeAposentadoriaDesejada() {
        return idadeAposentadoriaDesejada;
    }

    public void setIdadeAposentadoriaDesejada(int idadeAposentadoriaDesejada) {
        this.idadeAposentadoriaDesejada = idadeAposentadoriaDesejada;
    }
}