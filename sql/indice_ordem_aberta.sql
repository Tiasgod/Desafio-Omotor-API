-- Execute uma vez no SQL Editor do Neon depois da criacao das tabelas.
-- Nao aplica se ja existem duas ordens ABERTA para o mesmo cliente.
CREATE UNIQUE INDEX IF NOT EXISTS uk_ordem_aberta_por_cliente
ON ordens_servico (cliente_id)
WHERE status = 'ABERTA';
