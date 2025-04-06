package com.agibank.aposentarbemfx.model;
import java.time.LocalDate;
import java.util.Objects;

public class Contribuicao {
    private int idContribuicao;
    private int idUsuario;
    private double valorSalario;
    private LocalDate periodoInicio;
    private LocalDate periodoFim;

    // Construtor sem idContribuicao (usado ao inserir no banco)
    public Contribuicao(int idUsuario, double valorSalario, LocalDate periodoInicio, LocalDate periodoFim) {
        validarValores(valorSalario, periodoInicio, periodoFim);
        this.idUsuario = idUsuario;
        this.valorSalario = valorSalario;
        this.periodoInicio = periodoInicio;
        this.periodoFim = periodoFim;
    }

    // Construtor completo (para quando já há um idContribuicao)
    public Contribuicao(int idContribuicao, int idUsuario, double valorSalario, LocalDate periodoInicio, LocalDate periodoFim) {
        this(idUsuario, valorSalario, periodoInicio, periodoFim);
        this.idContribuicao = idContribuicao;
    }

    private void validarValores(double valorSalario, LocalDate periodoInicio, LocalDate periodoFim) {
        if (valorSalario <= 0) {
            throw new IllegalArgumentException("O valor do salário deve ser positivo.");
        }
        if (periodoInicio.isAfter(periodoFim)) {
            throw new IllegalArgumentException("O período de início deve ser antes do período de fim.");
        }
    }

    public int getIdContribuicao() {
        return idContribuicao;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public double getValorSalario() {
        return valorSalario;
    }

    public void setValorSalario(double valorSalario) {
        if (valorSalario <= 0) {
            throw new IllegalArgumentException("O valor do salário deve ser positivo.");
        }
        this.valorSalario = valorSalario;
    }

    public LocalDate getPeriodoInicio() {
        return periodoInicio;
    }

    public void setPeriodoInicio(LocalDate periodoInicio) {
        if (periodoInicio.isAfter(this.periodoFim)) {
            throw new IllegalArgumentException("O período de início deve ser antes do período de fim.");
        }
        this.periodoInicio = periodoInicio;
    }

    public LocalDate getPeriodoFim() {
        return periodoFim;
    }

    public void setPeriodoFim(LocalDate periodoFim) {
        if (this.periodoInicio.isAfter(periodoFim)) {
            throw new IllegalArgumentException("O período de fim deve ser após o período de início.");
        }
        this.periodoFim = periodoFim;
    }

    public void setIdContribuicao(int idContribuicao) {
        this.idContribuicao = idContribuicao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contribuicao that = (Contribuicao) o;
        return idContribuicao == that.idContribuicao && idUsuario == that.idUsuario &&
                Double.compare(that.valorSalario, valorSalario) == 0 &&
                Objects.equals(periodoInicio, that.periodoInicio) && Objects.equals(periodoFim, that.periodoFim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idContribuicao, idUsuario, valorSalario, periodoInicio, periodoFim);
    }

    @Override
    public String toString() {
        return "Contribuicao{" +
                "idContribuicao=" + idContribuicao +
                ", idUsuario=" + idUsuario +
                ", valorSalario=" + valorSalario +
                ", periodoInicio=" + periodoInicio +
                ", periodoFim=" + periodoFim +
                '}';
    }
}
