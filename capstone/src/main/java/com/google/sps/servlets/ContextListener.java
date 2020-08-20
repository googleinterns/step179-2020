package com.google.sps.servlets;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ContextListener implements ServletContextListener {
  private ScheduledExecutorService executorService;

  public void contextInitialized(ServletContextEvent sce) {
    executorService = Executors.newScheduledThreadPool(5);
    sce.getServletContext().setAttribute("executorService", executorService);
  }

  public void contextDestroyed(ServletContextEvent sce) {
    executorService.shutdownNow();
  }
}
