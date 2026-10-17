#!/bin/sh
set -eu

validate_secret() {
    value=${1}
    name=${2}
    case "${value}" in
        *[!A-Za-z0-9._-]*|'')
            echo "ERROR: ${name} solo puede contener letras, números, punto, guion y guion bajo." >&2
            exit 1
            ;;
    esac
}

validate_secret "${AUTH_DB_PASSWORD}" "AUTH_DB_PASSWORD"
validate_secret "${CLIENTES_DB_PASSWORD}" "CLIENTES_DB_PASSWORD"
validate_secret "${VETERINARIOS_DB_PASSWORD}" "VETERINARIOS_DB_PASSWORD"
validate_secret "${MASCOTAS_DB_PASSWORD}" "MASCOTAS_DB_PASSWORD"
validate_secret "${CITAS_DB_PASSWORD}" "CITAS_DB_PASSWORD"
validate_secret "${CONSULTAS_DB_PASSWORD}" "CONSULTAS_DB_PASSWORD"
validate_secret "${VACUNAS_DB_PASSWORD}" "VACUNAS_DB_PASSWORD"

mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" <<EOSQL
CREATE DATABASE IF NOT EXISTS auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS clientes_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS veterinarios_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mascotas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS citas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS consultas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS vacunas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'dawivet_auth'@'%' IDENTIFIED BY '${AUTH_DB_PASSWORD}';
ALTER USER 'dawivet_auth'@'%' IDENTIFIED BY '${AUTH_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON auth_db.* TO 'dawivet_auth'@'%';

CREATE USER IF NOT EXISTS 'dawivet_clientes'@'%' IDENTIFIED BY '${CLIENTES_DB_PASSWORD}';
ALTER USER 'dawivet_clientes'@'%' IDENTIFIED BY '${CLIENTES_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON clientes_db.* TO 'dawivet_clientes'@'%';

CREATE USER IF NOT EXISTS 'dawivet_veterinarios'@'%' IDENTIFIED BY '${VETERINARIOS_DB_PASSWORD}';
ALTER USER 'dawivet_veterinarios'@'%' IDENTIFIED BY '${VETERINARIOS_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON veterinarios_db.* TO 'dawivet_veterinarios'@'%';

CREATE USER IF NOT EXISTS 'dawivet_mascotas'@'%' IDENTIFIED BY '${MASCOTAS_DB_PASSWORD}';
ALTER USER 'dawivet_mascotas'@'%' IDENTIFIED BY '${MASCOTAS_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON mascotas_db.* TO 'dawivet_mascotas'@'%';

CREATE USER IF NOT EXISTS 'dawivet_citas'@'%' IDENTIFIED BY '${CITAS_DB_PASSWORD}';
ALTER USER 'dawivet_citas'@'%' IDENTIFIED BY '${CITAS_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON citas_db.* TO 'dawivet_citas'@'%';

CREATE USER IF NOT EXISTS 'dawivet_consultas'@'%' IDENTIFIED BY '${CONSULTAS_DB_PASSWORD}';
ALTER USER 'dawivet_consultas'@'%' IDENTIFIED BY '${CONSULTAS_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON consultas_db.* TO 'dawivet_consultas'@'%';

CREATE USER IF NOT EXISTS 'dawivet_vacunas'@'%' IDENTIFIED BY '${VACUNAS_DB_PASSWORD}';
ALTER USER 'dawivet_vacunas'@'%' IDENTIFIED BY '${VACUNAS_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON vacunas_db.* TO 'dawivet_vacunas'@'%';

FLUSH PRIVILEGES;
EOSQL
