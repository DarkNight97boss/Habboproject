# API CMS-V3 per il load test (usa e getta). Build TS -> node dist.
FROM node:24-bookworm-slim AS build
WORKDIR /app
COPY CMS-V3/ ./
# SOLO per il load test: l'API in prod binda 127.0.0.1 (dietro nginx); nel
# container serve 0.0.0.0 per il port-forward. Patch build-time, prod intatto.
RUN sed -i "s/hostname: '127.0.0.1'/hostname: '0.0.0.0'/" apps/api/src/index.ts
RUN corepack enable && (yarn install --frozen-lockfile || yarn install)
RUN yarn workspace @cms-v3/api build || (cd apps/api && yarn build)

FROM node:24-bookworm-slim
WORKDIR /app/apps/api
COPY --from=build /app /app
EXPOSE 8092
CMD ["node", "dist/index.js"]
