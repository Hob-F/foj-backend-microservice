package com.U100.fojbackendjudgeservice.judge;

import com.U100.fojbackendjudgeservice.judge.strategy.DefaultJudgeStrategy;
import com.U100.fojbackendjudgeservice.judge.strategy.JavaLanguageJudgeStrategy;
import com.U100.fojbackendjudgeservice.judge.strategy.JudgeContext;
import com.U100.fojbackendjudgeservice.judge.strategy.JudgeStrategy;
import com.U100.fojbackendmodel.model.codesandbox.JudgeInfo;
import com.U100.fojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }

}
