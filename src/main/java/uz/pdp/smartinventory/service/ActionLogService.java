package uz.pdp.smartinventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.smartinventory.model.domain.ActionLog;
import uz.pdp.smartinventory.repository.ActionLogRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActionLogService {

    private final ActionLogRepository repository;

    public void saveLog(String message, String type){
        ActionLog log = new ActionLog();
        log.setMessage(message);
        log.setType((type == null || type.isEmpty()) ? "INFO" : type);
        repository.save(log);
    }

    public List<ActionLog> getRecentActivities(){
        return repository.findTop5ByOrderByCreatedAtDesc();
    }
}
