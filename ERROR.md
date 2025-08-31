2025-08-31T22:05:12.346+08:00  INFO 18568 --- [ai-code-mother-backend] [io-8123-exec-10] c.s.a.controller.AppController           : 开始创建应用，请求参数: AppAddRequest(initPrompt=创建一个现代化的个人 To Do List 的个人计划列表，记得我喜欢蓝色+紫色，选用 HTML模式)
2025-08-31T22:05:12.366+08:00  INFO 18568 --- [ai-code-mother-backend] [io-8123-exec-10] d.l.http.client.log.LoggingHttpClient    : HTTP request:
- method: POST
- url: https://api.deepseek.com/chat/completions
- headers: [Authorization: Beare...9e], [User-Agent: langchain4j-openai], [Content-Type: application/json]
- body: {
  "model" : "deepseek-chat",
  "messages" : [ {
  "role" : "system",
  "content" : "你是一个专业的代码生成方案路由器，需要根据用户需求返回最合适的代码生成类型。\r\n\r\n可选的代码生成类型：\r\n1. HTML - 适合简单的静态页面，单个 HTML 文件，包含内联 CSS 和 JS\r\n2. MULTI_FILE - 适合简单的多文件静态页面，分离 HTML、CSS、JS 代码\r\n3. VUE_PROJECT - 适合复杂的现代化前端项目\r\n\r\n判断规则：\r\n- 如果用户需求简单，只需要一个展示页面，选择 HTML\r\n- 如果用户需要多个页面但不涉及复杂交互，选择 MULTI_FILE\r\n- 如果用户需求复杂，涉及多页面、复杂交互、数据管理等，选择 VUE_PROJECT\r\n"
  }, {
  "role" : "user",
  "content" : "创建一个现代化的个人 To Do List 的个人计划列表，记得我喜欢蓝色+紫色，选用 HTML模式\nYou must answer strictly with one of these enums:\nHTML\nMULTI_FILE\nVUE_PROJECT"
  } ],
  "stream" : false
  }

2025-08-31T22:05:15.007+08:00  INFO 18568 --- [ai-code-mother-backend] [io-8123-exec-10] d.l.http.client.log.LoggingHttpClient    : HTTP response:
- status code: 200
- headers: [Date: Sun, 31 Aug 2025 14:05:14 GMT], [Content-Type: application/json], [Transfer-Encoding: chunked], [Connection: keep-alive], [Set-Cookie: [HWWAFSESTIME=1756649110978; path=/, HWWAFSESID=513aa7f5a00efb2067; path=/]], [vary: origin, access-control-request-method, access-control-request-headers], [access-control-allow-credentials: true], [x-ds-trace-id: 2686239db284678990883a33488185ac], [Strict-Transport-Security: max-age=31536000; includeSubDomains; preload], [X-Content-Type-Options: nosniff], [Server: CW]
- body: {"id":"f3a2ae63-893f-4774-b344-8b95bac1ec4d","object":"chat.completion","created":1756649114,"model":"deepseek-chat","choices":[{"index":0,"message":{"role":"assistant","content":"HTML"},"logprobs":null,"finish_reason":"stop"}],"usage":{"prompt_tokens":206,"completion_tokens":1,"total_tokens":207,"prompt_tokens_details":{"cached_tokens":192},"prompt_cache_hit_tokens":192,"prompt_cache_miss_tokens":14},"system_fingerprint":"fp_feb633d1f5_prod0820_fp8_kvcache"}

2025-08-31T22:05:15.016+08:00  INFO 18568 --- [ai-code-mother-backend] [io-8123-exec-10] c.s.a.service.impl.AppServiceImpl        : 应用创建成功，ID: 319763062493458432, 类型: html
2025-08-31T22:05:15.523+08:00  INFO 18568 --- [ai-code-mother-backend] [nio-8123-exec-2] c.s.a.controller.AppController           : 开始获取应用详情，应用ID: 319763062493458432
2025-08-31T22:05:15.529+08:00  INFO 18568 --- [ai-code-mother-backend] [nio-8123-exec-2] c.s.a.controller.AppController           : 数据库查询结果，应用ID: 319763062493458432，查询结果: 找到应用
2025-08-31T22:05:15.529+08:00  INFO 18568 --- [ai-code-mother-backend] [nio-8123-exec-2] c.s.a.controller.AppController           : 应用详情 - ID: 319763062493458432, 名称: 创建一个现代化的个人 T, 创建者: 1, 创建时间: 2025-08-31T22:05:15
2025-08-31T22:05:15.534+08:00  INFO 18568 --- [ai-code-mother-backend] [nio-8123-exec-2] c.s.a.controller.AppController           : 成功获取应用详情，应用ID: 319763062493458432
2025-08-31T22:05:15.971+08:00  INFO 18568 --- [ai-code-mother-backend] [nio-8123-exec-3] c.s.a.controller.AppController           : 开始获取应用详情，应用ID: 319763062493458432
2025-08-31T22:05:15.974+08:00  INFO 18568 --- [ai-code-mother-backend] [nio-8123-exec-3] c.s.a.controller.AppController           : 数据库查询结果，应用ID: 319763062493458432，查询结果: 找到应用
2025-08-31T22:05:15.974+08:00  INFO 18568 --- [ai-code-mother-backend] [nio-8123-exec-3] c.s.a.controller.AppController           : 应用详情 - ID: 319763062493458432, 名称: 创建一个现代化的个人 T, 创建者: 1, 创建时间: 2025-08-31T22:05:15
2025-08-31T22:05:15.976+08:00  INFO 18568 --- [ai-code-mother-backend] [nio-8123-exec-3] c.s.a.controller.AppController           : 成功获取应用详情，应用ID: 319763062493458432
2025-08-31T22:05:16.935+08:00 ERROR 18568 --- [ai-code-mother-backend] [nio-8123-exec-5] o.a.c.c.C.[.[.[.[dispatcherServlet]      : Servlet.service() for servlet [dispatcherServlet] in context with path [/api] threw exception [Handler dispatch failed: java.lang.NoClassDefFoundError: com/spring/aicodemother/ratelimit/aspect/RateLimitAspect$1] with root cause

java.lang.ClassNotFoundException: com.spring.aicodemother.ratelimit.aspect.RateLimitAspect$1
at com.spring.aicodemother.ratelimit.aspect.RateLimitAspect.generateRateLimitKey(RateLimitAspect.java:70) ~[classes/:na]
at com.spring.aicodemother.ratelimit.aspect.RateLimitAspect.doBefore(RateLimitAspect.java:43) ~[classes/:na]
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs(AbstractAspectJAdvice.java:649) ~[spring-aop-6.2.9.jar:6.2.9]
at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod(AbstractAspectJAdvice.java:624) ~[spring-aop-6.2.9.jar:6.2.9]
at org.springframework.aop.aspectj.AspectJMethodBeforeAdvice.before(AspectJMethodBeforeAdvice.java:44) ~[spring-aop-6.2.9.jar:6.2.9]
at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:57) ~[spring-aop-6.2.9.jar:6.2.9]
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:173) ~[spring-aop-6.2.9.jar:6.2.9]
at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-6.2.9.jar:6.2.9]
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.2.9.jar:6.2.9]
at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:728) ~[spring-aop-6.2.9.jar:6.2.9]
at com.spring.aicodemother.controller.AppController$$SpringCGLIB$$0.chatToGenCode(<generated>) ~[classes/:na]
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:258) ~[spring-web-6.2.9.jar:6.2.9]
at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:191) ~[spring-web-6.2.9.jar:6.2.9]
at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118) ~[spring-webmvc-6.2.9.jar:6.2.9]
at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:991) ~[spring-webmvc-6.2.9.jar:6.2.9]
at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:896) ~[spring-webmvc-6.2.9.jar:6.2.9]
at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-6.2.9.jar:6.2.9]
at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089) ~[spring-webmvc-6.2.9.jar:6.2.9]
at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979) ~[spring-webmvc-6.2.9.jar:6.2.9]
at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014) ~[spring-webmvc-6.2.9.jar:6.2.9]
at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:903) ~[spring-webmvc-6.2.9.jar:6.2.9]
at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:564) ~[tomcat-embed-core-10.1.43.jar:6.0]
at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885) ~[spring-webmvc-6.2.9.jar:6.2.9]
at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658) ~[tomcat-embed-core-10.1.43.jar:6.0]
at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:195) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51) ~[tomcat-embed-websocket-10.1.43.jar:10.1.43]
at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-6.2.9.jar:6.2.9]
at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.9.jar:6.2.9]
at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-6.2.9.jar:6.2.9]
at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.9.jar:6.2.9]
at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.springframework.session.web.http.SessionRepositoryFilter.doFilterInternal(SessionRepositoryFilter.java:142) ~[spring-session-core-3.5.1.jar:3.5.1]
at org.springframework.session.web.http.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:82) ~[spring-session-core-3.5.1.jar:3.5.1]
at org.springframework.web.filter.DelegatingFilterProxy.invokeDelegate(DelegatingFilterProxy.java:362) ~[spring-web-6.2.9.jar:6.2.9]
at org.springframework.web.filter.DelegatingFilterProxy.doFilter(DelegatingFilterProxy.java:278) ~[spring-web-6.2.9.jar:6.2.9]
at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201) ~[spring-web-6.2.9.jar:6.2.9]
at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.2.9.jar:6.2.9]
at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:483) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:116) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:398) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1769) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1189) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:658) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63) ~[tomcat-embed-core-10.1.43.jar:10.1.43]
at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]

