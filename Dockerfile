# Step 1: Use OpenJDK image
FROM openjdk:17-jdk-slim

# Step 2: Set workdir
WORKDIR /app

# Step 3: Copy jar file (adjust your jar file name after build)
COPY target/foodiesapi-0.0.1-SNAPSHOT.jar app.jar

# Step 4: Expose the port (important for Render)
EXPOSE 8080

# Step 5: Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
