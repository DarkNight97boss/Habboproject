package com.eu.habbo.habbohotel.polls;

import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.map.hash.THashMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/polls/PollQuestion.class */
public class PollQuestion implements ISerialize, Comparable<PollQuestion> {
    public final int id;
    public final int parentId;
    public final int type;
    public final String question;
    public final int minSelections;
    public final int order;
    public final THashMap<Integer, String[]> options = new THashMap<>();
    private ArrayList<PollQuestion> subQuestions = new ArrayList<>();

    public PollQuestion(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.parentId = resultSet.getInt("parent_id");
        this.type = resultSet.getInt("type");
        this.question = resultSet.getString("question");
        this.minSelections = resultSet.getInt("min_selections");
        this.order = resultSet.getInt("order");
        String string = resultSet.getString("options");
        if (this.type == 1 || this.type == 2) {
            for (int i = 0; i < string.split(";").length; i++) {
                this.options.put(Integer.valueOf(i), new String[]{string.split(";")[i].split(":")[0], string.split(";")[i].split(":")[1]});
            }
        }
    }

    public void addSubQuestion(PollQuestion pollQuestion) {
        this.subQuestions.add(pollQuestion);
    }

    @Override // com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(this.id));
        serverMessage.appendInt(Integer.valueOf(this.order));
        serverMessage.appendInt(Integer.valueOf(this.type));
        serverMessage.appendString(this.question);
        serverMessage.appendInt(Integer.valueOf(this.minSelections));
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt(Integer.valueOf(this.options.size()));
        if (this.type == 1 || this.type == 2) {
            for (Map.Entry entry : this.options.entrySet()) {
                serverMessage.appendString(((String[]) entry.getValue())[0]);
                serverMessage.appendString(((String[]) entry.getValue())[1]);
                serverMessage.appendInt((Integer) entry.getKey());
            }
        }
        if (this.parentId <= 0) {
            Collections.sort(this.subQuestions);
            serverMessage.appendInt(Integer.valueOf(this.subQuestions.size()));
            Iterator<PollQuestion> it = this.subQuestions.iterator();
            while (it.hasNext()) {
                it.next().serialize(serverMessage);
            }
        }
    }

    @Override // java.lang.Comparable
    public int compareTo(PollQuestion pollQuestion) {
        return this.order - pollQuestion.order;
    }
}
