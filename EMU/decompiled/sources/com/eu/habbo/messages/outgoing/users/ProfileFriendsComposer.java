package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.habbohotel.messenger.MessengerBuddy;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/ProfileFriendsComposer.class */
public class ProfileFriendsComposer extends MessageComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileFriendsComposer.class);
    private final List<MessengerBuddy> lovers = new ArrayList();
    private final List<MessengerBuddy> friends = new ArrayList();
    private final List<MessengerBuddy> haters = new ArrayList();
    private final int userId;

    public ProfileFriendsComposer(THashMap<Integer, THashSet<MessengerBuddy>> tHashMap, int i) {
        this.lovers.addAll((Collection) tHashMap.get(1));
        this.friends.addAll((Collection) tHashMap.get(2));
        this.haters.addAll((Collection) tHashMap.get(3));
        this.userId = i;
    }

    public ProfileFriendsComposer(Habbo habbo) {
        try {
            for (Map.Entry<Integer, MessengerBuddy> entry : habbo.getMessenger().getFriends().entrySet()) {
                if (entry.getValue().getRelation() != 0) {
                    switch (entry.getValue().getRelation()) {
                        case 1:
                            this.lovers.add(entry.getValue());
                            break;
                        case 2:
                            this.friends.add(entry.getValue());
                            break;
                        case 3:
                            this.haters.add(entry.getValue());
                            break;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
        this.userId = habbo.getHabboInfo().getId();
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        try {
            this.response.init(Outgoing.ProfileFriendsComposer);
            this.response.appendInt(Integer.valueOf(this.userId));
            int i = 0;
            if (!this.lovers.isEmpty()) {
                i = 0 + 1;
            }
            if (!this.friends.isEmpty()) {
                i++;
            }
            if (!this.haters.isEmpty()) {
                i++;
            }
            this.response.appendInt(Integer.valueOf(i));
            Random random = new Random();
            if (!this.lovers.isEmpty()) {
                int iNextInt = random.nextInt(this.lovers.size());
                this.response.appendInt((Integer) 1);
                this.response.appendInt(Integer.valueOf(this.lovers.size()));
                this.response.appendInt(Integer.valueOf(this.lovers.get(iNextInt).getId()));
                this.response.appendString(this.lovers.get(iNextInt).getUsername());
                this.response.appendString(this.lovers.get(iNextInt).getLook());
            }
            if (!this.friends.isEmpty()) {
                int iNextInt2 = random.nextInt(this.friends.size());
                this.response.appendInt((Integer) 2);
                this.response.appendInt(Integer.valueOf(this.friends.size()));
                this.response.appendInt(Integer.valueOf(this.friends.get(iNextInt2).getId()));
                this.response.appendString(this.friends.get(iNextInt2).getUsername());
                this.response.appendString(this.friends.get(iNextInt2).getLook());
            }
            if (!this.haters.isEmpty()) {
                int iNextInt3 = random.nextInt(this.haters.size());
                this.response.appendInt((Integer) 3);
                this.response.appendInt(Integer.valueOf(this.haters.size()));
                this.response.appendInt(Integer.valueOf(this.haters.get(iNextInt3).getId()));
                this.response.appendString(this.haters.get(iNextInt3).getUsername());
                this.response.appendString(this.haters.get(iNextInt3).getLook());
            }
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
        return this.response;
    }
}
