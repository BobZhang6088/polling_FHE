# 第一阶段：构建前端
FROM node:16 AS build-frontend

# 设置工作目录
WORKDIR /app

# 复制 package.json 和 package-lock.json
COPY src/frontend/package*.json ./

# 安装前端依赖
RUN npm install

# 复制所有前端代码
COPY src/frontend .

# 运行 Webpack 打包
RUN npm run build

# 第二阶段：构建后端（Spring Boot）
FROM openjdk:17-jdk-slim

# 安装 curl 和 Node.js
RUN apt-get update && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_16.x | bash - \
    && apt-get install -y nodejs

# 设置工作目录
WORKDIR /app

# 安装 node-seal
RUN npm install node-seal

# 将应用的 JAR 文件复制到容器中
COPY build/libs/PollingWithHE-0.0.1-SNAPSHOT.jar app.jar

# 将前端打包后的文件复制到 Spring Boot 静态资源目录
COPY --from=build-frontend /app/dist /app/static

# 暴露应用所需的端口
EXPOSE 8080

# 运行 Spring Boot 应用
ENTRYPOINT ["java", "-jar", "app.jar"]
