# 使用 OpenJDK 17 作为基础镜像
FROM openjdk:17

# 设置工作目录
WORKDIR /app

# 将应用的 JAR 文件复制到容器中
COPY build/libs/PollingWithHE-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用所需的端口
EXPOSE 8080

# 运行 Spring Boot 应用
ENTRYPOINT ["java", "-jar", "app.jar"]