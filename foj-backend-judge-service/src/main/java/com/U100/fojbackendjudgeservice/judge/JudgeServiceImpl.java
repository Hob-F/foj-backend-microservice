package com.U100.fojbackendjudgeservice.judge;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.U100.fojbackendcommon.common.ErrorCode;
import com.U100.fojbackendcommon.exception.BusinessException;
import com.U100.fojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.U100.fojbackendjudgeservice.judge.codesandbox.CodeSandboxFactory;
import com.U100.fojbackendjudgeservice.judge.codesandbox.CodeSandboxProxy;
import com.U100.fojbackendjudgeservice.judge.strategy.JudgeContext;
import com.U100.fojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.U100.fojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import com.U100.fojbackendmodel.model.codesandbox.JudgeInfo;
import com.U100.fojbackendmodel.model.dto.question.JudgeCase;
import com.U100.fojbackendmodel.model.entity.Question;
import com.U100.fojbackendmodel.model.entity.QuestionSubmit;
import com.U100.fojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.U100.fojbackendserviceclient.service.QuestionFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionFeignClient questionFeignClient;

    @Resource
    private JudgeManager judgeManager;

    @Value("${codesandbox.type:example}")
    private String type;


    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1）传入题目的提交 id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2）如果题目提交状态不为等待中，就不用重复执行了
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3）更改判题（题目提交）的状态为 “判题中”，防止重复执行
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        // 4）调用沙箱，获取到执行结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        // 获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse;
        try {
            executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        } catch (Exception e) {
            // 沙箱调用异常兜底:置为终态"系统错误",不抛异常以免消息反复重投
            log.error("沙箱调用异常, submitId={}", questionSubmitId, e);
            return updateSubmitToTerminalState(questionSubmitId, "系统错误");
        }
        // 沙箱返回错误状态(2=沙箱错误,如编译错误):置为终态,message 取沙箱返回信息
        if (executeCodeResponse == null || Integer.valueOf(2).equals(executeCodeResponse.getStatus())) {
            String message = executeCodeResponse == null ? "系统错误"
                    : (StrUtil.isNotBlank(executeCodeResponse.getMessage()) ? executeCodeResponse.getMessage() : "编译错误");
            return updateSubmitToTerminalState(questionSubmitId, message);
        }
        List<String> outputList = executeCodeResponse.getOutputList();
        // 5）根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        // 6）修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        QuestionSubmit questionSubmitResult = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        return questionSubmitResult;
    }

    /**
     * 把提交置为终态(判题完成)。
     *
     * <p>用于编译错误/系统错误等场景:判题已结束但结果不是正常通过,
     * 因此 status 置为 SUCCEED(2),judgeInfo.message 记录错误原因,供前端展示。</p>
     *
     * @param questionSubmitId 提交 id
     * @param message          错误信息(如"编译错误"/"系统错误")
     * @return 更新后的提交记录
     */
    private QuestionSubmit updateSubmitToTerminalState(long questionSubmitId, String message) {
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(message);
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        boolean update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        return questionFeignClient.getQuestionSubmitById(questionSubmitId);
    }
}
