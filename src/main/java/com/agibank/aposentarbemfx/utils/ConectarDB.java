package com.agibank.aposentarbemfx.utils;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConectarDB {

    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        Dotenv dotenv = Dotenv.load();
        String port = dotenv.get("DATABASE_PORT", "3306"); // Porta padrão caso não esteja no .env
        String baseUrl = dotenv.get("DATABASE_URL");
        USERNAME = dotenv.get("DATABASE_USERNAME");
        PASSWORD = dotenv.get("DATABASE_PASSWORD");

        if (baseUrl == null || USERNAME == null || PASSWORD == null) {
            throw new RuntimeException("Erro: Variáveis de ambiente do banco de dados não foram carregadas corretamente.");
        }

        URL = "jdbc:mysql://" + baseUrl + ":" + port + "/aposentarBem";
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar ao banco de dados: " + e.getMessage(), e);
        }
    }
}