# Java 21 공식 이미지 사용
FROM eclipse-temurin:21-jdk

# 작업 디렉터리
WORKDIR /app

# Maven Wrapper와 Maven 설정 복사
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# 실행 권한 부여
RUN chmod +x mvnw

# 컨테이너가 시작될 때 Spring Boot 실행
CMD ["./mvnw", "spring-boot:run"]