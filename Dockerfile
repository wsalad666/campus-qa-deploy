# ============ 构建阶段 ============
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 先复制 pom.xml，利用 Docker 缓存层加速构建
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN mvn dependency:go-offline -B -q

# 复制源码并构建
COPY src ./src
RUN mvn package -DskipTests -B -q

# ============ 运行阶段 ============
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 创建上传目录
RUN mkdir -p /app/uploads

# 从构建阶段复制 JAR
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
