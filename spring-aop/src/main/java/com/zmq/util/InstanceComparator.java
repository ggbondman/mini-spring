package com.zmq.util;

import java.util.Comparator;

/**
 * @author zmq
 */
public class InstanceComparator<T> implements Comparator<T> {
    private final Class<?>[] instanceOrder;

    public InstanceComparator(Class<?>... instanceOrder) {
        this.instanceOrder = instanceOrder;

    }

    @Override
    public int compare(T o1, T o2) {
        Integer order1 = getOder(o1);
        Integer order2 = getOder(o2);
        return Integer.compare(order1,order2);
    }

    private Integer getOder(T o){
        for (int i = 0; i < instanceOrder.length; i++) {
            if (instanceOrder[i].isInstance(o)){
                return i;
            }
        }
        return this.instanceOrder.length;
    }
}
