ARG JDK_IMAGE

FROM ${JDK_IMAGE}

RUN java --version

COPY sandbox.jar /sandbox.jar

ENTRYPOINT [ "java", "-jar", "/sandbox.jar" ]

CMD [ "io.javaalmanac.sandbox.service.VersionHandler" ]
