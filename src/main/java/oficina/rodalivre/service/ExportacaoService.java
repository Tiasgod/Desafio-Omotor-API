package oficina.rodalivre.service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

@Service
public class ExportacaoService {

    private final JdbcTemplate jdbcTemplate;

    public ExportacaoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SQL = """
            SELECT
                'ORDEM' AS tipo_registro,
                o.id AS ordem_id,
                c.id AS cliente_id,
                c.nome AS cliente_nome,
                c.cidade AS cliente_cidade,
                c.ativo AS cliente_ativo,
                s.id AS servico_id,
                s.codigo AS servico_codigo,
                s.nome AS servico_nome,
                s.categoria AS categoria,
                s.ativo AS servico_ativo,
                o.data_entrada,
                o.status,
                o.valor,
                o.modelo_bicicleta,
                o.descricao
            FROM ordens_servico o
            JOIN clientes c ON c.id = o.cliente_id
            JOIN servicos s ON s.id = o.servico_id

            UNION ALL

            SELECT
                'CLIENTE',
                NULL,
                c.id,
                c.nome,
                c.cidade,
                c.ativo,
                NULL,
                NULL,
                NULL,
                NULL,
                NULL,
                NULL,
                NULL,
                NULL,
                NULL,
                NULL
            FROM clientes c
            WHERE NOT EXISTS (
                SELECT 1
                FROM ordens_servico o
                WHERE o.cliente_id = c.id
            )

            UNION ALL

            SELECT
                'SERVICO',
                NULL,
                NULL,
                NULL,
                NULL,
                NULL,
                s.id,
                s.codigo,
                s.nome,
                s.categoria,
                s.ativo,
                NULL,
                NULL,
                NULL,
                NULL,
                NULL
            FROM servicos s
            WHERE NOT EXISTS (
                SELECT 1
                FROM ordens_servico o
                WHERE o.servico_id = s.id
            )

            ORDER BY tipo_registro, ordem_id, cliente_id, servico_id
            """;

    @Transactional(readOnly = true)
    public byte[] exportarCsv() {
        return jdbcTemplate.query(SQL, (ResultSetExtractor<byte[]>) rs -> {
            StringBuilder csv = new StringBuilder();

            csv.append("tipo_registro,ordem_id,cliente_id,cliente_nome,")
                    .append("cliente_cidade,cliente_ativo,servico_id,servico_codigo,")
                    .append("servico_nome,categoria,servico_ativo,data_entrada,")
                    .append("status,valor,modelo_bicicleta,descricao\r\n");

            while (rs.next()) {
                escreverLinha(csv, rs);
            }

            return csv.toString().getBytes(StandardCharsets.UTF_8);
        });
    }

    private void escreverLinha(StringBuilder csv, ResultSet rs) throws SQLException {
        Timestamp dataEntrada = rs.getTimestamp("data_entrada");

        String data = dataEntrada == null
                ? ""
                : dataEntrada.toLocalDateTime()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        adicionarLinha(csv,
                rs.getString("tipo_registro"),
                rs.getString("ordem_id"),
                rs.getString("cliente_id"),
                rs.getString("cliente_nome"),
                rs.getString("cliente_cidade"),
                rs.getString("cliente_ativo"),
                rs.getString("servico_id"),
                rs.getString("servico_codigo"),
                rs.getString("servico_nome"),
                rs.getString("categoria"),
                rs.getString("servico_ativo"),
                data,
                rs.getString("status"),
                rs.getString("valor"),
                rs.getString("modelo_bicicleta"),
                rs.getString("descricao")
        );
    }

    private void adicionarLinha(StringBuilder csv, String... valores) {
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                csv.append(',');
            }

            String valor = valores[i] == null ? "" : valores[i];

            if (valor.contains(",") || valor.contains("\"")
                    || valor.contains("\n") || valor.contains("\r")) {
                csv.append('"')
                        .append(valor.replace("\"", "\"\""))
                        .append('"');
            } else {
                csv.append(valor);
            }
        }

        csv.append("\r\n");
    }
}
