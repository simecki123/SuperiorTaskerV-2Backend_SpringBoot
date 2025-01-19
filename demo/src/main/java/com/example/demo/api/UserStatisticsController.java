package com.example.demo.api;

import com.example.demo.config.openapi.ShowAPI;
import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoUserFoundException;
import com.example.demo.models.dto.UserStatisticsDto;
import com.example.demo.service.UserStatisticsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/stats")
@Slf4j
@AllArgsConstructor
@ShowAPI
public class UserStatisticsController {
    private final UserStatisticsService userStatisticsService;

    @GetMapping("/get-user-stats")
    public ResponseEntity<UserStatisticsDto> getUserStats(@RequestParam (required = true) String userId) {
        try {
            log.info("Fetching user statistics... ");
            return ResponseEntity.ok(userStatisticsService.getUserStats(userId));

        } catch (NoGroupFoundException | NoUserFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Error e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
