package nl.michelbijnen.ctf.challenges.controllers;

import nl.michelbijnen.ctf.challenges.dtos.ChallengeDto;
import nl.michelbijnen.ctf.challenges.dtos.CheckFlagDto;
import nl.michelbijnen.ctf.challenges.model.Challenge;
import nl.michelbijnen.ctf.challenges.model.User;
import nl.michelbijnen.ctf.challenges.repositories.ChallengeRepository;
import nl.michelbijnen.ctf.challenges.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class ChallengeController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ChallengeRepository challengeRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/check/{challengeId}")
    public ResponseEntity checkFlag(@PathVariable String challengeId, @RequestHeader String requestingUserId, @RequestBody CheckFlagDto checkFlagDto) {
        Optional<Challenge> optionalChallenge = this.challengeRepository.findById(challengeId);
        if (!optionalChallenge.isPresent()) {
            this.logger.warn("'" + requestingUserId + "' tried to check flag with not existing challenge id '" + challengeId + "'");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge id does not exist");
        }

        Challenge challenge = optionalChallenge.get();
        if (!challenge.getFlag().equals(checkFlagDto.getFlag())) {
            this.logger.warn("'" + requestingUserId + "' has entered the wrong flag for challenge id '" + challengeId + "'");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong flag");
        }

        Optional<User> optionalUser = this.userRepository.findById(requestingUserId);
        if (!optionalUser.isPresent()) {
            this.logger.warn("'" + requestingUserId + "' tried to check flag with not existing user id");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id does not exist");
        }

        User user = optionalUser.get();

        List<Challenge> solvedChallenges = user.getTeam().getSolvedChallenges();
        for (Challenge solvedChallenge : solvedChallenges) {
            if (solvedChallenge.getId().equals(challenge.getId())) {
                this.logger.warn("'" + requestingUserId + "' tried to resubmit already solved challenge '" + challenge.getId() + "'");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge already solved");
            }
        }
        solvedChallenges.add(challenge);

        this.userRepository.save(user);

        this.logger.info("'" + requestingUserId + "' solved challenge '" + challenge.getId() + "'");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/")
    public ResponseEntity<List<ChallengeDto>> getAllChallenges(@RequestHeader String requestingUserId) {
        Optional<User> optionalUser = this.userRepository.findById(requestingUserId);
        if (!optionalUser.isPresent()) {
            this.logger.warn("'" + requestingUserId + "' tried to get all challenges with not existing user id");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id does not exist");
        }

        User user = optionalUser.get();

        List<Challenge> challenges = this.challengeRepository.findAll();
        List<ChallengeDto> challengeDtos = new ArrayList<>();

        for (Challenge challenge : challenges) {
            ChallengeDto challengeDto = new ChallengeDto();
            challengeDto.setId(challenge.getId());
            challengeDto.setTitle(challenge.getTitle());
            challengeDto.setDescription(challenge.getDescription());
            challengeDto.setFileURL(challenge.getFileURL());
            challengeDto.setPoints(challenge.getPoints());

            // Get if challenge is solved
            challengeDto.setSolved(false);
            List<Challenge> solvedChallenges = user.getTeam().getSolvedChallenges();
            for (Challenge solvedChallenge : solvedChallenges) {
                if (solvedChallenge.getId().equals(challenge.getId())) {
                    challengeDto.setSolved(true);
                    break;
                }
            }

            challengeDtos.add(challengeDto);
        }

        return ResponseEntity.ok(challengeDtos);
    }
}