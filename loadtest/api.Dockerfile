# API CMS-V3 per il load test (usa e getta). Build TS -> node dist.
FROM node:24-bookworm-slim AS build
WORKDIR /app
COPY CMS-V3/package.json CMS-V3/yarn.lock ./
COPY CMS-V3/apps/api/package.json apps/api/package.json
RUN corepack enable && yarn install --frozen-lockfile || yarn install
COPY CMS-V3/ ./
RUN yarn workspace @cms-v3/api build || (cd apps/api && yarn build)

FROM node:24-bookworm-slim
WORKDIR /app/apps/api
COPY --from=build /app /app
EXPOSE 8092
CMD ["node", "dist/index.js"]
