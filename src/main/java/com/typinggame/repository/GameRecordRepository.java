package com.typinggame.repository;

import com.typinggame.domain.GameRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB repository for GameRecord entity.
 * Provides CRUD operations and custom queries for game history.
 */
@Repository
public interface GameRecordRepository extends MongoRepository<GameRecord, String> {

    /**
     * Find all game records for a user, ordered by timestamp descending.
     */
    List<GameRecord> findByUserIdOrderByTimestampDesc(String userId);

    /**
     * Find top 10 recent games for a user.
     */
    List<GameRecord> findTop10ByUserIdOrderByTimestampDesc(String userId);

    /**
     * Find top 20 recent games for a user.
     */
    List<GameRecord> findTop20ByUserIdOrderByTimestampDesc(String userId);
}
