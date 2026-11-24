BEGIN;
INSERT INTO clientes
    (nome, email, telefone, cidade, data_cadastro, ativo)
VALUES
    ('Arthur Monteverde', 'arthur@example.com', '11900000001', 'Poá', '2026-08-01 09:00:00', true),
    ('Beatriz Venturini', 'beatriz@example.com', '11900000002', 'Poá', '2026-08-02 10:00:00', true),
    ('Caio Estelar', 'caio@example.com', '11900000003', 'Suzano', '2026-08-03 11:00:00', true),
    ('Diana Floravista', 'diana@example.com', '11900000004', 'Suzano', '2026-08-04 12:00:00', true),
    ('Erick Valeazul', 'erick@example.com', '11900000005', 'Mogi das Cruzes', '2026-08-05 13:00:00', true),
    ('Fernanda Solari', 'fernanda@example.com', '11900000006', 'Mogi das Cruzes', '2026-08-06 14:00:00', true),
    ('Gustavo Lunaris', 'gustavo@example.com', '11900000007', 'Ferraz de Vasconcelos', '2026-08-07 15:00:00', true),
    ('Helena Brisamar', 'helena@example.com', '11900000008', 'Ferraz de Vasconcelos', '2026-08-08 16:00:00', true),
    ('Igor Ventania', 'igor@example.com', '11900000009', 'Poá', '2026-08-09 09:30:00', true),
    ('Julia Camposol', 'julia@example.com', '11900000010', 'Suzano', '2026-08-10 10:30:00', true),
    ('Kaique Monteluz', 'kaique@example.com', '11900000011', 'Itaquaquecetuba', '2026-08-11 11:30:00', true),
    ('Larissa Brisavento', 'larissa@example.com', '11900000012', 'Itaquaquecetuba', '2026-08-12 12:30:00', false);

INSERT INTO servicos
    (codigo, nome, categoria, preco_sugerido, ativo)
VALUES
    ('REV001', 'Revisão Preventiva', 'REVISAO', 80.00, true),
    ('REV002', 'Revisão Completa', 'REVISAO', 180.00, true),
    ('FRE001', 'Regulagem de Freios', 'FREIOS', 65.00, true),
    ('PNE001', 'Troca de Pneu', 'PNEUS', 120.00, true),
    ('TRA001', 'Ajuste de Transmissão', 'TRANSMISSAO', 150.00, true),
    ('TRA002', 'Lubrificação Especial', 'TRANSMISSAO', 45.00, false);

INSERT INTO ordens_servico
    (cliente_id, servico_id, data_entrada, modelo_bicicleta, descricao, valor, status)
SELECT
    c.id,
    s.id,
    d.data_entrada,
    d.modelo,
    d.descricao,
    d.valor,
    d.status
FROM (
    VALUES
    ('arthur@example.com', 'REV001', TIMESTAMP '2026-09-01 09:00:00', 'Caloi Elite', 'Revisão preventiva', 80.00, 'CONCLUIDA'),
    ('beatriz@example.com', 'FRE001', TIMESTAMP '2026-09-03 10:00:00', 'Oggi Hacker', 'Regulagem dos freios', 65.00, 'CONCLUIDA'),
    ('caio@example.com', 'PNE001', TIMESTAMP '2026-09-05 11:00:00', 'Sense Impact', 'Troca do pneu traseiro', 120.00, 'CONCLUIDA'),
    ('diana@example.com', 'TRA001', TIMESTAMP '2026-09-07 12:00:00', 'Trek Marlin', 'Ajuste da transmissão', 150.00, 'CONCLUIDA'),
    ('erick@example.com', 'REV002', TIMESTAMP '2026-09-09 13:00:00', 'Caloi Explorer', 'Revisão completa', 175.00, 'CONCLUIDA'),
    ('fernanda@example.com', 'TRA002', TIMESTAMP '2026-09-11 14:00:00', 'Sense One', 'Lubrificação especial da corrente', 45.00, 'CANCELADA'),
    ('gustavo@example.com', 'PNE001', TIMESTAMP '2026-09-13 15:00:00', 'Oggi Big Wheel', 'Troca do pneu dianteiro', 115.00, 'CONCLUIDA'),
    ('helena@example.com', 'TRA001', TIMESTAMP '2026-09-15 16:00:00', 'Caloi Vulcan', 'Ajuste das marchas', 145.00, 'CONCLUIDA'),
    ('arthur@example.com', 'FRE001', TIMESTAMP '2026-09-17 09:00:00', 'Caloi Elite', 'Regulagem do freio dianteiro', 60.00, 'CONCLUIDA'),
    ('beatriz@example.com', 'PNE001', TIMESTAMP '2026-09-19 10:00:00', 'Oggi Hacker', 'Substituição do pneu', 125.00, 'CANCELADA'),
    ('caio@example.com', 'REV002', TIMESTAMP '2026-09-21 11:00:00', 'Sense Impact', 'Revisão geral da bicicleta', 180.00, 'CONCLUIDA'),
    ('diana@example.com', 'FRE001', TIMESTAMP '2026-09-23 12:00:00', 'Trek Marlin', 'Ajuste dos freios', 75.00, 'CONCLUIDA'),
    ('erick@example.com', 'PNE001', TIMESTAMP '2026-09-25 13:00:00', 'Caloi Explorer', 'Troca do pneu traseiro', 130.00, 'CONCLUIDA'),
    ('fernanda@example.com', 'TRA001', TIMESTAMP '2026-09-27 14:00:00', 'Sense One', 'Regulagem da transmissão', 160.00, 'CONCLUIDA'),
    ('gustavo@example.com', 'REV001', TIMESTAMP '2026-09-29 15:00:00', 'Oggi Big Wheel', 'Revisão preventiva', 85.00, 'CANCELADA'),
    ('helena@example.com', 'REV002', TIMESTAMP '2026-10-01 16:00:00', 'Caloi Vulcan', 'Revisão completa', 190.00, 'CONCLUIDA'),
    ('arthur@example.com', 'TRA001', TIMESTAMP '2026-10-03 09:00:00', 'Caloi Elite', 'Ajuste das marchas', 155.00, 'CONCLUIDA'),
    ('beatriz@example.com', 'REV001', TIMESTAMP '2026-10-05 10:00:00', 'Oggi Hacker', 'Inspeção preventiva', 90.00, 'CONCLUIDA'),
    ('caio@example.com', 'FRE001', TIMESTAMP '2026-10-07 11:00:00', 'Sense Impact', 'Regulagem dos freios', 68.00, 'CANCELADA'),
    ('diana@example.com', 'PNE001', TIMESTAMP '2026-10-10 12:00:00', 'Trek Marlin', 'Substituição do pneu', 135.00, 'CONCLUIDA'),
    ('erick@example.com', 'TRA001', TIMESTAMP '2026-10-13 13:00:00', 'Caloi Explorer', 'Ajuste da transmissão', 170.00, 'CONCLUIDA'),
    ('fernanda@example.com', 'REV002', TIMESTAMP '2026-10-16 14:00:00', 'Sense One', 'Revisão completa da bicicleta', 200.00, 'CONCLUIDA'),
    ('gustavo@example.com', 'FRE001', TIMESTAMP '2026-10-19 15:00:00', 'Oggi Big Wheel', 'Manutenção dos freios', 72.50, 'CONCLUIDA'),
    ('helena@example.com', 'PNE001', TIMESTAMP '2026-10-22 16:00:00', 'Caloi Vulcan', 'Troca do pneu dianteiro', 128.00, 'CANCELADA'),
    ('arthur@example.com', 'REV002', TIMESTAMP '2026-10-25 09:00:00', 'Caloi Elite', 'Revisão geral da bicicleta', 195.00, 'CONCLUIDA'),
    ('beatriz@example.com', 'TRA001', TIMESTAMP '2026-10-28 10:00:00', 'Oggi Hacker', 'Manutenção da transmissão', 165.00, 'CONCLUIDA'),
    ('caio@example.com', 'PNE001', TIMESTAMP '2026-11-01 11:00:00', 'Sense Impact', 'Troca do pneu traseiro', 140.00, 'ABERTA'),
    ('diana@example.com', 'REV001', TIMESTAMP '2026-11-03 12:00:00', 'Trek Marlin', 'Revisão preventiva', 95.00, 'ABERTA'),
    ('erick@example.com', 'FRE001', TIMESTAMP '2026-11-05 13:00:00', 'Caloi Explorer', 'Regulagem dos freios', 78.00, 'ABERTA'),
    ('fernanda@example.com', 'TRA001', TIMESTAMP '2026-11-07 14:00:00', 'Sense One', 'Ajuste completo da transmissão', 185.00, 'ABERTA')

) AS d(email, codigo, data_entrada, modelo, descricao, valor, status)
JOIN clientes c ON c.email = d.email
JOIN servicos s ON s.codigo = d.codigo;

DO $$
BEGIN
    IF (SELECT COUNT(*) FROM clientes) <> 12
       OR (SELECT COUNT(DISTINCT cidade) FROM clientes) < 3
       OR (SELECT COUNT(*) FROM servicos) <> 6
       OR (SELECT COUNT(DISTINCT categoria) FROM servicos) <> 4
       OR NOT EXISTS (SELECT 1 FROM servicos WHERE ativo = false)
       OR (SELECT COUNT(*) FROM ordens_servico) <> 30
       OR (SELECT MAX(data_entrada)::date - MIN(data_entrada)::date FROM ordens_servico) < 60
       OR (SELECT COUNT(DISTINCT status) FROM ordens_servico) <> 3
       OR (SELECT COUNT(DISTINCT s.categoria)
           FROM ordens_servico o
           JOIN servicos s ON s.id = o.servico_id) <> 4
       OR EXISTS (
           SELECT 1
           FROM ordens_servico
           WHERE status = 'ABERTA'
           GROUP BY cliente_id
           HAVING COUNT(*) > 1
       )
       OR EXISTS (
           SELECT 1
           FROM ordens_servico
           WHERE valor <= 0
       )
    THEN
        RAISE EXCEPTION 'Os dados de exemplo não atendem aos requisitos';
    END IF;
END $$;

COMMIT;
