FROM openjdk:11

COPY /target/fda-flowable-1.2.0-RELEASE.jar /Flowable/
COPY /src/main/resources/application.properties /Flowable/
COPY /src/main/resources/application-dev.properties /Flowable/

WORKDIR /Flowable/
RUN pwd
RUN ls -ltr

EXPOSE 8086

ENTRYPOINT [ "nohup","java","-jar","fda-flowable-1.2.0-RELEASE.jar","--spring.config.name=application,application-dev","--spring.config.location=file:./","--spring.profiles.active=dev" ]
