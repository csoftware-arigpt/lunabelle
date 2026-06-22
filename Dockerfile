FROM eclipse-temurin:21-jdk

ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    GRADLE_USER_HOME=/cache/gradle \
    DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y --no-install-recommends \
        curl unzip git ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# cmdline-tools baked into the image (small). The heavy SDK packages (platforms,
# build-tools) are installed at runtime into the mounted ANDROID_HOME volume so they
# live on the external drive, not the image layer / root disk.
ARG CMDLINE_TOOLS_VERSION=13114758
RUN mkdir -p /opt/cmdline-tools && \
    curl -fsSL -o /tmp/cmdline-tools.zip \
      "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip" && \
    unzip -q /tmp/cmdline-tools.zip -d /opt/cmdline-tools && \
    mv /opt/cmdline-tools/cmdline-tools /opt/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

ENV PATH="${PATH}:/opt/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools"

# World-writable mount points so the container can run as the host uid (--user)
RUN mkdir -p /cache /opt/android-sdk && chmod -R 777 /cache /opt/android-sdk

COPY docker/entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

WORKDIR /workspace
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["./gradlew", "assembleDebug", "--no-daemon", "--stacktrace"]
