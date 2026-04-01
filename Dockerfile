# Usa uma imagem do Java JDK
# FROM openjdk:17-jdk-slim
FROM eclipse-temurin:17-jdk


# Define diretório de trabalho no container
WORKDIR /app

# Copia o arquivo JAR gerado para o container
COPY target/*.jar crm_mosia.jar

# Exponha a porta da aplicação
EXPOSE 443

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "crm_mosia.jar"]
