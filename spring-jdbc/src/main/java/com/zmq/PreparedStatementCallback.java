package com.zmq;

import jakarta.annotation.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author zmq
 */
@FunctionalInterface
public interface PreparedStatementCallback<T> {

    @Nullable
    T doInPreparedStatement(PreparedStatement ps) throws SQLException;

}