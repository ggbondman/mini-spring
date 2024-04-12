package com.zmq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zmq
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private int productId;
    private int newPrice;
}
