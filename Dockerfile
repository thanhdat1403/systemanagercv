#*Tác dụng của Dockerfile: bth nếu muốn chạy dự án, là phải mở interlliJ nên, máy tính phải cài sẵn
 #Java 17(tùy vào dự án đang dùng Java bản gì), cài Gradle, rồi bấm run.Nhưng khi có Dockerfile , ta có thể mang dự án này sang bất kỳ máy tính nào
 #khác (Linux,Windows, MacOS, hoặc máy chủ Cloud) để chạy ngay lập tức mà k cần cài Java, Gradle hay interlliJ trên máy đó
 #**Tác dụng lớn nhất của Dockerfile;
 #-Stage 1 (BUILD - Phân xưởng thô): Kéo bộ công cụ cồng kềnh đầy đủ (JDK - Java Development Kit) về để biên dịch code Java của bạn thành file .jar.
 #-Stage 2 (RUN - Phân xưởng tinh gọn): Chỉ lấy môi trường chạy core cực nhẹ (JRE - Java Runtime Environment) và copy file .jar từ Stage 1 sang để chạy.
 #Toàn bộ mã nguồn gốc, công cụ Gradle rác ở Stage 1 bị vứt bỏ hoàn toàn. Kết quả là bạn có một sản phẩm cuối cùng cực kỳ nhẹ và bảo mật để đem đi chạy thực tế.


## Vì project Spring Boot của bạn đang sử dụng Java 17
 ## nên Docker cần JDK 17 để có thể BUILD project.
 ##
 ## "AS builder" đặt tên cho stage này là "builder"
 ## để sau này stage 2 có thể lấy file JAR từ đây.
# ============================================================
# STAGE 1 - BUILD
# ============================================================

FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Gradle Wrapper
COPY gradlew .
COPY gradle ./gradle

# Gradle configuration
COPY build.gradle .
COPY settings.gradle .

# Source code
COPY src ./src

# Grant execute permission
RUN chmod +x gradlew

# Build Spring Boot JAR
RUN ./gradlew clean bootJar --no-daemon


# ============================================================
# STAGE 2 - RUN
# ============================================================

FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Spring Boot port
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]

#LUỒNG HOẠT ĐỘNG:                    Dockerfile
                 #                        │
                 #                        ▼
                 #              ┌─────────────────┐
                 #              │    STAGE 1      │
                 #              │                 │
                 #              │    JDK 17       │
                 #              │    Gradle       │
                 #              │    source code  │
                 #              │                 │
                 #              │  ./gradlew      │
                 #              │  clean bootJar   │
                 #              └────────┬────────┘
                 #                       │
                 #                       │ tạo
                 #                       ▼
                 #              build/libs/*.jar
                 #                       │
                 #                       │ COPY
                 #                       ▼
                 #              ┌─────────────────┐
                 #              │    STAGE 2      │
                 #              │                 │
                 #              │    JRE 17       │
                 #              │                 │
                 #              │    app.jar      │
                 #              │                 │
                 #              │ java -jar app.jar│
                 #              └────────┬────────┘
                 #                       │
                 #                       ▼
                 #                 Spring Boot
                 #                 localhost:8080