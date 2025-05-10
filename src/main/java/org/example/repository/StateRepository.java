package org.example.repository;

import org.example.model.State;
import org.example.projections.StateColumnView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StateRepository extends JpaRepository<State, Long> {
    Optional<StateColumnView> findFirst1ByUserIdOrderByIdDesc(long userId);
}
