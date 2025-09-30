package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.reaction.Reaction;
import ru.practicum.ewm.model.reaction.ReactionType;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.reaction = :reaction AND r.eventId = :eventId")
    Long countReactionsByEvent(@Param("eventId") Long eventId, @Param("reaction") ReactionType reactionType);

    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.reaction = :reaction AND r.eventUserId = :eventUserId")
    Long countReactionsByInitiator(@Param("eventUserId") Long eventUserId, @Param("reaction") ReactionType reactionType);

    @Query("SELECT COUNT(r) > 0 FROM Reaction r WHERE r.eventId = :eventId AND r.likedUserId = :likedUserId AND r.reaction = :reaction")
    Boolean existsReaction(@Param("eventId") Long eventId, @Param("likedUserId") Long likedUserId, @Param("reaction") ReactionType reactionType);

    void deleteByEventIdAndLikedUserId(Long eventId, Long likedUserId);
}
