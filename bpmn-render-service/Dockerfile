FROM node:20-slim

ENV PUPPETEER_SKIP_DOWNLOAD=true

# Puppeteer
RUN apt update \
    && apt install -y \
      ca-certificates \
      chromium \
      fonts-liberation \
      libasound2 \
      libdrm2 \
      libgbm1 \
      libglib2.0-0 \
      libnss3 \
      libx11-xcb1 \
      libxcomposite1 \
      libxcursor1 \
      libxdamage1 \
      libxi6 \
      libxrandr2 \
      libxss1 \
      libxtst6 \
      wget \
      xdg-utils \
    && apt clean

WORKDIR /app

COPY package*.json ./
RUN npm install --omit=dev

COPY . .

EXPOSE 3004
CMD ["node", "index.js"]
