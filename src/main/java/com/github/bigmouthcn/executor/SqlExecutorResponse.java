package com.github.bigmouthcn.executor;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author huxiao
 * @date 2024/3/11
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class SqlExecutorResponse {

    private int updateRows;

    private List<Map<String, Object>> queryResult;
}
