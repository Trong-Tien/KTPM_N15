# Sử dụng base image Java 21 JDK
FROM eclipse-temurin:21-jdk AS build

# Thư mục chứa ứng dụng trong container
WORKDIR /app

# Copy file WAR đã được build sẵn vào container
COPY target/*.war app.war

# Sử dụng Eclipse Temurin để chạy ứng dụng
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/app.war app.war

# Cấu hình port mặc định (sửa tùy service)
EXPOSE 8083

# Command để chạy ứng dụng WAR bằng Spring Boot
ENTRYPOINT ["java", "-jar", "app.war"]
