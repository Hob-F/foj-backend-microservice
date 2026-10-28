package com.U100.fojbackendjudgeservice.judge.codesandbox;

import com.U100.fojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.U100.fojbackendmodel.model.codesandbox.ExecuteCodeResponse;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
