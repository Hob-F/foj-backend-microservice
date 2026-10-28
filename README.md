# FOJ 在线判题系统（微服务版）

基于 Spring Cloud Alibaba 的在线判题系统（Online Judge），支持代码提交、
沙箱执行、多组测试用例自动判题（Accepted / Wrong Answer / 超时）。
由单体架构演进为微服务架构（单体版见 [foj-backend](https://github.com/🔲你的用户名/foj-backend)）。

## 架构总览

```
前端(Vue3)
   │  HTTP
   ▼
网关 foj-backend-gateway (:8121，统一路由/跨域)
   │  Nacos 服务发现 + OpenFeign 调用
   ├──► 用户服务 foj-backend-user-service（注册登录、权限）
   ├──► 题目服务 foj-backend-question-service（题目管理、提交记录）
   └──► 判题服务 foj-backend-judge-service（RabbitMQ 消费判题）
              │  代理模式调用
              ▼
        Java 代码沙箱（独立服务，见 foj-code-sandbox 仓库）
   基础设施：MySQL / Redis / RabbitMQ / Nacos
```

## 仓库导航

| 模块 | 仓库 |
|---|---|
| 后端微服务（本仓库） | 网关 + 用户/题目/判题三服务 |
| [foj-frontend](https://github.com/🔲你的用户名/foj-frontend) | Vue3 + Arco Design 前端 |
| [foj-code-sandbox](https://github.com/🔲你的用户名/foj-code-sandbox) | Java 代码沙箱（独立部署） |
| [foj-backend](https://github.com/🔲你的用户名/foj-backend) | 单体版后端（演进前版本） |

## 模块说明

| 模块 | 职责 |
|---|---|
| foj-backend-gateway | API 网关，统一路由与跨域处理 |
| foj-backend-user-service | 用户注册/登录、Session 会话（Redis） |
| foj-backend-question-service | 题目 CRUD、题目提交、判题记录查询 |
| foj-backend-judge-service | MQ 消费提交消息，拉取用例调用沙箱，执行判题策略回写结果 |
| foj-backend-service-client | 服务间 Feign 接口契约（解耦的物理边界） |
| foj-backend-model / common | 公共实体与工具类 |

## 核心流程：异步判题

```
用户提交代码 ──► 题目服务落库(status=等待判题) ──► 发送 RabbitMQ
                                                        │
前端每 500ms 轮询提交记录 ◄── 判题服务回写终态 ◄── 消费消息，Feign 拉取题目+用例
                                                        │
                                        代理模式调用沙箱，逐用例执行比对
```

## 技术亮点

- **RabbitMQ 异步解耦**：提交与判题分离，高峰期削峰填谷；判题服务串行消费保障稳定性
- **代理 + 策略模式**：沙箱实现可插拔切换（本地/远程），判题策略独立封装、可扩展
- **雪花 ID 精度丢失治理**：前端 JS Number 只有 53 位安全整数，Long 型 ID 超限被截断；
  通过 Jackson 全局配置 Long → String 序列化根治（所有接口统一生效）
- **多组用例逐条判题**：全部通过才 AC，任意一条不匹配即 WA，耗时/内存取多次执行峰值

## 功能测试记录

| 用例 | 代码版本 | 预期 | 实际 |
|---|---|---|---|
| 1 2 / 0 0 / -5 3 / 2147483647 0 | long | Accepted | ✅ |
| 2147483647 + 1（int 溢出边界） | int 版 | Wrong Answer | ✅ 输出 -2147483648（补码回绕）被精准识别 |
| 2147483647 + 1 | long 版 | Accepted（输出 2147483648） | ✅ |
| 超时行为 | 死循环版 | 时间超限 | ✅ |

## 快速启动

1. 准备基础设施：MySQL、Redis、RabbitMQ、Nacos（并建库执行建表 SQL）
2. 依次启动：`gateway` → `user-service` → `question-service` → `judge-service`
3. 前端启动见 [foj-frontend](https://github.com/🔲你的用户名/foj-frontend)
4. 访问 🔲http://localhost:8080（前端地址）

## 待优化

- [ ] 沙箱资源隔离（Docker 容器化 / cgroups 限制 CPU 内存）与超时强杀
- [ ] 判题并发消费与水平扩容
- [ ] Java 原生沙箱实现（Java Process API）
