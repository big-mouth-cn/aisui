package com.github.bigmouthcn.executor;

import java.util.function.Function;

/**
 * @author huxiao
 * @date 2024/3/11
 * @since 1.0.0
 */
public class MultiAccountingTransactionFunction implements Function<MultiAccountingTransactionRequest, Object> {
    @Override
    public Object apply(MultiAccountingTransactionRequest accountingTransaction) {
        return new MultiAccountingTransactionResponse().setSize(accountingTransaction.getAccountingTransactions().size());
    }
}
