FROM node:latest

# Create app directory
COPY ./connectors/etherdelta/src/main/node/fund/cyber/markets/connectors/etherdelta/ /cm
WORKDIR /cm
# Install app dependencies
RUN npm install
COPY . /cm

CMD node etherdelta.js