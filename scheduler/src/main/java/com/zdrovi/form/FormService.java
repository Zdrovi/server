package com.zdrovi.form;

import java.time.ZonedDateTime;
import java.util.List;

public interface FormService {

    public List<List<String>> getAnswers();

    public List<List<String>> getAnswers(ZonedDateTime from);
}
