package com.zmq.util;

import com.zmq.aop.AdviceType;

import java.util.Comparator;

/**
 * @author zmq
 */
public class AdviceTypeComparator<T> implements Comparator<T> {

    private final AdviceType[] adviceTypeOrder;

    public AdviceTypeComparator(AdviceType... adviceTypeOrder) {
        this.adviceTypeOrder = adviceTypeOrder;
    }

    @Override
    public int compare(T o1, T o2) {
        int order1 = getOrder(o1);
        int order2 = getOrder(o2);
        return Integer.compare(order1,order2);
    }

    private int getOrder(T o){
        for (int i = 0; i < adviceTypeOrder.length; i++) {
            if (adviceTypeOrder[i] == o) {
                return i;
            }
        }
        return this.adviceTypeOrder.length;
    }
}
