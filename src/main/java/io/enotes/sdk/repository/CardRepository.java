package io.enotes.sdk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.enotes.sdk.entity.Card;

public interface CardRepository extends JpaRepository<Card, Long>{
}
