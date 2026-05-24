package com.eu.habbo.habbohotel.polls;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/polls/Poll.class */
public class Poll {
    public final int id;
    public final String title;
    public final String thanksMessage;
    public final String badgeReward;
    public int lastQuestionId;
    private ArrayList<PollQuestion> questions = new ArrayList<>();

    public Poll(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.title = resultSet.getString("title");
        this.thanksMessage = resultSet.getString("thanks_message");
        this.badgeReward = resultSet.getString("reward_badge");
    }

    public ArrayList<PollQuestion> getQuestions() {
        return this.questions;
    }

    public PollQuestion getQuestion(int i) {
        for (PollQuestion pollQuestion : this.questions) {
            if (pollQuestion.id == i) {
                return pollQuestion;
            }
        }
        return null;
    }

    public void addQuestion(PollQuestion pollQuestion) {
        this.questions.add(pollQuestion);
        Collections.sort(this.questions);
    }
}
