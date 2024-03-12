package com.github.bigmouthcn.executor;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 更新响应
 *
 * @author huxiao
 * @date 2024/3/11
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MultiAccountingTransactionResponse {
    private int size;
}
