# Используем официальный образ с JDK
FROM eclipse-temurin:21-jdk-alpine-3.21

# Рабочая директория внутри контейнера
WORKDIR /app

# Копируем Maven Wrapper и POM файл
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Загружаем зависимости Maven
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код приложения
COPY src ./src

# Собираем приложение (без тестов)
RUN ./mvnw package -DskipTests

# Экспонируем порт 8080 для приложения
EXPOSE 8080

# Запуск JAR-файла, указав точный путь
CMD ["java", "-jar", "target/hotel-0.0.1-SNAPSHOT.jar"]
