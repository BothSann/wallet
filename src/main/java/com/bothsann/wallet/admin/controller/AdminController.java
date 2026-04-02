package com.bothsann.wallet.admin.controller;

import com.bothsann.wallet.admin.dto.UserDetailResponse;
import com.bothsann.wallet.admin.dto.UserSummaryResponse;
import com.bothsann.wallet.admin.service.AdminService;
import com.bothsann.wallet.shared.dto.PageResponse;
import com.bothsann.wallet.user.entity.User;
import com.bothsann.wallet.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserSummaryResponse>> getAllUsers(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDetailResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<UserSummaryResponse> deactivateUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(adminService.deactivateUser(id, currentUser.getId()));
    }

    @GetMapping("/wallets")
    public ResponseEntity<PageResponse<WalletResponse>> getAllWallets(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllWallets(pageable));
    }
}
