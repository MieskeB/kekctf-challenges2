package nl.michelbijnen.ctf.challenges.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {

    @Id
    private String id;
    private String username;
    @OneToOne
    private Team team;
    private String hash;
    private String salt;
    private String secretKey;
    @ElementCollection
    private List<String> role;
}
