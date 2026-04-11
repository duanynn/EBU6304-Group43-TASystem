package bupt.is.ta.service;

import bupt.is.ta.model.Job;
import bupt.is.ta.store.DataStore;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JobService {

    private final DataStore store = DataStore.getInstance();

    public List<Job> listOpenJobs() {
        return store.getJobs().stream()
                .filter(Job::isOpen)
                .collect(Collectors.toList());
    }

    public List<Job> listJobsByMo(String moId) {
        return store.getJobs().stream()
                .filter(j -> moId.equals(j.getMoId()))
                .collect(Collectors.toList());
    }

    public Optional<Job> findById(String id) {
        return store.getJobs().stream()
                .filter(j -> j.getId().equals(id))
                .findFirst();
    }

    public void save(Job job) throws Exception {
        synchronized (store) {
            if (job.getId() == null) {
                store.addJob(job);
            } else {
                store.updateJob(job);
            }
            store.saveAll();
        }
    }
}

