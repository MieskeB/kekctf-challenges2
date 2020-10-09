package nl.michelbijnen.ctf.challenges.controllers;

import nl.michelbijnen.ctf.challenges.dtos.ChallengeDto;
import nl.michelbijnen.ctf.challenges.dtos.CheckFlagDto;
import nl.michelbijnen.ctf.challenges.dtos.CreateChallengeDto;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @GetMapping("/{challengeId}")
    public ResponseEntity<ChallengeDto> getChallenge(@RequestHeader String requestingUserId, @PathVariable String challengeId) {
        Optional<User> optionalUser = this.userRepository.findById(requestingUserId);
        if (!optionalUser.isPresent()) {
            this.logger.warn("'" + requestingUserId + "' tried to get a challenge with not existing user id");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id does not exist");
        }

        User user = optionalUser.get();

        Optional<Challenge> optionalChallenge = this.challengeRepository.findById(challengeId);
        if (!optionalChallenge.isPresent()) {
            this.logger.warn("'" + requestingUserId + "' tried to get a challenge with not existing challenge id");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge id does not exist");
        }

        Challenge challenge = optionalChallenge.get();

        ChallengeDto challengeDto = new ChallengeDto();
        challengeDto.setId(challenge.getId());
        challengeDto.setTitle(challenge.getTitle());
        challengeDto.setDescription(challenge.getDescription());
        challengeDto.setCategory(challenge.getCategory());
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

        return ResponseEntity.ok(challengeDto);
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
            challengeDto.setCategory(challenge.getCategory());
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

    @PostMapping("/")
    public ResponseEntity createChallenge(@RequestBody CreateChallengeDto createChallengeDto) throws URISyntaxException {
        if (!createChallengeDto.getFlag().toLowerCase().startsWith("kekctf{") || !createChallengeDto.getFlag().toLowerCase().endsWith("}")) {
            createChallengeDto.setFlag("kekctf{" + createChallengeDto.getFlag() + "}");
        } else {
            String flag = createChallengeDto.getFlag();
            flag = flag.substring(7, flag.length() - 1);
            flag = "kekctf{" + flag + "}";
            createChallengeDto.setFlag(flag);
        }

        if (createChallengeDto.getPoints() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Points can't be equal or lower then 0");
        }

        if (createChallengeDto.getTitle().trim().equals("") || createChallengeDto.getDescription().trim().equals("")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some fields are not filled in");
        }

        Challenge challenge = new Challenge(
                UUID.randomUUID().toString(),
                createChallengeDto.getTitle(),
                createChallengeDto.getDescription(),
                createChallengeDto.getCategory(),
                createChallengeDto.getFileURL(),
                createChallengeDto.getFlag(),
                createChallengeDto.getPoints()
        );

        this.challengeRepository.save(challenge);
        return ResponseEntity.created(new URI("/" + challenge.getId())).build();
    }
}
