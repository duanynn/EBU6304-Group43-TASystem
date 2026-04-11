package bupt.is.ta.web;

import bupt.is.ta.store.DataStore;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.nio.file.Path;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        String dataRealPath = ctx.getRealPath("/WEB-INF/data");
        try {
            DataStore.getInstance().init(Path.of(dataRealPath));
        } catch (Exception e) {
            ctx.log("Failed to initialize DataStore", e);
        }
    }
}

