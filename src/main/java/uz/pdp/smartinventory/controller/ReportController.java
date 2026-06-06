package uz.pdp.smartinventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.smartinventory.criteria.OrderCriteria;
import uz.pdp.smartinventory.criteria.ProductCriteria;
import uz.pdp.smartinventory.model.dto.OrderDto;
import uz.pdp.smartinventory.model.dto.StockMovementReportDto;
import uz.pdp.smartinventory.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;
    private final ProductService productService;
    private final OrderServiceImpl orderService;
    private final UserService userService;
    private final StockMovementService stockMovementService;


    // ==========================================
    //  1. JADVAL MA'LUMOTLARI UCHUN ENDPOINTLAR (JSON)
    // ==========================================

    //  A. Savdo (Sales) hisoboti ma'lumotlari
    @GetMapping("/sales")
    //@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Map<String, Object>> getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to){

        // default time last 30 days
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();

        OrderCriteria criteria = new OrderCriteria();
        criteria.setDateFrom(from);
        criteria.setDateTo(to);
        criteria.setSize(1000);

        List<OrderDto> sales = orderService.getAll(criteria).getContent();

        Map<String, Object> response = new HashMap<>();
        response.put("orders", sales);
        response.put("totalOrders", sales.size());
        response.put("totalRevenue", orderService.getTotalRevenue());
        response.put("users", userService.getAllUsers());

        return ResponseEntity.ok(response);
    }

    // Ombor (Warehouse) qoldig'i hisoboti ma'lumotlari
    @GetMapping("/warehouse")
    //@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Map<String, Object>> getWarehouseReport(){
        ProductCriteria criteria = new ProductCriteria();
        criteria.setSize(1000);

        Map<String, Object> response = new HashMap<>();
        response.put("products", productService.getAll(criteria).getContent());
        response.put("lowStockProducts", productService.getLowStockProductsList());
        response.put("lowStockCount", productService.getLowStockProductsList().size());

        return ResponseEntity.ok(response);
    }


    // Ombor Harakatlari (Movements) hisoboti ma'lumotlari
    @GetMapping("/movements")
    //@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<StockMovementReportDto> getMovementsReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){

        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();

        StockMovementReportDto report = stockMovementService.getMovementReport(from, to, pageable);
        return ResponseEntity.ok(report);

    }

    // ==========================================
    //  2. PDF FILE YUKLAB OLISH ENDPOINTLARI (BYTE)
    // ==========================================

    //  D. Tanlangan tur bo'yicha mukammal PDF hisobot yuklab olish
    @GetMapping("/pdf")
    //@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<byte[]> downloadPdf(
            @RequestParam(defaultValue = "sales") String type,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to)
        throws Exception {

        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();

        Map<String, Object> data = new HashMap<>();
        data.put("reportDate", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        data.put("from",from.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        data.put("to",  to.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        data.put("type",type);

        switch (type) {
            case "warehouse" -> {
                ProductCriteria criteria = new ProductCriteria();
                criteria.setSize(1000);

                data.put("products",         productService.getAll(criteria).getContent());
                data.put("lowStockCount",    productService.getLowStockProductsList().size());
                data.put("lowStockProducts", productService.getLowStockProductsList());
            }
            case "movements" -> {
                Pageable pdfPageable = PageRequest.of(
                        0, 2000, Sort.by(Sort.Direction.DESC, "createdAt"));
                StockMovementReportDto report = stockMovementService.getMovementReport(from, to, pdfPageable);

                data.put("movements",     report.getMovementPage().getContent());
                data.put("kirimCount",    report.getKirimCount());
                data.put("chiqimCount",   report.getChiqimCount());
                data.put("totalKirimSum", report.getTotalKirimSum());
            }
            default -> { //sales

                OrderCriteria salCriteria = new OrderCriteria();
                salCriteria.setDateFrom(from);
                salCriteria.setDateTo(to);
                salCriteria.setSize(1000);
                //salCriteria.setStatus(OrderStatus.COMPLETED);

                List<OrderDto> sales = orderService.getAll(salCriteria).getContent();

                data.put("orders",      sales);
                data.put("totalOrders", sales.size());
                data.put("totalRevenue",orderService.getTotalRevenue());
                data.put("users",       userService.getAllUsers());
            }
        }

        String templateName = "reports/pdf_" + type;
        byte[] pdfBytes = reportService.generateReport(data, templateName);

        String fileName = "SmartStore_" + type + "_"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"))
                + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", fileName);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    // Dashboard umumiy qisqacha hisobotini PDF yuklab olish
    @GetMapping("/download")
    //@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<byte[]> downloadReport() throws Exception{

        Map<String, Object> data = new HashMap<>();
        data.put("totalOrders",     orderService.countTodayOrders());
        data.put("lowStockProducts",productService.getLowStockProductsList());
        data.put("users",           userService.getAllUsers());
        data.put("reportDate",      LocalDateTime.now().
                format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        byte[] pdfBytes = reportService.generatedDashboardReport(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "SmartStore_Report.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
