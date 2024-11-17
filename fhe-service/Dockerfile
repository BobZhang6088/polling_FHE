# Use Node.js LTS as base image
FROM node:lts

# Set working directory
WORKDIR /usr/src/app

# Copy package.json and install dependencies
COPY package*.json ./
RUN npm install

# Copy the application code
COPY . .

# Expose the port
EXPOSE 3000

# Command to run the service
CMD ["node", "index.js"]