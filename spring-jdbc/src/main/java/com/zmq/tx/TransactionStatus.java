package com.zmq.tx;

import java.sql.Connection;

/**
 * @author zmq
 */
public class TransactionStatus {
    final Connection connection;

    public TransactionStatus(Connection connection) {
        this.connection = connection;
    }
}
