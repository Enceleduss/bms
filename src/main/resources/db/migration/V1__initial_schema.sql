SET search_path = ag_catalog, "$user", public;
CREATE EXTENSION IF NOT EXISTS age CASCADE;
GRANT USAGE ON SCHEMA ag_catalog TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA ag_catalog TO postgres;
SELECT ag_catalog.create_graph('main_graph');
SELECT ag_catalog.create_graph('test_graph');