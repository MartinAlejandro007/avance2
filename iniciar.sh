#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"
echo 'Iniciando CampusUDLATracker. Cuando aparezca Started IntercampusApplication, abre http://localhost:8080'
echo 'Para detener la aplicación: Ctrl+C. Los datos se conservan en data/.'
exec ./mvnw -Daether.syncContext.named.factory=rwlock-local spring-boot:run
