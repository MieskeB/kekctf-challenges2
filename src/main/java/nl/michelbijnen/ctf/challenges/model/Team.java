package nl.michelbijnen.ctf.challenges.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Team {
    @Id
    private String id;
    private String name;
    @ManyToMany
    private List<Challenge> solvedChallenges;
}
