FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21@sha256:ccd0b89bf1e3a4de6ce444a27c7fe9282a680cdee390a9ba3fc6a306a2da7d58
ENV TZ="Europe/Oslo"
COPY build/libs/app.jar app.jar
CMD ["-jar","app.jar"]