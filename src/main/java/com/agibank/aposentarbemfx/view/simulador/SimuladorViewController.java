package com.agibank.aposentarbemfx.view.simulador;

import com.agibank.aposentarbemfx.model.Contribuicao;
import com.agibank.aposentarbemfx.model.Usuario;
import com.agibank.aposentarbemfx.service.ElegibilidadeService;
import com.agibank.aposentarbemfx.service.RegraAposReformaService;
import com.agibank.aposentarbemfx.service.RegraPedagio50;
import com.agibank.aposentarbemfx.service.RegraPedagio100;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

    @FXML
    private Button btnVoltar;

    @FXML
    private Button btnGerarRelatorio;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

        // Atualizar dados do usuário
        lblNomeUsuario.setText(usuario.getNome());
        lblDataNascimento.setText(usuario.getDataNascimento().format(dateFormatter));
        lblProfissao.setText(usuario.getProfissao().toString());
        lblIdadeDesejada.setText(String.valueOf(usuario.getIdadeAposentadoriaDesejada()));
        lblTotalContribuicoes.setText(String.valueOf(contribuicoes.size()));

        // Calcular e exibir informações de elegibilidade
        int idadeMinimaNecessaria = elegibilidadeService.calcularIdadeMinima(usuario);
        int tempoTotalContribuicao = elegibilidadeService.calcularTempoContribuicao(contribuicoes);
        LocalDate dataElegivel = elegibilidadeService.calcularDataElegivel(usuario, tempoTotalContribuicao);

        lblIdadeMinima.setText(idadeMinimaNecessaria + " anos");
        lblTempoContribuicao.setText(tempoTotalContribuicao + " anos");
        lblDataElegivel.setText(dataElegivel.format(dateFormatter));

        // Verificar elegibilidade e calcular valor da aposentadoria pelo Pedágio 50%
        boolean elegivelPedagio50 = elegibilidadeService.isElegivelPedagio50(usuario, contribuicoes);
        lblStatusPedagio50.setText(elegivelPedagio50 ? "✅" : "❌");
        lblElegivelPedagio50.setText(elegivelPedagio50 ? "Elegível" : "Não elegível");

        if (elegivelPedagio50) {
            double valorPedagio50 = regraPedagio50.calcularAposentadoria(usuario, contribuicoes, 75.0);
            lblValorPedagio50.setText(String.format("Valor estimado: R$ %.2f", valorPedagio50));
        } else {
            lblValorPedagio50.setText("Não aplicável");
        }

        // Verificar elegibilidade e calcular valor da aposentadoria pelo Pedágio 100%
        boolean elegivelPedagio100 = elegibilidadeService.isElegivelPedagio100(usuario, contribuicoes);
        lblStatusPedagio100.setText(elegivelPedagio100 ? "✅" : "❌");
        lblElegivelPedagio100.setText(elegivelPedagio100 ? "Elegível" : "Não elegível");

        if (elegivelPedagio100) {
            double valorPedagio100 = regraPedagio100.calcularAposentadoria(usuario, contribuicoes, usuario.getIdadeAposentadoriaDesejada());
            lblValorPedagio100.setText(String.format("Valor estimado: R$ %.2f", valorPedagio100));
        } else {
            lblValorPedagio100.setText("Não aplicável");
        }

        // Verificar elegibilidade e calcular valor da aposentadoria pela Regra Pós-Reforma
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

    @FXML
    private void handleVoltar() {
        try {
            // Voltar para a tela de contribuição
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agibank/aposentarbemfx/ContribuicaoView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) btnVoltar.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Erro ao voltar para a tela anterior: " + e.getMessage());
        }
    }

    @FXML
    private void handleGerarRelatorio() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório da Simulação");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivos de Texto", "*.txt"));
        fileChooser.setInitialFileName("simulacao_aposentadoria_" +
                usuario.getNome().replaceAll("\\s+", "_") + ".txt");

        File file = fileChooser.showSaveDialog(btnGerarRelatorio.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("🔹 🔹 🔹 SIMULAÇÃO DE APOSENTADORIA 🔹 🔹 🔹");
                writer.println();
                writer.println("DADOS DO USUÁRIO");
                writer.println("Nome: " + usuario.getNome());
                writer.println("Data de Nascimento: " + usuario.getDataNascimento().format(dateFormatter));
                writer.println("Profissão: " + usuario.getProfissao());
                writer.println("Idade desejada para aposentadoria: " + usuario.getIdadeAposentadoriaDesejada());
                writer.println("Total de contribuições: " + contribuicoes.size());
                writer.println();

                writer.println("RESUMO DA ELEGIBILIDADE");
                writer.println("Idade mínima necessária: " + lblIdadeMinima.getText());
                writer.println("Tempo total de contribuição: " + lblTempoContribuicao.getText());
                writer.println("Data estimada para aposentadoria: " + lblDataElegivel.getText());
                writer.println();

                writer.println("REGRAS DE APOSENTADORIA");
                writer.println(lblStatusPedagio50.getText() + " Pedágio 50%: " + lblElegivelPedagio50.getText());
                writer.println("    " + lblValorPedagio50.getText());
                writer.println();

                writer.println(lblStatusPedagio100.getText() + " Pedágio 100%: " + lblElegivelPedagio100.getText());
                writer.println("    " + lblValorPedagio100.getText());
                writer.println();

                writer.println(lblStatusPosReforma.getText() + " Regra Pós-Reforma: " + lblElegivelPosReforma.getText());
                writer.println("    " + lblValorPosReforma.getText());
                writer.println();

                writer.println("🔹 🔹 🔹 FIM DA SIMULAÇÃO 🔹 🔹 🔹");
                writer.println("Relatório gerado em: " + LocalDate.now().format(dateFormatter));

                showInfo("Relatório salvo com sucesso em: " + file.getAbsolutePath());
            } catch (IOException e) {
                showError("Erro ao salvar o relatório: " + e.getMessage());
            }
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