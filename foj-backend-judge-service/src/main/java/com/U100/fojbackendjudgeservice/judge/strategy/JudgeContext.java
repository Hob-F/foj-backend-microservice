package com.U100.fojbackendjudgeservice.judge.strategy;

import com.U100.fojbackendmodel.model.codesandbox.JudgeInfo;
import com.U100.fojbackendmodel.model.dto.question.JudgeCase;
import com.U100.fojbackendmodel.model.entity.Question;
import com.U100.fojbackendmodel.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * 上下文（用于定义在策略中传递的参数）
 */
@Data
public class JudgeContext {

    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaseList;

    private Question question;

    private QuestionSubmit questionSubmit;

}
