ARG JDK_IMAGE

FROM ${JDK_IMAGE}
RUN java -version
COPY sandbox.jar /home/sandbox.jar

ENV SANDBOX_WORK_DIR=/tmp/sandbox
ARG VMARGS=""
ENV VMARGS=$VMARGS
RUN echo Extra vm args ${VMARGS}

ENTRYPOINT java ${VMARGS} -jar /home/sandbox.jar io.javaalmanac.sandbox.service.RequestDispatcher
