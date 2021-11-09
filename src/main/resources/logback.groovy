import ch.qos.logback.classic.Level

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}
logger("org.reflections.Reflections", Level.OFF)
root(WARN)
