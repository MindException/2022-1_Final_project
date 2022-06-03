//개발: 이승현

package com.lastproject.used_item_market;

import java.util.Comparator;

public class CompareChatList <T extends ChattingRoomInfo> implements Comparator<T> {
    @Override
    public int compare(T o1, T o2) {
        //양수이면 o2 즉, 자기 자신이 더 크다.
        //음수이면 o1 즉, 비교치가 더 크다.
        long l1 = Long.parseLong(o1.last_time);
        long l2 = Long.parseLong(o2.last_time);

        return (l1 > l2 ? -1 : (l1 == l2) ? 0 : 1);
    }
}
