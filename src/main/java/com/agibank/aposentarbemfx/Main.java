package com.agibank.aposentarbemfx;

import com.agibank.aposentarbemfx.controller.ContribuicaoController;
import com.agibank.aposentarbemfx.controller.UsuarioController;
import com.agibank.aposentarbemfx.dao.contribuicao.ContribuicaoDAOImpl;
import com.agibank.aposentarbemfx.dao.usuario.UsuarioDAOImpl;
import com.agibank.aposentarbemfx.view.usuario.UsuarioView;
import com.agibank.aposentarbemfx.view.usuario.UsuarioViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        UsuarioDAOImpl usuarioDAO = new UsuarioDAOImpl();
        UsuarioView usuarioView = new UsuarioView();
        UsuarioController usuarioController = new UsuarioController(usuarioDAO, usuarioView);

        ContribuicaoDAOImpl contribuicaoDAO = new ContribuicaoDAOImpl();
        ContribuicaoController contribuicaoController = new ContribuicaoController(contribuicaoDAO);

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/agibank/aposentarbemfx/UsuarioView.fxml"));
        UsuarioViewController usuarioViewController = new UsuarioViewController(usuarioController);
        loader.setControllerFactory(param -> new UsuarioViewController(usuarioController));

        usuarioViewController.setContribuicaoController(contribuicaoController);

        Parent root = loader.load();
        primaryStage.setTitle("Calculadora de Contribuição Previdenciária");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
