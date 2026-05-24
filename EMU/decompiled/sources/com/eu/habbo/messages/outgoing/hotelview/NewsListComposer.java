package com.eu.habbo.messages.outgoing.hotelview;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.hotelview.NewsList;
import com.eu.habbo.habbohotel.hotelview.NewsWidget;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/hotelview/NewsListComposer.class */
public class NewsListComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(286);
        NewsList newsList = Emulator.getGameEnvironment().getHotelViewManager().getNewsList();
        this.response.appendInt(Integer.valueOf(newsList.getNewsWidgets().size()));
        for (NewsWidget newsWidget : newsList.getNewsWidgets()) {
            this.response.appendInt(Integer.valueOf(newsWidget.getId()));
            this.response.appendString(newsWidget.getTitle());
            this.response.appendString(newsWidget.getMessage());
            this.response.appendString(newsWidget.getButtonMessage());
            this.response.appendInt(Integer.valueOf(newsWidget.getType()));
            this.response.appendString(newsWidget.getLink());
            this.response.appendString(newsWidget.getImage());
        }
        return this.response;
    }
}
