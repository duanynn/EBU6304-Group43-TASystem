package bupt.is.ta.service;

import bupt.is.ta.model.Application;
import bupt.is.ta.store.DataStore;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApplicationService {

    private final DataStore store = DataStore.getInstance();

    public List<Application> listByStudent(String studentId) {
        return store.getApplications().stream()
                .filter(a -> studentId.equals(a.getStudentId()))
                .collect(Collectors.toList());
    }

    public List<Application> listByJob(String jobId) {
        return store.getApplications().stream()
                .filter(a -> jobId.equals(a.getJobId()))
                .collect(Collectors.toList());
    }

    public Optional<Application> findById(String id) {
        return store.getApplications().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    public Optional<Application> findByStudentAndJob(String studentId, String jobId) {
        return store.getApplications().stream()
                .filter(a -> studentId.equals(a.getStudentId()) && jobId.equals(a.getJobId()))
                .findFirst();
    }

    public long countAcceptedByStudent(String studentId) {
        return store.getApplications().stream()
                .filter(a -> studentId.equals(a.getStudentId()))
                .filter(a -> a.getStatus() == Application.Status.ACCEPTED)
                .count();
    }

    public void create(Application app) throws Exception {
        synchronized (store) {
            store.addApplication(app);
            store.saveAll();
        }
    }

    public void update(Application app) throws Exception {
        synchronized (store) {
            store.updateApplication(app);
            store.saveAll();
        }
    }
}

