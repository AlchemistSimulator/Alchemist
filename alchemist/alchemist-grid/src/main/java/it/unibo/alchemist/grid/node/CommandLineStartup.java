/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibo.alchemist.grid.node;

import java.awt.Image;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import javax.swing.ImageIcon;
import org.apache.ignite.IgniteState;
import org.apache.ignite.IgnitionListener;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.internal.util.typedef.G;
import org.apache.ignite.internal.util.typedef.X;
import org.apache.ignite.startup.cmdline.AboutDialog;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static org.apache.ignite.IgniteState.STOPPED;
import static org.apache.ignite.IgniteState.STOPPED_ON_SEGMENTATION;
import static org.apache.ignite.IgniteSystemProperties.IGNITE_PROG_NAME;
import static org.apache.ignite.IgniteSystemProperties.IGNITE_RESTART_CODE;
import static org.apache.ignite.internal.IgniteVersionUtils.ACK_VER_STR;
import static org.apache.ignite.internal.IgniteVersionUtils.COPYRIGHT;
import static org.apache.ignite.internal.IgniteVersionUtils.RELEASE_DATE_STR;
import static org.apache.ignite.internal.IgniteVersionUtils.VER_STR;

/**
 * This class defines command-line Ignite startup. This startup can be used to start Ignite
 * outside of any hosting environment from command line. This startup is a Java application with
 * {@link #main(String[])} method that accepts command line arguments. It accepts just one
 * parameter which is Spring XML configuration file path. You can run this class from command
 * line without parameters to get help message.
 * <p>
 * Note that scripts {@code ${IGNITE_HOME}/bin/ignite.{sh|bat}} shipped with Ignite use
 * this startup and you can use them as an example.
 */
public final class CommandLineStartup {

    /** Build date. */
    private static Date releaseDate;

    private static final Logger L = LoggerFactory.getLogger(CommandLineStartup.class);

    /**
     * Static initializer.
     */
    static {

        // Mac OS specific customizations: app icon and about dialog.
        try {
            releaseDate = new SimpleDateFormat("ddMMyyyy", Locale.US).parse(RELEASE_DATE_STR);

            final Class<?> appCls = Class.forName("com.apple.eawt.Application");

            final Object osxApp = appCls.getDeclaredMethod("getApplication").invoke(null);

            final String icoPath = "logo_ignite_128x128.png";

            final URL url = CommandLineStartup.class.getResource(icoPath);

            assert url != null : "Unknown icon path: " + icoPath;

            final ImageIcon ico = new ImageIcon(url);

            appCls.getDeclaredMethod("setDockIconImage", Image.class).invoke(osxApp, ico.getImage());

            // Setting Up about dialog
            final Class<?> aboutHndCls = Class.forName("com.apple.eawt.AboutHandler");

            final URL bannerUrl = CommandLineStartup.class.getResource("logo_ignite_48x48.png");

            final Object aboutHndProxy = Proxy.newProxyInstance(
                appCls.getClassLoader(),
                new Class<?>[] {aboutHndCls},
                new InvocationHandler() {
                    @Override public Object invoke(final Object proxy, final Method mtd, final Object[] args) throws Throwable {
                        AboutDialog.centerShow("Ignite Node", bannerUrl.toExternalForm(), VER_STR,
                            releaseDate, COPYRIGHT);

                        return null;
                    }
                }
            );

            appCls.getDeclaredMethod("setAboutHandler", aboutHndCls).invoke(osxApp, aboutHndProxy);
        } catch (Exception ignore) {
            L.error(ignore.toString());
        }
    }

    /**
     * Enforces singleton.
     */
    private CommandLineStartup() {
        // No-op.
    }

    /**
     * Exists with optional error message, usage show and exit code.
     *
     * @param errMsg Optional error message.
     * @param showUsage Whether or not to show usage information.
     * @param exitCode Exit code.
     */
    private static void exit(@Nullable final String errMsg, final boolean showUsage, final int exitCode) {
        if (errMsg != null) {
            X.error(errMsg);
        }

        String runner = System.getProperty(IGNITE_PROG_NAME, "ignite.{sh|bat}");

        final int space = runner.indexOf(' ');

        runner = runner.substring(0, space == -1 ? runner.length() : space);

        if (showUsage) {
            final boolean ignite = runner.contains("ignite.");

            X.error(
                "Usage:",
                "    " + runner + (ignite ? " [?]|[path {-v}{-np}]|[-i]" : " [?]|[-v]"),
                "    Where:",
                "    ?, /help, -help, - show this message.",
                "    -v               - verbose mode (quiet by default).",
                "    -np              - no pause on exit (pause by default)",
                "    -nojmx           - disable JMX monitoring (enabled by default)");

            if (ignite) {
                X.error(
                    "    -i              - interactive mode (choose configuration file from list).",
                    "    path            - path to Spring XML configuration file.",
                    "                      Path can be absolute or relative to IGNITE_HOME.",
                    " ",
                    "Spring file should contain one bean definition of Java type",
                    "'org.apache.ignite.configuration.IgniteConfiguration'. Note that bean will be",
                    "fetched by the type and its ID is not used.");
            }
        }

        System.exit(exitCode);
    }

    /**
     * Main entry point.
     *
     * @param configFile Ignite config file's path
     */
    public static void runNode(final String configFile) {

            X.println("Ignite Command Line Startup, ver. " + ACK_VER_STR);
            X.println(COPYRIGHT);
            X.println();

        final String cfg = configFile;
        // Name of the grid loaded from the command line (unique in JVM).
        final String igniteInstanceName;

        try {
            igniteInstanceName = G.start(cfg).name();
        } catch (Exception e) {
            L.error(e.toString());

            String note = "";

            if (X.hasCause(e, ClassNotFoundException.class)) {
                note = "\nNote! You may use 'USER_LIBS' environment variable to specify your classpath.";
            }

            exit("Failed to start grid: " + e.getMessage() + note, false, -1);

            return;
        }

        // Exit latch for the grid loaded from the command line.
        final CountDownLatch latch = new CountDownLatch(1);

        G.addListener(new IgnitionListener() {
            @Override public void onStateChange(final String name, final IgniteState state) {
                // Skip all grids except loaded from the command line.
                if (!F.eq(igniteInstanceName, name)) {
                    return;
                }

                if (state == STOPPED || state == STOPPED_ON_SEGMENTATION) {
                    latch.countDown();
                }
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            X.error("Start was interrupted (exiting): " + e.getMessage());
        }

        final String code = System.getProperty(IGNITE_RESTART_CODE);

        if (code != null) {
            try {
                System.exit(Integer.parseInt(code));
            } catch (NumberFormatException ignore) {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }
}