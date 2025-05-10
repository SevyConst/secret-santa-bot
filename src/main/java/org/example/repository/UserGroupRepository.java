package org.example.repository;

import org.example.model.UserGroup;
import org.example.projections.GroupView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    List<GroupView> findGroupsByUserId(Long userId);
}
