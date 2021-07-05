ARG JDK_IMAGE

FROM ${JDK_IMAGE}

RUN java -version

COPY sandbox.jar /home/sandbox.jar

ENV SANDBOX_WORK_DIR=/tmp/sandbox

ENTRYPOINT [ "java", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-jar", "/home/sandbox.jar" ]

CMD [ "io.javaalmanac.sandbox.service.RequestDispatcher" ]
