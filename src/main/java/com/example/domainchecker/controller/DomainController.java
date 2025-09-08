package com.example.domainchecker.controller;

import com.example.domainchecker.entity.DomainResult;
import com.example.domainchecker.repository.DomainResultRepository;
import com.example.domainchecker.service.DomainCheckerService;
import com.example.domainchecker.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class DomainController {
    
    @Autowired
    private DomainCheckerService domainCheckerService;
    
    @Autowired
    private DomainResultRepository domainResultRepository;
    
    @Autowired
    private ExportService exportService;

    /**
     * Show the main form page
     */
    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("domain", "");
        return "index";
    }
    
    /**
     * Process domain check form submission
     */
    @PostMapping("/domains")
    public String checkDomain(@RequestParam("domain") String domain, 
                             RedirectAttributes redirectAttributes) {
        try {
            if (domain == null || domain.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please enter a domain name");
                return "redirect:/";
            }
            
            // Perform domain check
            DomainResult result = domainCheckerService.checkDomain(domain.trim());
            
            // Add success message with result details
            redirectAttributes.addFlashAttribute("success", 
                "Domain check completed successfully for: " + result.getDomain());
            redirectAttributes.addFlashAttribute("latestResult", result);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error checking domain: " + e.getMessage());
        }
        
        return "redirect:/domains/list";
    }
    
    /**
     * Show list of all domain check results
     */
    @GetMapping("/domains/list")
    public String listDomains(
            Model model,
            @RequestParam(value = "domain", required = false) String domainFilter,
            @RequestParam(value = "reachable", required = false) String reachable,
            @RequestParam(value = "safety", required = false) String safety,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page
    ) {
        try {
            List<DomainResult> all = domainResultRepository.findAllOrderByCreatedAtDesc();
            List<DomainResult> results = applyFilters(all, domainFilter, reachable, safety, startDate, endDate);

            // Pagination (page size 50)
            int pageSize = 50;
            int total = results.size();
            int from = Math.min(page * pageSize, total);
            int to = Math.min(from + pageSize, total);
            List<DomainResult> pageContent = results.subList(from, to);
            int totalPages = (int) Math.ceil(total / (double) pageSize);

            model.addAttribute("results", pageContent);
            model.addAttribute("totalResults", total);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);

            // Calculate statistics on filtered set
            long reachableCount = results.stream().mapToLong(r -> Boolean.TRUE.equals(r.getReachable()) ? 1 : 0).sum();
            long safeCount = results.stream().mapToLong(r ->
                    r.getVirusCheck() != null && r.getVirusCheck().toLowerCase().contains("safe") ? 1 : 0).sum();

            model.addAttribute("reachableCount", reachableCount);
            model.addAttribute("safeCount", safeCount);

            // Preserve filter values in the UI
            model.addAttribute("domainFilter", Objects.toString(domainFilter, ""));
            model.addAttribute("reachableFilter", Objects.toString(reachable, ""));
            model.addAttribute("safetyFilter", Objects.toString(safety, ""));
            model.addAttribute("startDateFilter", normalizeIsoDate(startDate));
            model.addAttribute("endDateFilter", normalizeIsoDate(endDate));

            // Query string for export links
            model.addAttribute("filtersQuery", buildFiltersQuery(domainFilter, reachable, safety,
                    normalizeIsoDate(startDate), normalizeIsoDate(endDate)));

        } catch (Exception e) {
            model.addAttribute("error", "Error loading domain results: " + e.getMessage());
            model.addAttribute("results", List.of());
            model.addAttribute("totalResults", 0);
            model.addAttribute("reachableCount", 0);
            model.addAttribute("safeCount", 0);
            model.addAttribute("domainFilter", "");
            model.addAttribute("reachableFilter", "");
            model.addAttribute("safetyFilter", "");
            model.addAttribute("startDateFilter", "");
            model.addAttribute("endDateFilter", "");
            model.addAttribute("filtersQuery", "");
        }

        return "domain-list";
    }
    
    /**
     * Get domain details by ID (for AJAX calls or detailed view)
     */
    @GetMapping("/domains/{id}")
    @ResponseBody
    public DomainResult getDomainResult(@PathVariable Long id) {
        return domainResultRepository.findById(id).orElse(null);
    }
    
    /**
     * Delete a domain result
     */
    @PostMapping("/domains/{id}/delete")
    public String deleteDomainResult(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (domainResultRepository.existsById(id)) {
                domainResultRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Domain result deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Domain result not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting domain result: " + e.getMessage());
        }
        
        return "redirect:/domains/list";
    }
    
    /**
     * Show bulk domain checking page
     */
    @GetMapping("/bulk")
    public String showBulkForm(Model model) {
        model.addAttribute("domains", "");
        return "bulk-check";
    }
    
    /**
     * Process bulk domain check (like PowerShell script)
     */
    @PostMapping("/domains/bulk")
    public String checkBulkDomains(@RequestParam("domains") String domainsText, 
                                  RedirectAttributes redirectAttributes) {
        try {
            if (domainsText == null || domainsText.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please enter domain names");
                return "redirect:/bulk";
            }
            
            // Parse domains like PowerShell script
            String[] lines = domainsText.split("\\r?\\n");
            int processedCount = 0;
            int successCount = 0;
            
            for (String rawDomain : lines) {
                String domain = rawDomain.trim().replace("\"", "").replace("\t", "");
                if (domain.isEmpty()) continue;
                
                try {
                    domainCheckerService.checkDomain(domain);
                    successCount++;
                } catch (Exception e) {
                    System.err.println("Error checking domain " + domain + ": " + e.getMessage());
                }
                processedCount++;
            }
            
            redirectAttributes.addFlashAttribute("success", 
                "Bulk check completed! Processed " + processedCount + " domains, " + 
                successCount + " successful checks.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error processing bulk domains: " + e.getMessage());
        }
        
        return "redirect:/domains/list";
    }
    
    /**
     * Quick check endpoint for API access
     */
    @PostMapping("/api/check")
    @ResponseBody
    public DomainResult apiCheckDomain(@RequestParam("domain") String domain) {
        try {
            return domainCheckerService.checkDomain(domain.trim());
        } catch (Exception e) {
            DomainResult errorResult = new DomainResult();
            errorResult.setDomain(domain);
            errorResult.setReachable(false);
            errorResult.setHttpStatus(-1);
            errorResult.setVirusCheck("Error: " + e.getMessage());
            return errorResult;
        }
    }
    
    /**
     * Export all domain results to CSV
     */
    @GetMapping("/export/csv")
    public ResponseEntity<ByteArrayResource> exportToCSV(
            @RequestParam(value = "domain", required = false) String domainFilter,
            @RequestParam(value = "reachable", required = false) String reachable,
            @RequestParam(value = "safety", required = false) String safety,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        try {
            List<DomainResult> all = domainResultRepository.findAllOrderByCreatedAtDesc();
            List<DomainResult> results = applyFilters(all, domainFilter, reachable, safety, startDate, endDate);
            byte[] csvData = exportService.exportToCSV(results);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "domain_checker_results_" + timestamp + ".csv";
            
            ByteArrayResource resource = new ByteArrayResource(csvData);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .contentLength(csvData.length)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Export all domain results to PDF
     */
    @GetMapping("/export/pdf")
    public ResponseEntity<ByteArrayResource> exportToPDF(
            @RequestParam(value = "domain", required = false) String domainFilter,
            @RequestParam(value = "reachable", required = false) String reachable,
            @RequestParam(value = "safety", required = false) String safety,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        try {
            List<DomainResult> all = domainResultRepository.findAllOrderByCreatedAtDesc();
            List<DomainResult> results = applyFilters(all, domainFilter, reachable, safety, startDate, endDate);
            byte[] pdfData = exportService.exportToPDF(results);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "domain_checker_report_" + timestamp + ".pdf";
            
            ByteArrayResource resource = new ByteArrayResource(pdfData);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfData.length)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Export all domain results to Word
     */
    @GetMapping("/export/word")
    public ResponseEntity<ByteArrayResource> exportToWord(
            @RequestParam(value = "domain", required = false) String domainFilter,
            @RequestParam(value = "reachable", required = false) String reachable,
            @RequestParam(value = "safety", required = false) String safety,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        try {
            List<DomainResult> all = domainResultRepository.findAllOrderByCreatedAtDesc();
            List<DomainResult> results = applyFilters(all, domainFilter, reachable, safety, startDate, endDate);
            byte[] wordData = exportService.exportToWord(results);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "domain_checker_report_" + timestamp + ".docx";
            
            ByteArrayResource resource = new ByteArrayResource(wordData);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .contentLength(wordData.length)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== Helpers =====
    private List<DomainResult> applyFilters(
            List<DomainResult> input,
            String domainFilter,
            String reachable,
            String safety,
            String startDate,
            String endDate
    ) {
        LocalDateTime start = parseDateStart(startDate);
        LocalDateTime end = parseDateEnd(endDate);

        return input.stream()
                .filter(r -> domainFilter == null || domainFilter.isBlank() ||
                        (r.getDomain() != null && r.getDomain().toLowerCase().contains(domainFilter.toLowerCase())))
                .filter(r -> reachable == null || reachable.isBlank() ||
                        ("yes".equalsIgnoreCase(reachable) && Boolean.TRUE.equals(r.getReachable())) ||
                        ("no".equalsIgnoreCase(reachable) && Boolean.FALSE.equals(r.getReachable())))
                .filter(r -> safety == null || safety.isBlank() ||
                        ("safe".equalsIgnoreCase(safety) &&
                                r.getVirusCheck() != null && r.getVirusCheck().toLowerCase().contains("safe")) ||
                        ("unsafe".equalsIgnoreCase(safety) &&
                                r.getVirusCheck() != null && r.getVirusCheck().toLowerCase().contains("unsafe")))
                .filter(r -> {
                    if (r.getCreatedAt() == null) return false;
                    boolean afterStart = (start == null) || !r.getCreatedAt().isBefore(start);
                    boolean beforeEnd = (end == null) || !r.getCreatedAt().isAfter(end);
                    return afterStart && beforeEnd;
                })
                .collect(Collectors.toList());
    }

    private LocalDateTime parseDateStart(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) return null;
        try {
            LocalDate d = LocalDate.parse(isoDate); // expects yyyy-MM-dd
            return d.atStartOfDay();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalDateTime parseDateEnd(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) return null;
        try {
            LocalDate d = LocalDate.parse(isoDate);
            return d.atTime(23, 59, 59);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String normalizeIsoDate(String isoDate) {
        // just return the same value if already yyyy-MM-dd; used to prefill inputs
        if (isoDate == null) return "";
        try {
            LocalDate.parse(isoDate);
            return isoDate;
        } catch (Exception e) {
            return "";
        }
    }

    private String buildFiltersQuery(String domainFilter, String reachable, String safety,
                                     String startDate, String endDate) {
        StringBuilder sb = new StringBuilder();
        appendQuery(sb, "domain", domainFilter);
        appendQuery(sb, "reachable", reachable);
        appendQuery(sb, "safety", safety);
        appendQuery(sb, "startDate", startDate);
        appendQuery(sb, "endDate", endDate);
        return sb.toString();
    }

    private void appendQuery(StringBuilder sb, String key, String value) {
        if (value == null || value.isBlank()) return;
        if (sb.length() > 0) sb.append('&');
        sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8)).append('=')
          .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}