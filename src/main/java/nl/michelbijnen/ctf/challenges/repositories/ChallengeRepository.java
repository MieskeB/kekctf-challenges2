package nl.michelbijnen.ctf.challenges.repositories;

import nl.michelbijnen.ctf.challenges.model.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, String> {
}
