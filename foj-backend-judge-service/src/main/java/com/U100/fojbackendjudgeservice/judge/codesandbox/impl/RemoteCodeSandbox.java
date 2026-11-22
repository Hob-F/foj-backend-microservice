package com.U100.fojbackendjudgeservice.judge.codesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.U100.fojbackendcommon.common.ErrorCode;
import com.U100.fojbackendcommon.exception.BusinessException;
import com.U100.fojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.U100.fojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.U100.fojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import org.apache.commons.lang3.StringUtils;

/**
 * 远程代码沙箱(实际调用接口的沙箱)
 */
public class RemoteCodeSandbox implements CodeSandbox {

    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "secretKey";

    /**
     * 沙箱服务地址。默认指向 VM 中的沙箱实例;
     * 容器化部署时仍可用 JVM 参数 -Dcodesandbox.url=http://<沙箱服务名>:8090/executeCode 覆盖。
     */
    private static final String SANDBOX_URL = System.getProperty(
            "codesandbox.url",
            "http://192.168.93.128:8090/executeCode"   // ← 唯一实质改动点
    );

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱,请求地址: " + SANDBOX_URL);   // ← 诊断日志,顺带升级
        String url = SANDBOX_URL;
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(url)
                .header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET)
                .body(json)
                .execute()
                .body();
        if (StringUtils.isBlank(responseStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR,
                    "executeCode remoteSandbox error, message = " + responseStr);
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}
