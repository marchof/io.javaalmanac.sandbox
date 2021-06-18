ARG JDK_IMAGE

FROM ${JDK_IMAGE}

RUN java --version

COPY sandbox.jar /sandbox.jar

ENTRYPOINT [ "java", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-jar", "/sandbox.jar" ]

CMD [ "io.javaalmanac.sandbox.service.VersionHandler" ]
