FROM openjdk:12-jdk-alpine
COPY build/libs/coupons_api-1.0.0.jar coupons_api.jar
ENTRYPOINT ["java","-Dspring.profiles.active=local","-jar","coupons_api.jar"]