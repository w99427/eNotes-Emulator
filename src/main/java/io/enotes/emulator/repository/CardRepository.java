package io.enotes.emulator.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.enotes.emulator.entity.Card;

public interface CardRepository extends JpaRepository<Card, Long>{
}
