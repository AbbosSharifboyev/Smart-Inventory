package uz.pdp.smartinventory.service;

import java.util.Map;

public interface WarehouseService {
    Map<String, Object> getWarehouseDashboardData(int page, String search, String status);
}
