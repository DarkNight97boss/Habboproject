package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.subscriptions.Subscription;
import com.eu.habbo.habbohotel.users.subscriptions.SubscriptionHabboClub;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/UserClubComposer.class */
public class UserClubComposer extends MessageComposer {
    private final Habbo habbo;
    private final String subscriptionType;
    private final int responseType;
    public static int RESPONSE_TYPE_NORMAL = 0;
    public static int RESPONSE_TYPE_LOGIN = 1;
    public static int RESPONSE_TYPE_PURCHASE = 2;
    public static int RESPONSE_TYPE_DISCOUNT_AVAILABLE = 3;
    public static int RESPONSE_TYPE_CITIZENSHIP_DISCOUNT = 4;

    public UserClubComposer(Habbo habbo) {
        this.habbo = habbo;
        this.subscriptionType = Subscription.HABBO_CLUB.toLowerCase();
        this.responseType = 0;
    }

    public UserClubComposer(Habbo habbo, String str) {
        this.habbo = habbo;
        this.subscriptionType = str;
        this.responseType = 0;
    }

    public UserClubComposer(Habbo habbo, String str, int i) {
        this.habbo = habbo;
        this.subscriptionType = str;
        this.responseType = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UserClubComposer);
        this.response.appendString(this.subscriptionType.toLowerCase());
        if (Emulator.getGameEnvironment().getSubscriptionManager().getSubscriptionClass(this.subscriptionType.toUpperCase()) == null) {
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
            this.response.appendBoolean(false);
            this.response.appendBoolean(false);
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
            return this.response;
        }
        Subscription subscription = this.habbo.getHabboStats().getSubscription(this.subscriptionType);
        int iFloor = 0;
        int iCeil = 0;
        int remaining = 0;
        int pastTimeAsClub = this.habbo.getHabboStats().getPastTimeAsClub();
        if (subscription != null) {
            remaining = subscription.getRemaining();
            iFloor = (int) Math.floor(((double) remaining) / 86400.0d);
            iCeil = (int) Math.ceil(((double) remaining) / 60.0d);
            if (iFloor < 1 && iCeil > 0) {
                iFloor = 1;
            }
        }
        int i = (this.responseType > RESPONSE_TYPE_LOGIN || remaining <= 0 || !SubscriptionHabboClub.DISCOUNT_ENABLED || iFloor > SubscriptionHabboClub.DISCOUNT_DAYS_BEFORE_END) ? this.responseType : RESPONSE_TYPE_DISCOUNT_AVAILABLE;
        this.response.appendInt(Integer.valueOf(iFloor));
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 0);
        this.response.appendInt(Integer.valueOf(i));
        this.response.appendBoolean(Boolean.valueOf(pastTimeAsClub > 0));
        this.response.appendBoolean(true);
        this.response.appendInt((Integer) 0);
        this.response.appendInt(Integer.valueOf((int) Math.floor(((double) pastTimeAsClub) / 86400.0d)));
        this.response.appendInt(Integer.valueOf(iCeil));
        this.response.appendInt(Integer.valueOf((Emulator.getIntUnixTimestamp() - this.habbo.getHabboStats().hcMessageLastModified) / 60));
        this.habbo.getHabboStats().hcMessageLastModified = Emulator.getIntUnixTimestamp();
        return this.response;
    }
}
