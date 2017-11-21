FROM node:latest

# Create app directory
WORKDIR /app
# Install app dependencies
COPY package.json /app
RUN npm install
COPY . /app

CMD node etherdelta.js