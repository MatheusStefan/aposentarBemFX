package com.agibank.aposentarbemfx.view.simulador;

import com.agibank.aposentarbemfx.Main;
import com.agibank.aposentarbemfx.model.Contribuicao;
import com.agibank.aposentarbemfx.model.Usuario;
import com.agibank.aposentarbemfx.service.ElegibilidadeService;
import com.agibank.aposentarbemfx.service.RegraAposReformaService;
import com.agibank.aposentarbemfx.service.RegraPedagio50;
import com.agibank.aposentarbemfx.service.RegraPedagio100;
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