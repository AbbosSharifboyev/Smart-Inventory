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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uz.pdp.smartinventory.criteria.OrderCriteria;
import uz.pdp.smartinventory.criteria.ProductCriteria;
import uz.pdp.smartinventory.model.dto.OrderDto;
import uz.pdp.smartinventory.model.dto.StockMovementReportDto;
import uz.pdp.smartinventory.model.enums.OrderStatus;
import uz.pdp.smartinventory.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;
    private final ProductService productService;
    private final OrderServiceImpl orderService;
    private final UserService userService;
    private final StockMovementService stockMovementService;


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String reportsPage(
            @RequestParam(defaultValue = "sales") String type,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
            ) {
        // default time last 30 days
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();

        model.addAttribute("type", type);
        model.addAttribute("from", from);
        model.addAttribute("to",   to);

        switch (type){
            case "warehouse" -> buildWareHouseModel(model);
            case "movements" -> buildMovementsModel(model, from, to, pageable);
            default          -> buildSalesModel(model, from, to);
        }
        return "reports/list";
    }

    @GetMapping("/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
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


    @GetMapping("/download")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
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

    private void buildSalesModel(Model model, LocalDate from, LocalDate to){

        OrderCriteria criteria = new OrderCriteria();
        criteria.setDateFrom(from);
        criteria.setDateTo(to);
        criteria.setSize(1000);
        //criteria.setStatus(OrderStatus.COMPLETED);

        List<OrderDto> sales = orderService.getAll(criteria).getContent();

        model.addAttribute("orders",       sales);
        model.addAttribute("totalOrders",  sales.size());
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("users",        userService.getAllUsers());
    }

    private void buildWareHouseModel(Model model){

        ProductCriteria criteria = new ProductCriteria();
        criteria.setSize(1000);

        model.addAttribute("products", productService.getAll(criteria).getContent());
        model.addAttribute("lowStockProducts", productService.getLowStockProductsList());
        model.addAttribute("lowStockCount", productService.getLowStockProductsList().size());
    }

    private void buildMovementsModel(Model model, LocalDate from, LocalDate to, Pageable pageable){

        StockMovementReportDto report = stockMovementService.getMovementReport(from, to, pageable);

        model.addAttribute("movements",     report.getMovementPage().getContent());
        model.addAttribute("movementPage", report.getMovementPage());
        model.addAttribute("kirimCount",    report.getKirimCount());
        model.addAttribute("chiqimCount",   report.getChiqimCount());
        model.addAttribute("totalKirimSum", report.getTotalKirimSum());
    }
}
