package com.lastproject.used_item_market;

import java.util.Comparator;

public class CompareUnivDistance <T extends University> implements Comparator<T> {

    @Override
    public int compare(T o1, T o2) {
        //양수이면 o2 즉, 자기 자신이 더 크다.
        //음수이면 o1 즉, 비교치가 더 크다.

        return (o1.distance < o2.distance ? -1 : (o1.distance == o2.distance) ? 0 : 1);
    }
}
