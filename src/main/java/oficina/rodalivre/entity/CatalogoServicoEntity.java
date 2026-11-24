package oficina.rodalivre.entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import oficina.rodalivre.entity.enums.CategoriaServico;
import java.math.BigDecimal;

@Builder
@AllArgsConstructor 
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "servicos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_servicos_codigo", columnNames = "codigo")
        }
)
public class CatalogoServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)

    private Long id;
    @Column(name = "codigo", nullable = false, length = 20)

    private String codigo;
    @Column(name = "nome", nullable = false, length = 120)

    private String nome;
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 20)

    private CategoriaServico categoria;
    @Column( name = "preco_sugerido", nullable = false, precision = 12, scale = 2)

    private BigDecimal precoSugerido;
    @Column(name = "ativo", nullable = false)
    
    private Boolean ativo;
    @PrePersist
    public void antesDeSalvar() {
        if (ativo == null) {
            ativo = true;
        }
    }
}
