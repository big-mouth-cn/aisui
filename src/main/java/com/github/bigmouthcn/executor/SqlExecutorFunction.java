package com.github.bigmouthcn.executor;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author huxiao
 * @date 2024/3/11
 * @since 1.0.0
 */
public class SqlExecutorFunction implements Function<SqlExecutorRequest, Object> {

    @Override
    public Object apply(SqlExecutorRequest sqlExecutorRequest) {
        String url = "";
        String user = "";
        String password = "";

        SqlExecutorResponse sqlExecutorResponse = new SqlExecutorResponse();
        if (SqlExecutorRequest.ExecuteType.SELECT == sqlExecutorRequest.getExecuteType()) {
            List<Map<String, Object>> result = new ArrayList<>();
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sqlExecutorRequest.getSql())) {

                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object columnValue = resultSet.getObject(i);
                        row.put(columnName, columnValue);
                    }
                    result.add(row);
                }
                sqlExecutorResponse.setQueryResult(result);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                try (Connection connection = DriverManager.getConnection(url, user, password);
                     Statement statement = connection.createStatement()) {
                    int rows = statement.executeUpdate(sqlExecutorRequest.getSql());
                    sqlExecutorResponse.setUpdateRows(rows);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return sqlExecutorResponse;
    }
}
