package Utils.Math.Impl;

import Utils.Math.SingleNumber;

public class doublec implements SingleNumber{
    @Override
    public Number sqrt(Number right) {
        return Math.sqrt(right.doubleValue());
    }
}
