package oficina.rodalivre.entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor 
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "clientes", uniqueConstraints = {
    @UniqueConstraint(name = "uk_clientes_email", columnNames = "email")
    }
)
public class ClienteServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;
    @Column(name = "nome", nullable = false, length = 120)

    private String nome;
    @Column(name = "email", nullable = false, length = 254)

    private String email;
    @Column(name = "telefone", nullable = false, length = 11)

    private String telefone;
    @Column(name = "cidade", nullable = false, length = 80)

    private String cidade;
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)

    private LocalDateTime dataCadastro;
    @Column(name = "ativo", nullable = false)

    private Boolean ativo;
    @PrePersist
    public void antesDeSalvar() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
        if (ativo == null) {
            ativo = true;
        }
    }
}
