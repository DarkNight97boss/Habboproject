package com.eu.habbo.habbohotel.modtool;

import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/CfhCategory.class */
public class CfhCategory {
    private final int id;
    private final String name;
    private final TIntObjectMap<CfhTopic> topics = TCollections.synchronizedMap(new TIntObjectHashMap());

    public CfhCategory(int i, String str) {
        this.id = i;
        this.name = str;
    }

    public void addTopic(CfhTopic cfhTopic) {
        this.topics.put(cfhTopic.id, cfhTopic);
    }

    public TIntObjectMap<CfhTopic> getTopics() {
        return this.topics;
    }

    public String getName() {
        return this.name;
    }
}
