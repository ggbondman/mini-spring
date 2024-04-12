package com.zmq;

import jakarta.annotation.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author zmq
 */
@FunctionalInterface
public interface ConnectionCallback<T>{

    @Nullable
    T doInConnection(Connection con) throws SQLException;
}
