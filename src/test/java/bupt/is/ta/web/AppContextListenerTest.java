package bupt.is.ta.web;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.mockito.Mockito.*;

class AppContextListenerTest {

    @Test
    void contextInitializedInitializesStoreWhenRealPathExists() throws Exception {
        ServletContext context = mock(ServletContext.class);
        ServletContextEvent event = new ServletContextEvent(context);
        when(context.getRealPath("/WEB-INF/data")).thenReturn(Files.createTempDirectory("ta-listener-test").toString());

        new AppContextListener().contextInitialized(event);

        verify(context, never()).log(eq("Failed to initialize DataStore"), any(Throwable.class));
    }

    @Test
    void contextInitializedLogsWhenRealPathIsMissing() {
        ServletContext context = mock(ServletContext.class);
        ServletContextEvent event = new ServletContextEvent(context);
        when(context.getRealPath("/WEB-INF/data")).thenReturn(null);

        new AppContextListener().contextInitialized(event);

        verify(context).log(eq("Failed to initialize DataStore"), any(Throwable.class));
    }
}
