package com.zdrovi.form;

import java.time.ZonedDateTime;
import java.util.List;

public interface FormService {

    List<List<String>> getAnswers(ZonedDateTime from);
}
