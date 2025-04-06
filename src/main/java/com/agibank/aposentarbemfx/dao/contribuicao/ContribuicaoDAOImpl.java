package com.agibank.aposentarbemfx.dao.contribuicao;

import com.agibank.aposentarbemfx.model.Contribuicao;
import com.agibank.aposentarbemfx.utils.ConectarDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContribuicaoDAOImpl implements ContribuicaoDAO {

    private static final String INSERT_SQL = "INSERT INTO Contribuicao (idUsuario, valorSalario, periodoInicio, periodoFim) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM Contribuicao WHERE idContribuicao = ?";
    private static final String SELECT_BY_USER_SQL = "SELECT * FROM Contribuicao WHERE idUsuario = ?";
    private static final String UPDATE_SQL = "UPDATE Contribuicao SET valorSalario = ?, periodoInicio = ?, periodoFim = ? WHERE idContribuicao = ?";
    private static final String DELETE_SQL = "DELETE FROM Contribuicao WHERE idContribuicao = ?";

    public void adicionar(com.agibank.aposentarbemfx.model.Contribuicao contribuicao) {
        try (Connection conn = ConectarDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, contribuicao.getIdUsuario());
            stmt.setDouble(2, contribuicao.getValorSalario());
            stmt.setDate(3, Date.valueOf(contribuicao.getPeriodoInicio()));
            stmt.setDate(4, Date.valueOf(contribuicao.getPeriodoFim()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        contribuicao.setIdContribuicao(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir contribuição: " + e.getMessage(), e);
        }
    }

    public Contribuicao buscarPorId(int idContribuicao) {
        try (Connection conn = ConectarDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, idContribuicao);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return criarContribuicao(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar contribuição por ID: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Contribuicao> buscarPorUsuario(int idUsuario) {
        List<Contribuicao> contribuicoes = new ArrayList<>();
        try (Connection conn = ConectarDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER_SQL)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                contribuicoes.add(criarContribuicao(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar contribuições por usuário: " + e.getMessage(), e);
        }
        return contribuicoes;
    }

    public void atualizar(Contribuicao contribuicao) {
        try (Connection conn = ConectarDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setDouble(1, contribuicao.getValorSalario());
            stmt.setDate(2, Date.valueOf(contribuicao.getPeriodoInicio()));
            stmt.setDate(3, Date.valueOf(contribuicao.getPeriodoFim()));
            stmt.setInt(4, contribuicao.getIdContribuicao());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar contribuição: " + e.getMessage(), e);
        }
    }

    public void remover(int idContribuicao) {
        try (Connection conn = ConectarDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, idContribuicao);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover contribuição: " + e.getMessage(), e);
        }
    }

    private Contribuicao criarContribuicao(ResultSet rs) throws SQLException {
        return new Contribuicao(
                rs.getInt("idContribuicao"),
                rs.getInt("idUsuario"),
                rs.getDouble("valorSalario"),
                rs.getDate("periodoInicio").toLocalDate(),
                rs.getDate("periodoFim").toLocalDate()
        );
    }


}