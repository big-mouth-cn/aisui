package com.github.bigmouthcn.executor;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

/**
 * @author huxiao
 * @date 2024/3/11
 * @since 1.0.0
 */
@Data
public class SqlExecutorRequest {

    @JsonPropertyDescription("执行类型。可选值：SELECT / UPDATE / INSERT / DELETE")
    private ExecuteType executeType;

    @JsonPropertyDescription("执行的sql")
    private String sql;

    public enum ExecuteType{

        /**
         * 查询
         */
        SELECT,
        /**
         * 更新
         */
        UPDATE,
        /**
         * 插入
         */
        INSERT,
        /**
         * 删除
         */
        DELETE
    }
}
