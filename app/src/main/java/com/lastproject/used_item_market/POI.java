//개발: 김도훈

package com.lastproject.used_item_market;

import com.skt.Tmap.poi_item.TMapPOIItem;

public class POI {
    TMapPOIItem item;

    public POI(TMapPOIItem item){
        this.item = item;
    }

    @Override
    public String toString() {
        return item.getPOIName();
    }
}
