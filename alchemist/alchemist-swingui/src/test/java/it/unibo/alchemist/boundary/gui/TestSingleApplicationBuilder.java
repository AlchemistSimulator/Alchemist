package it.unibo.alchemist.boundary.gui;

import javafx.application.Application;

public class TestSingleApplicationBuilder {
    public static void main(final String... args) {
        Application.launch(TestSingleApplication.class, "--use-pippo=pippo", "--use-pluto=pluto", "superpippo", "--mickey-mouse");
    }
}
