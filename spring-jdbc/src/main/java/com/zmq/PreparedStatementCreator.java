package com.zmq;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author zmq
 */
@FunctionalInterface
public interface PreparedStatementCreator {

    PreparedStatement createPreparedStatement(Connection con) throws SQLException;

}