package nl.michelbijnen.ctf.challenges.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Challenge {
    @Id
    private String id;
    private String title;
    private String description;
    private String category;
    private String fileURL;
    private String flag;
    private int points;
}
