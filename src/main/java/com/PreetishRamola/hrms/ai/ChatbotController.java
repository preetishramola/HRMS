package com.PreetishRamola.hrms.ai;

import com.PreetishRamola.hrms.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final HrChatbotService hrChatbotService;

    @PostMapping("/ask")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HrChatbotService.ChatResponse>> ask(
            @RequestParam String question,
            @RequestParam(required = false) String sessionId,
            @AuthenticationPrincipal UserDetails currentUser) {
        HrChatbotService.ChatResponse response = hrChatbotService.askQuestion(
                question, currentUser.getUsername(), sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
