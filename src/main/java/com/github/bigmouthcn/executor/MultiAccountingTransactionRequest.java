package com.github.bigmouthcn.executor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author huxiao
 * @date 2024/3/11
 * @since 1.0.0
 */
@Data
public class MultiAccountingTransactionRequest {

    @JsonPropertyDescription("多个交易对象")
    private List<AccountingTransaction> accountingTransactions;

    @Data
    public static class AccountingTransaction {

        @JsonPropertyDescription("#ID，一般只有更新和删除时会有值。")
        private Long id;
        @JsonPropertyDescription("交易类型。1：收入，2：支出")
        private Integer type = 2;
        @JsonPropertyDescription("交易分类")
        private String classification;
        @JsonPropertyDescription("交易账户")
        private String account;
        @JsonPropertyDescription("交易金额")
        private BigDecimal amount;
        @JsonPropertyDescription("交易备注信息")
        private Object description;
        @JsonPropertyDescription("交易时间，时间格式要求：yyyy-MM-dd HH:mm:ss")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime createTime = LocalDateTime.now();
    }
}
