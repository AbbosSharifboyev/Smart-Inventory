package uz.pdp.smartinventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uz.pdp.smartinventory.service.OrderServiceImpl;
import uz.pdp.smartinventory.service.ProductService;
import uz.pdp.smartinventory.service.ReportService;
import uz.pdp.smartinventory.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ProductService productService;
    private final OrderServiceImpl orderService;
    private final UserService userService;

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadReport() throws Exception{

        Map<String, Object> data = new HashMap<>();
        data.put("totalOrders",orderService.countTodayOrders());
        data.put("lowStockProducts",productService.getLowStockProductsList());
        data.put("users",userService.getAllUsers());
        data.put("reportDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        byte[] pdfBytes = reportService.generatedDashboardReport(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "SmartStore_Report.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
