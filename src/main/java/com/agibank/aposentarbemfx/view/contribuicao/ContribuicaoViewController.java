package com.agibank.aposentarbemfx.view.contribuicao;

import com.agibank.aposentarbemfx.Main;
import com.agibank.aposentarbemfx.controller.ContribuicaoController;
import com.agibank.aposentarbemfx.model.Contribuicao;
import com.agibank.aposentarbemfx.model.Usuario;
import com.agibank.aposentarbemfx.view.simulador.SimuladorView;
import com.agibank.aposentarbemfx.view.simulador.SimuladorViewController;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ContribuicaoViewController {

    @FXML
    private TextField valorSalarioField;
    @FXML
    private DatePicker periodoInicioPicker;
    @FXML
    private DatePicker periodoFimPicker;
    @FXML
    private Label mensagemLabel;
    @FXML
    private Button simularButton;
    @FXML
    private TableView<Contribuicao> contribuicoesTableView;
    @FXML
    private TableColumn<Contribuicao, Double> valorSalarioColumn;
    @FXML
    private TableColumn<Contribuicao, LocalDate> periodoInicioColumn;
    @FXML
    private TableColumn<Contribuicao, LocalDate> periodoFimColumn;

    private final ContribuicaoController contribuicaoController;
    private final Usuario usuario;
    private final ObservableList<Contribuicao> contribuicoes;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    public ContribuicaoViewController(ContribuicaoController contribuicaoController, Usuario usuario) {
        this.contribuicaoController = contribuicaoController;
        this.usuario = usuario;
        this.contribuicoes = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        valorSalarioColumn.setCellValueFactory(new PropertyValueFactory<>("valorSalario"));
        periodoInicioColumn.setCellValueFactory(new PropertyValueFactory<>("periodoInicio"));
        periodoFimColumn.setCellValueFactory(new PropertyValueFactory<>("periodoFim"));

        periodoInicioColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.format(dateFormatter));
                }
            }
        });

        periodoFimColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.format(dateFormatter));
                }
            }
        });

        valorSalarioColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(String.format("R$ %.2f", item));
                }
            }
        });

        contribuicoesTableView.setItems(contribuicoes);
        contribuicoes.addAll(contribuicaoController.buscarContribuicoesPorUsuario(usuario.getId()));
        simularButton.disableProperty().bind(Bindings.size(contribuicoes).lessThan(1));
    }

    @FXML
    private void handleSimular() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agibank/aposentarbemfx/SimuladorView.fxml"));
            Parent root = loader.load();

            SimuladorViewController simuladorController = loader.getController();
            List<Contribuicao> contribuicoes = contribuicaoController.buscarContribuicoesPorUsuario(usuario.getId());
            simuladorController.setDados(usuario, contribuicoes);

            Stage stage = (Stage) contribuicoesTableView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Simulação de Aposentadoria");
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao carregar a tela de simulação: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void handleAdicionarContribuicao() {
        try {
            double valorSalario = Double.parseDouble(valorSalarioField.getText());
            LocalDate periodoInicio = periodoInicioPicker.getValue();
            LocalDate periodoFim = periodoFimPicker.getValue();

            contribuicaoController.adicionarContribuicao(usuario.getId(), valorSalario, periodoInicio, periodoFim);
            Contribuicao novaContribuicao = new Contribuicao(usuario.getId(), valorSalario, periodoInicio, periodoFim);
            contribuicoes.add(novaContribuicao);
            mensagemLabel.setText("Contribuição adicionada com sucesso!");

            // Clear the input fields
            limparCampos();
        } catch (Exception e) {
            mensagemLabel.setText("Erro: " + e.getMessage());
        }
    }

    private void limparCampos() {
        valorSalarioField.clear();
        periodoInicioPicker.setValue(null);
        periodoFimPicker.setValue(null);
    }

    public void handleIrParaSimulacao() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/agibank/aposentarbemfx/SimuladorView.fxml"));
        loader.setControllerFactory(param -> new SimuladorViewController());

        Parent simulacaoRoot = loader.load();
        SimuladorViewController simuladorController = loader.getController();
        simuladorController.setDados(usuario, contribuicoes);

        Scene simulacaoScene = new Scene(simulacaoRoot);

        // Get the current stage
        Stage stage = (Stage) ((Node) mensagemLabel).getScene().getWindow();
        stage.setScene(simulacaoScene);
        stage.show();
    }
}
