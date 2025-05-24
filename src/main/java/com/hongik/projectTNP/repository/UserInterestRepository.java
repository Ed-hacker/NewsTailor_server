package com.hongik.projectTNP.repository;

import com.hongik.projectTNP.domain.Interest;
import com.hongik.projectTNP.domain.User;
import com.hongik.projectTNP.domain.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findByUser(User user);
    List<UserInterest> findByInterest(Interest interest);
    Optional<UserInterest> findByUserAndInterest(User user, Interest interest);
    void deleteByUserAndInterest(User user, Interest interest);
} 