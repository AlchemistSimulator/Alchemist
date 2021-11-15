appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{20} - %msg%n"
    }
}
logger("org.reflections.Reflections", OFF)
root(WARN, ["STDOUT"])
