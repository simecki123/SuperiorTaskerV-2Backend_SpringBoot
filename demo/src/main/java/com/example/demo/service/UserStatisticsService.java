package com.example.demo.service;

import com.example.demo.models.dto.UserStatisticsDto;

public interface UserStatisticsService {
    UserStatisticsDto getUserStats(String userId);
}
