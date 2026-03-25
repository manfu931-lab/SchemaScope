HealthController.java 
这是一个“健康检查接口”。意思是：以后只要访问一个固定地址，就能知道你的项目是不是活着。对外暴露一个 HTTP 地址比如 /health
当浏览器访问这个地址时，返回一句简单的话,比如：SchemaScope is running

AnalysisController.java 暴露 HTTP 接口，接收分析请求