package org.example.expert.domain.user.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.user.entity.User;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    public void bulkInsert(List<User> users) {
        String sql = """
                INSERT INTO users (email, password, nickname, user_role, created_at, modified_at)
                VALUES (?, ?, ?, ?, now(), now())
            """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                User user = users.get(i);
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getNickname());
                ps.setString(4, user.getUserRole().name());
            }

            @Override
            public int getBatchSize() {
                return users.size();
            }
        });
    }
}
