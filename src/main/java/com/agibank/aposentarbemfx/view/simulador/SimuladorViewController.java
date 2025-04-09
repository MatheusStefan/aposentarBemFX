package com.agibank.aposentarbemfx.view.simulador;

import com.agibank.aposentarbemfx.Main;
import com.agibank.aposentarbemfx.model.Contribuicao;
import com.agibank.aposentarbemfx.model.Usuario;
import com.agibank.aposentarbemfx.service.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class SimuladorViewController {

    private Usuario usuario;
    private List<Contribuicao> contribuicoes;
    private ElegibilidadeService elegibilidadeService;
    private RegraAposReformaService regraAposReformaService;
    private RegraPedagio50 regraPedagio50;
    private RegraPedagio100 regraPedagio100;

    @FXML
    private Label lblNomeUsuario;

    @FXML
    private Label lblDataNascimento;

    @FXML
    private Label lblProfissao;

    @FXML
    private Label lblIdadeDesejada;

    @FXML
    private Label lblTotalContribuicoes;

    @FXML
    private Label lblIdadeMinima;

    @FXML
    private Label lblTempoContribuicao;

    @FXML
    private Label lblDataElegivel;

    @FXML
    private Label lblStatusPedagio50;

    @FXML
    private Label lblElegivelPedagio50;

    @FXML
    private Label lblValorPedagio50;

    @FXML
    private Label lblStatusPedagio100;

    @FXML
    private Label lblElegivelPedagio100;

    @FXML
    private Label lblValorPedagio100;

    @FXML
    private Label lblStatusPosReforma;

    @FXML
    private Label lblElegivelPosReforma;

    @FXML
    private Label lblValorPosReforma;
    @FXML
    private Label lblElegivelPontos;

    @FXML Label lblStatusPontos;
    @FXML
    private Label lblAnoAposentadoriaPontos;
    @FXML
    private Label lblValorEstimadoPontos;
    @FXML
    private Label lblEstimativaElegibilidadePontos;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");

    public SimuladorViewController() {
        this.elegibilidadeService = new ElegibilidadeService();
        this.regraAposReformaService = new RegraAposReformaService(elegibilidadeService);
        this.regraPedagio50 = new RegraPedagio50(elegibilidadeService);
        this.regraPedagio100 = new RegraPedagio100(elegibilidadeService);
    }

    public void setDados(Usuario usuario, List<Contribuicao> contribuicoes) {
        this.usuario = usuario;
        this.contribuicoes = contribuicoes;
        atualizarTela();
    }

    @FXML
    public void initialize() {
        // Esta função será chamada quando o FXML for carregado
        // A inicialização ocorrerá no método setDados
    }

    private void atualizarTela() {
        if (usuario == null) {
            showError("Usuário não encontrado. Por favor, volte e selecione um usuário.");
            return;
        }

        lblNomeUsuario.setText(usuario.getNome());
        lblDataNascimento.setText(usuario.getDataNascimento().format(dateFormatter));
        lblProfissao.setText(usuario.getProfissao().toString());
        lblIdadeDesejada.setText(String.valueOf(usuario.getIdadeAposentadoriaDesejada()));
        lblTotalContribuicoes.setText(String.valueOf(contribuicoes.size()));

        int idadeMinimaNecessaria = elegibilidadeService.calcularIdadeMinima(usuario);
        int tempoTotalContribuicao = elegibilidadeService.calcularTempoContribuicao(contribuicoes);
        LocalDate dataElegivel = elegibilidadeService.calcularDataElegivel(usuario, tempoTotalContribuicao);

        lblIdadeMinima.setText(idadeMinimaNecessaria + " anos");
        lblTempoContribuicao.setText(tempoTotalContribuicao + " anos");
        lblDataElegivel.setText(dataElegivel.format(yearFormatter));

        boolean elegivelPedagio50 = elegibilidadeService.isElegivelPedagio50(usuario, contribuicoes);
        lblStatusPedagio50.setText(elegivelPedagio50 ? "✅" : "❌");
        lblElegivelPedagio50.setText(elegivelPedagio50 ? "Elegível" : "Não elegível");

        if (elegivelPedagio50) {
            double valorPedagio50 = regraPedagio50.calcularAposentadoria(usuario, contribuicoes, 75.0);
            lblValorPedagio50.setText(String.format("Valor estimado: R$ %.2f", valorPedagio50));
        } else {
            lblValorPedagio50.setText("Não aplicável");
        }

        boolean elegivelPedagio100 = elegibilidadeService.isElegivelPedagio100(usuario, contribuicoes);
        lblStatusPedagio100.setText(elegivelPedagio100 ? "✅" : "❌");
        lblElegivelPedagio100.setText(elegivelPedagio100 ? "Elegível" : "Não elegível");

        if (elegivelPedagio100) {
            double valorPedagio100 = regraPedagio100.calcularAposentadoria(usuario, contribuicoes, usuario.getIdadeAposentadoriaDesejada());
            lblValorPedagio100.setText(String.format("Valor estimado: R$ %.2f", valorPedagio100));
        } else {
            lblValorPedagio100.setText("Não aplicável");
        }

        try {
            double valorAposReforma = regraAposReformaService.simularAposentadoria(usuario, contribuicoes);
            boolean elegivelPosReforma = valorAposReforma > 0;
            lblStatusPosReforma.setText(elegivelPosReforma ? "✅" : "❌");
            lblElegivelPosReforma.setText(elegivelPosReforma ? "Elegível" : "Não elegível");

            if (elegivelPosReforma) {
                lblValorPosReforma.setText(String.format("Valor estimado: R$ %.2f", valorAposReforma));
            } else {
                lblValorPosReforma.setText("Não aplicável");
            }
        } catch (Exception e) {
            lblStatusPosReforma.setText("❌");
            lblElegivelPosReforma.setText("Erro no cálculo");
            lblValorPosReforma.setText("Erro: " + e.getMessage());
        }

        calcularRegraPontos();
    }

    private void calcularRegraPontos() {
        RegraPontos regraPontos = new RegraPontos(elegibilidadeService);
        Map<String, Object> resultadoPontos = regraPontos.calcularRegraPontos(usuario, contribuicoes, usuario.getIdadeAposentadoriaDesejada());

        boolean elegivelPontos = (boolean) resultadoPontos.get("elegivel");
        if (elegivelPontos) {
            double valor = (double) resultadoPontos.get("valorEstimado");
            int ano = (int) resultadoPontos.get("anoElegivel");

            lblElegivelPontos.setText("Regra por Pontos: Elegível");
            lblStatusPontos.setText("✅");
            lblAnoAposentadoriaPontos.setText("📅 Ano da aposentadoria: " + ano);
            lblValorEstimadoPontos.setText("💰 Valor estimado: R$ " + String.format("%.2f", valor));
            lblEstimativaElegibilidadePontos.setText("");
        } else {
            int idadeEstimativa = (int) resultadoPontos.get("idadeElegivel");
            int anoEstimativa = (int) resultadoPontos.get("anoElegivel");
            double valorEstimado = (double) resultadoPontos.get("valorEstimado");

            lblElegivelPontos.setText("Regra por Pontos: Ainda não elegível");
            lblStatusPontos.setText("❌");
            lblEstimativaElegibilidadePontos.setText("📅 Estimativa de elegibilidade: Ano " + anoEstimativa + ", com " + idadeEstimativa + " anos de idade");
            lblValorEstimadoPontos.setText("💰 Valor estimado na data: R$ " + String.format("%.2f", valorEstimado));
            lblAnoAposentadoriaPontos.setText("");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}