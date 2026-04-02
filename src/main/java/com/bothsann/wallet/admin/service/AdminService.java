package com.bothsann.wallet.admin.service;

import com.bothsann.wallet.admin.dto.UserDetailResponse;
import com.bothsann.wallet.admin.dto.UserSummaryResponse;
import com.bothsann.wallet.shared.dto.PageResponse;
import com.bothsann.wallet.shared.exception.SelfDeactivationException;
import com.bothsann.wallet.shared.exception.UserNotFoundException;
import com.bothsann.wallet.shared.exception.WalletNotFoundException;
import com.bothsann.wallet.user.entity.User;
import com.bothsann.wallet.user.repository.UserRepository;
import com.bothsann.wallet.wallet.dto.WalletResponse;
import com.bothsann.wallet.wallet.entity.Wallet;
import com.bothsann.wallet.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public PageResponse<UserSummaryResponse> getAllUsers(Pageable pageable) {
        return PageResponse.of(userRepository.findAll(pageable).map(this::toUserSummary));
    }

    public UserDetailResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        Wallet wallet = walletRepository.findByUserId(id)
                .orElseThrow(WalletNotFoundException::new);
        return new UserDetailResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                toWalletResponse(wallet)
        );
    }

    @Transactional
    public UserSummaryResponse deactivateUser(UUID id, UUID currentAdminId) {
        if (id.equals(currentAdminId)) {
            throw new SelfDeactivationException();
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(false);
        userRepository.save(user);
        return toUserSummary(user);
    }

    public PageResponse<WalletResponse> getAllWallets(Pageable pageable) {
        return PageResponse.of(walletRepository.findAll(pageable).map(this::toWalletResponse));
    }

    private UserSummaryResponse toUserSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }

    private WalletResponse toWalletResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getUpdatedAt()
        );
    }
}
