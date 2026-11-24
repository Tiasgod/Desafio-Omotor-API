package oficina.rodalivre.entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import oficina.rodalivre.entity.enums.StatusOrdem;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder 
@AllArgsConstructor 
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ordens_servico", indexes = {
        @Index(name = "idx_ordens_cliente", columnList = "cliente_id"),
        @Index(name = "idx_ordens_servico", columnList = "servico_id"),
        @Index(name = "idx_ordens_status", columnList = "status"),
        @Index(name = "idx_ordens_data_entrada", columnList = "data_entrada")
        }
)
public class OrdemServicoEntity {     

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Setter(AccessLevel.NONE)

        private Long id;
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "cliente_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ordens_cliente"))

        private ClienteServicoEntity cliente;
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "servico_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ordens_servico"))

        private CatalogoServicoEntity servico;
        @Column(name = "data_entrada", nullable = false)

        private LocalDateTime dataEntrada;
        @Column(name = "modelo_bicicleta", nullable = false, length = 80)

        private String modeloBicicleta;
        @Column(name = "descricao", nullable = false, length = 500)
        
        private String descricao;
        @Column(name = "valor", nullable = false, precision = 12, scale = 2)

        private BigDecimal valor;
        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)

        private StatusOrdem status;
        @PrePersist
        public void antesDeSalvar() {
                if (status == null) {
                status = StatusOrdem.ABERTA;
                }
        }
}
