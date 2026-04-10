package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.models.UserReport;
import com.sandzaksell.sandzaksell.repositories.UserReportRepository;
import com.sandzaksell.sandzaksell.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserReportController {

    private final UserReportRepository userReportRepository;
    private final UserService userService;

    // Korisnik prijavljuje drugog korisnika
    @PostMapping("/api/user-reports")
    public ResponseEntity<?> reportUser(@RequestBody Map<String, String> body, Principal principal) {
        User reporter = userService.getUserByUsername(principal.getName());
        Long reportedId = Long.parseLong(body.get("reportedUserId"));
        String reason = body.get("reason");

        if (userReportRepository.existsByReporterIdAndReportedUserId(reporter.getId(), reportedId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Već ste prijavili ovog korisnika."));
        }

        User reported = userService.getUserById(reportedId);

        UserReport report = new UserReport();
        report.setReporter(reporter);
        report.setReportedUser(reported);
        report.setReason(reason);
        userReportRepository.save(report);

        return ResponseEntity.ok(Map.of("message", "Korisnik je prijavljen."));
    }

    // Admin dobija sve prijave korisnika
    @GetMapping("/api/admin/user-reports")
    public List<UserReport> getUserReports() {
        return userReportRepository.findAll();
    }

    // Admin odbacuje prijavu
    @DeleteMapping("/api/admin/user-reports/{id}")
    public ResponseEntity<?> dismissReport(@PathVariable Long id) {
        userReportRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}