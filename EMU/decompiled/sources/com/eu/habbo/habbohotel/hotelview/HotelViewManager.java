package com.eu.habbo.habbohotel.hotelview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/hotelview/HotelViewManager.class */
public class HotelViewManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HotelViewManager.class);
    private final HallOfFame hallOfFame;
    private final NewsList newsList;

    public HotelViewManager() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.hallOfFame = new HallOfFame();
        this.newsList = new NewsList();
        LOGGER.info("Hotelview Manager -> Loaded! ({} MS)", Long.valueOf(System.currentTimeMillis() - jCurrentTimeMillis));
    }

    public HallOfFame getHallOfFame() {
        return this.hallOfFame;
    }

    public NewsList getNewsList() {
        return this.newsList;
    }

    public void dispose() {
        LOGGER.info("HotelView Manager -> Disposed!");
    }
}
