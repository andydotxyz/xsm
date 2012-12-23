package com.rectang.xsm.dist;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

import java.lang.Exception;
import java.lang.String;

/**
 * A simple bootstrapping class for XSM outside of a web container.
 * <p/>
 * Created: 23/12/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class Main {
  public static void main(String[] arguments) throws Exception {
    printWelcome();

    startServer(getWarPath(arguments), 9090);
  }

  private static void printWelcome() {
    System.out.println();
    System.out.println(" _   ____   __       __          _ ");
    System.out.println("  \\  \\  /  /  )  ____) |        |  ");
    System.out.println("   \\  \\/  /  (  (___   |  |\\/|  |  ");
    System.out.println("    >    <    \\___  \\  |  |  |  |  ");
    System.out.println("   /  /\\  \\   ____)  ) |  |  |  |  ");
    System.out.println(" _/  /__\\  \\_(      (__|  |__|  |_ ");
    System.out.println();
  }

  private static String getWarPath(String[] arguments) {
    if (arguments.length > 0) {
      return arguments[0];
    }

    return "xsm.war";
  }

  private static void startServer(String war, int port) throws Exception {
    Server server = new Server();

    Connector connector = new SelectChannelConnector();
    connector.setPort(9090);
    server.addConnector(connector);

    WebAppContext webAppContext = new WebAppContext();
    webAppContext.setContextPath("/");
    webAppContext.setWar(war);

    server.setHandler(webAppContext);

    server.start();
    server.join();
  }
}
