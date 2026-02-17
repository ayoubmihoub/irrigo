# Étape 1 : Build de l'application
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Étape 2 : Exécution
FROM eclipse-temurin:17-jre
WORKDIR /app
# On ajoute le "/" à la fin de la destination pour s'assurer que Docker le traite comme un dossier
# Ou on nomme explicitement le fichier de destination.
COPY --from=build /app/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]