package com.zmq;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zmq
 */
public interface RowMapper<T> {
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
