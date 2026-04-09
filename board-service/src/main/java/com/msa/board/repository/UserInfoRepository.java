package com.msa.board.repository;

import com.msa.board.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

    Optional<UserInfo> findByUserId(Long userId);

    List<UserInfo> findAllByUserIdIn(List<Long> userIds);
}
