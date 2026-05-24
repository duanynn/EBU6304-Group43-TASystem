package bupt.is.ta.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class InterviewResponseTest {

    @Test
    void needsResponseOnlyWhenInterviewingAndPending() {
        Application app = new Application();
        app.setStatus(Application.Status.INTERVIEWING);
        app.setInterviewResponse(Application.InterviewResponse.PENDING);
        assertTrue(app.needsInterviewResponse());
    }

    @Test
    void acceptedInviteDoesNotNeedResponse() {
        Application app = new Application();
        app.setStatus(Application.Status.INTERVIEWING);
        app.setInterviewResponse(Application.InterviewResponse.ACCEPTED);
        assertFalse(app.needsInterviewResponse());
    }

    @Test
    void declineFlowFields() {
        Application app = new Application();
        app.setStatus(Application.Status.INTERVIEWING);
        app.setInterviewResponse(Application.InterviewResponse.PENDING);
        app.setInterviewLocation("Lab 201");
        app.setInterviewRequiresWrittenTest(true);
        app.setInterviewScope("Java, OS");
        app.setInterviewMessage("Bring laptop");

        app.setInterviewResponse(Application.InterviewResponse.DECLINED);
        app.setInterviewRespondedAt(Instant.now());
        app.setStatus(Application.Status.REJECTED);

        assertEquals(Application.InterviewResponse.DECLINED, app.getInterviewResponse());
        assertEquals(Application.Status.REJECTED, app.getStatus());
        assertEquals("Lab 201", app.getInterviewLocation());
        assertTrue(app.isInterviewRequiresWrittenTest());
    }
}
