-- V3: Seed initial supermarket chains operating in Spain
INSERT INTO supermarkets (id, name, country)
VALUES ('00000000-0000-0000-0000-000000000001', 'Mercadona', 'ES'),
       ('00000000-0000-0000-0000-000000000002', 'Carrefour', 'ES'),
       ('00000000-0000-0000-0000-000000000003', 'Alcampo', 'ES'),
       ('00000000-0000-0000-0000-000000000004', 'ALDI', 'ES'),
       ('00000000-0000-0000-0000-000000000005', 'LIDL', 'ES'),
       ('00000000-0000-0000-0000-000000000006', 'DIA', 'ES')
ON CONFLICT (name) DO NOTHING;

