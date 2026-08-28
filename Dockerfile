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
# STAGE 1: BUILD
# Dùng JDK 17 để BUILD project
# ============================================================

FROM eclipse-temurin:17-jdk AS builder

# ============================================================
# Tạo thư mục /app bên trong container
# ============================================================
#
# Docker container có filesystem riêng.
#
# WORKDIR /app có nghĩa:
# "Từ bây giờ hãy làm việc trong thư mục /app"
#
# Các lệnh COPY, RUN... phía sau sẽ chủ yếu làm việc
# trong thư mục /app.
WORKDIR /app


# ============================================================
# COPY GRADLE WRAPPER
# ============================================================
# Copy file gradlew từ project máy tính của bạn
# vào thư mục /app trong container.
#
# Project của bạn hiện có:
#
# project/
# ├── gradlew
# ├── gradlew.bat
# ├── gradle/
# ├── build.gradle
# ├── settings.gradle
# └── src/
#
# Linux container sử dụng "gradlew"
# chứ không sử dụng "gradlew.bat".
# Copy Gradle Wrapper của project
COPY gradlew .

# Copy toàn bộ thư mục gradle từ project
# vào container.
#
# Thư mục này chứa Gradle Wrapper và những file
# cần thiết để ./gradlew có thể hoạt động.
COPY gradle ./gradle


# ============================================================
# COPY CẤU HÌNH GRADLE
# ============================================================

# Copy build.gradle vào container.
#
# File này chứa cấu hình build của project:
# - Spring Boot
# - dependency
# - Java version
# - plugin
# - cấu hình build...
COPY build.gradle .

# Copy settings.gradle vào container.
#
# File này chứa cấu hình cấp project của Gradle.
COPY settings.gradle .


# ============================================================
# CẤP QUYỀN CHẠY CHO gradlew
# ============================================================

# Container đang sử dụng Linux.
#
# Linux yêu cầu file gradlew phải có quyền execute
# thì mới có thể chạy:
#
# ./gradlew clean bootJar
#
# chmod +x = cấp quyền thực thi cho file.
RUN chmod +x gradlew


# ============================================================
# COPY SOURCE CODE
# ============================================================

# Copy thư mục src từ project của bạn
# vào thư mục /app/src trong container.
#
# Đây chính là source code Spring Boot của bạn:
#
# src/
# ├── main/
# │   ├── java/
# │   └── resources/
# └── test/
COPY src ./src


# ============================================================
# BUILD SPRING BOOT
# ============================================================

# Chạy Gradle Wrapper để BUILD project.
#
# clean:
#   Xóa kết quả build cũ.
#
# bootJar:
#   Build project Spring Boot thành file .jar
#
# --no-daemon:
#   Không chạy Gradle Daemon trong container.
#   Container chỉ cần build xong rồi kết thúc quá trình build.
#
# Sau khi lệnh này chạy thành công,
# Gradle sẽ tạo JAR tại:
#
# /app/build/libs/
#
# Ví dụ:
#
# /app/build/libs/systemanagercv-0.0.1-SNAPSHOT.jar
#
RUN ./gradlew clean bootJar --no-daemon


# ============================================================
# STAGE 2: RUN
# ============================================================
#
# Sau khi build xong, chúng ta không cần JDK nữa.
# Chỉ cần JRE 17 để chạy file JAR.
#
# JRE nhẹ hơn JDK vì JRE chủ yếu dùng để chạy Java application.
#
# Đây chính là lý do chúng ta tách thành 2 stage.
FROM eclipse-temurin:17-jre

# Thư mục làm việc
WORKDIR /app


# ============================================================
# COPY FILE JAR TỪ STAGE 1
# ============================================================

# --from=builder
# có nghĩa:
#
# "Lấy file từ stage có tên builder ở phía trên"
#
# Stage 1 đã build ra:
#
# /app/build/libs/*.jar
#
# Sau đó copy file JAR đó sang stage 2
# và đổi tên thành:
#
# /app/app.jar
#
COPY --from=builder /app/build/libs/*.jar app.jar


# ============================================================
# KHAI BÁO PORT
# ============================================================

# Spring Boot của bạn mặc định chạy port 8080.
#
# EXPOSE 8080 chỉ có ý nghĩa:
# "Container này dự kiến sử dụng port 8080"
#
# Nó KHÔNG tự động mở port ra máy tính.
#
# Việc mapping port sẽ được thực hiện trong
# docker-compose.yml hoặc khi docker run.
EXPOSE 8080


# ============================================================
# LỆNH KHỞI ĐỘNG APPLICATION
# ============================================================

# Khi container được START,
# Docker sẽ chạy:
#
# java -jar app.jar
#
# Đây chính là lệnh chạy Spring Boot application
# trên máy tính của bạn.
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