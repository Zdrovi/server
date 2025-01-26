package com.zdrovi.commons;

import com.zdrovi.google.model.*;

import java.util.List;
import java.util.Map;

import static com.zdrovi.commons.TestConstants.EMAIL;
import static com.zdrovi.commons.TestConstants.NAME;

public interface ApiMockRepository {

    static Map<String, Answer> getAnswers() {
        return Map.of(
                "5bb6a046", Answer.builder()
                        .questionId("5bb6a046")
                        .textAnswers(TextAnswers.builder()
                                .answers(List.of(TextAnswer.builder().value(NAME).build()))
                                .build())
                        .build(),
                "7ff50009", Answer.builder()
                        .questionId("7ff50009")
                        .textAnswers(TextAnswers.builder()
                                .answers(List.of(TextAnswer.builder().value(EMAIL).build()))
                                .build())
                        .build(),
                "0d298066", Answer.builder()
                        .questionId("0d298066")
                        .textAnswers(TextAnswers.builder()
                                .answers(List.of(TextAnswer.builder().value("8").build()))
                                .build())
                        .build(),
                "3158daa2", Answer.builder()
                        .questionId("3158daa2")
                        .textAnswers(TextAnswers.builder()
                                .answers(List.of(TextAnswer.builder().value("5").build()))
                                .build())
                        .build(),
                "514e9301", Answer.builder()
                        .questionId("514e9301")
                        .textAnswers(TextAnswers.builder()
                                .answers(List.of(TextAnswer.builder().value("8").build()))
                                .build())
                        .build(),
                "24c1f4dd", Answer.builder()
                        .questionId("24c1f4dd")
                        .textAnswers(TextAnswers.builder()
                                .answers(List.of(TextAnswer.builder().value("4").build()))
                                .build())
                        .build(),
                "689e5405", Answer.builder()
                        .questionId("689e5405")
                        .textAnswers(TextAnswers.builder()
                                .answers(List.of(TextAnswer.builder().value("9").build()))
                                .build())
                        .build(),
                "5f0a2ea9", Answer.builder()
                        .questionId("5f0a2ea9")
                        .textAnswers(TextAnswers.builder()
                                .answers(List.of(TextAnswer.builder().value("3").build()))
                                .build())
                        .build(),
                "3a977e96", Answer.builder()
                        .questionId("3a977e96")
                        .textAnswers(TextAnswers.builder()
                                .answers(List.of(TextAnswer.builder().value("10").build()))
                                .build())
                        .build(),
                "33f358d4", Answer.builder()
                        .questionId("33f358d4")
                        .textAnswers(TextAnswers.builder()
                                .answers(List.of(TextAnswer.builder().value("8").build()))
                                .build())
                        .build()
        );
    }

    static List<Item> createItems() {
        return List.of(
                Item.builder()
                        .itemId("113db900")
                        .title("Imię")
                        .questionItem(QuestionItem.builder()
                                .question(Question.builder()
                                        .questionId("5bb6a046")
                                        .required(true)
                                        .textQuestion(new TextQuestion())
                                        .build())
                                .build())
                        .build(),
                Item.builder()
                        .itemId("14c13a6b")
                        .title("Email")
                        .questionItem(QuestionItem.builder()
                                .question(Question.builder()
                                        .questionId("7ff50009")
                                        .required(true)
                                        .textQuestion(new TextQuestion())
                                        .build())
                                .build())
                        .build(),
                Item.builder()
                        .itemId("6489a8da")
                        .title("Co cię interesuje?")
                        .pageBreakItem(new Object())
                        .build(),
                Item.builder()
                        .itemId("32c80cb5")
                        .title("Niewystarczająca długość")
                        .questionItem(QuestionItem.builder()
                                .question(Question.builder()
                                        .questionId("0d298066")
                                        .required(true)
                                        .build())
                                .build())
                        .build(),
                Item.builder()
                        .itemId("57a33dd0")
                        .title("Niska jakość snu")
                        .questionItem(QuestionItem.builder()
                                .question(Question.builder()
                                        .questionId("3158daa2")
                                        .required(true)
                                        .build())
                                .build())
                        .build(),
                Item.builder()
                        .itemId("7c0434a3")
                        .title("Częste pobudki")
                        .questionItem(QuestionItem.builder()
                                .question(Question.builder()
                                        .questionId("514e9301")
                                        .required(true)
                                        .build())
                                .build())
                        .build(),
                Item.builder()
                        .itemId("1364840b")
                        .title("Problemy w pracy")
                        .questionItem(QuestionItem.builder()
                                .question(Question.builder()
                                        .questionId("24c1f4dd")
                                        .required(true)
                                        .build())
                                .build())
                        .build(),
                Item.builder()
                        .itemId("1934fcc9")
                        .title("Przemęczenie")
                        .questionItem(QuestionItem.builder()
                                .question(Question.builder()
                                        .questionId("689e5405")
                                        .required(true)
                                        .build())
                                .build())
                        .build(),
                Item.builder()
                        .itemId("6feac521")
                        .title("Otyłość")
                        .questionItem(QuestionItem.builder()
                                .question(Question.builder()
                                        .questionId("5f0a2ea9")
                                        .required(true)
                                        .build())
                                .build())
                        .build(),
                Item.builder()
                        .itemId("009cf9d4")
                        .title("Anoreksja")
                        .questionItem(QuestionItem.builder()
                                .question(Question.builder()
                                        .questionId("3a977e96")
                                        .required(true)
                                        .build())
                                .build())
                        .build(),
                Item.builder()
                        .itemId("7f8570d4")
                        .title("Używki")
                        .questionItem(QuestionItem.builder()
                                .question(Question.builder()
                                        .questionId("33f358d4")
                                        .required(true)
                                        .build())
                                .build())
                        .build()
        );
    }

}
