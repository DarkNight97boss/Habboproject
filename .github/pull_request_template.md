## Cosa cambia

<!-- Descrizione sintetica della modifica e del perché. -->

## Area

- [ ] EMU (game server Java)
- [ ] CMS-V3 web (React)
- [ ] CMS-V3 api (Hono)
- [ ] Nitro V3 (client)
- [ ] SQL / migrations
- [ ] CI / deploy / infra

## Checklist

- [ ] `yarn typecheck` e `yarn build` passano in locale (CMS-V3)
- [ ] Nessun secret/credenziale committato (gitleaks verde)
- [ ] Se tocca `*.sql` / migrations: testato su `ms_dev`
- [ ] Se tocca config DB dell'EMU: usato il pattern **STOP → UPDATE DB → START**
- [ ] Documentazione aggiornata se necessario

## Note

<!-- Aperta da un autore in allowlist, questa PR fa partire un deploy automatico
     su https://dev.asteriacore.online (reset ms_dev). -->
