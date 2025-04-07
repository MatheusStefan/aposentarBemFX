package com.agibank.aposentarbemfx.view.usuario;

import com.agibank.aposentarbemfx.Main;
import com.agibank.aposentarbemfx.controller.ContribuicaoController;
import com.agibank.aposentarbemfx.controller.UsuarioController;
import com.agibank.aposentarbemfx.dao.contribuicao.ContribuicaoDAOImpl;
import com.agibank.aposentarbemfx.model.Usuario;
import com.agibank.aposentarbemfx.view.contribuicao.ContribuicaoViewController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class UsuarioViewController {

    @FXML
    private TextField nomeField;
    @FXML
    private TextField dataNascimentoField;
    @FXML
    private TextField generoField;
    @FXML
    private TextField profissaoField;
    @FXML
    private TextField idadeAposentadoriaField;
    @FXML
    private Label mensagemLabel;

    private final UsuarioController usuarioController;
    private ContribuicaoController contribuicaoController;


    public UsuarioViewController(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
    }

    public void setContribuicaoController(ContribuicaoController contribuicaoController) {
        this.contribuicaoController = contribuicaoController;
    }

    public void handleCalcular() {
        try {
            String nome = nomeField.getText().trim();
            String dataNascimento = dataNascimentoField.getText().trim();
            Usuario.Genero genero = Usuario.Genero.fromDescricao(generoField.getText().trim());
            Usuario.Profissao profissao = Usuario.Profissao.fromDescricao(profissaoField.getText().trim());
            int idadeAposentadoriaDesejada = Integer.parseInt(idadeAposentadoriaField.getText().trim());

            Usuario usuario = new Usuario(nome, dataNascimento, genero, profissao, idadeAposentadoriaDesejada);

            // Use the controller to create the user
            usuarioController.criarUsuario(usuario);
            mensagemLabel.setText("Usuário cadastrado com sucesso!");
            navigateToContribuicaoScreen();
        } catch (Exception e) {
            mensagemLabel.setText("Erro: " + e.getMessage());
        }
    }

    private void navigateToContribuicaoScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/agibank/aposentarbemfx/ContribuicaoView.fxml"));
        loader.setControllerFactory(param -> new ContribuicaoViewController(new ContribuicaoController(new ContribuicaoDAOImpl()), usuarioController.getUsuario()));

        Parent contribuicaoRoot = loader.load();
        Scene contribuicaoScene = new Scene(contribuicaoRoot);

        // Get the current stage
        Stage stage = (Stage) ((Node) mensagemLabel).getScene().getWindow();
        stage.setScene(contribuicaoScene);
        stage.show();
    }

    public void exibirMensagem(String mensagem) {
        mensagemLabel.setText(mensagem);
    }
}
