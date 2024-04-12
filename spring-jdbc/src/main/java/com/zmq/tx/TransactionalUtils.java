package com.zmq.tx;

import jakarta.annotation.Nullable;

import java.sql.Connection;

/**
 * @author zmq
 */
public class TransactionalUtils {

    @Nullable
    public static Connection getCurrentConnection() {
        TransactionStatus ts = DataSourceTransactionManager.transactionStatus.get();
        return ts == null ? null : ts.connection;
    }
}
