# Discord Crypto Alert Bot

## Introduction
### Brief Description

Crypto Alert Bot is an application written in Java which serves as a cryptocurrency alert service for Discord servers. Furthermore, it can hold a watchlist for each user on a particular server where the bot is deployed. 

The application operates with Binance API containing around 1000 cryptocurrency pairs (symbols and their prices) updated with a little to none delay compared to the prices binance.com provides to regular users. Since Binance on its own lists way more than 1000 cryptocurrency pairs, it is recommended to use rather conventional cryptocurrency pairs or to check the availability by taking a look at https://api.binance.com/api/v3/ticker/price and CTRL+F the desired cryptocurrency symbol with its price.

### Motivation
The main motivation behind the application creation was to extend alert percentage use https://cryptocurrencyalerting.com/coin/BOT
which otherwise serves as a swiss knife (messages on multiple platforms, multiple cryptocurrency exchanges etc.) in terms of cryptocurrency alerts
and to try out an own Discord bot creation which could serve a small subset of Cryptocurrency Alerting website functionality. 

## Technology Used
- Java
    - Java 16 or 17 was tested for a compilation and  run

- gradle
    - To build the library it is demanded to have at version 7+ (the version used was 7.1)
    - For Windows/Linux users - available inside of IntelliJ Idea or by [manual installation](https://gradle.org/install/)

- External dependencies
    - Google Gson - JSON parsing library to process the Binance API response
    - JDA - Java Discord API - Discord support for Java - token authentication, receive and send messages

## Setup and Launch
- IntelliJ Idea contains gradle support by default, therefore you are requested to build and run the project
  - During the build phase, the necessary external packages are installed if they are not found
  - Afterward, the run should be available - in case, you are using a different Java Runtime Environment, edit your configuration accordingly
- For the command line users, in the root directory, there is an install script according to your operating system (Windows - gradlew.bat, Linux - gradlew) which takes care of the run via ```gradlew.bat``` run resp. ```./gradlew run```

## Hardware Requirements
- For an end user, there are no special hardware requirements, however, it is demanded to be an user of either of a 64-bit operating system (due to external dependencies) - either Windows or Linux operating system (the gradlew script was tested on Ubuntu WSL 20.04)
- In case you want to be a host of the bot (no cloud service whatsoever), it is highly recommended to have a stable connection since the
bot does not operate with any persistent storage and upon the disconnection user watchlists and alerts are wiped
- The application was compiled and ran using a device with the CPU: AMD Ryzen 7 4800H, RAM 16 GB,
Windows 10 Home (64-bit) using IntelliJ Idea 2021.2 (Ultimate) on Windows (and also purely using the command line), Linux build and run was made purely using its command line on Ubuntu WSL 20.04