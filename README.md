
# MB Music Hub Server

This repository is the Spring Boot sever, powered by gradle, which performs the backend actions for the MB Music Hub Website. This functions as an API that handles all interaction with the Spotify API. It also provides any other data fetching necessary for the functionality of the Vue JS front end.
## Installation

First ensure that you have at least Java 21 installed on your system.

I assume this project may be run on any IDE, but it was originally built using Visual Studio Code.

Since this project was built using Gradle, you should be able to clone the project and then run the gradle wrapper to build and run the project. Before you do that though, ensure that you have the environment variables set up:


## Environment Variables

To run this project, you will need to add the following environment variables to your .env file

### Running

`SpotifyClientId` - The Client ID specified for the app in my Spotify API account

`SpotifyClientSecret` - The secret associated with the app

### Testing
The following variables are used within the unit tests. They are mainly used for access to the Spotify API.

`SpotifyAccessToken` - An access token that you can generate yourself for the tests

`SpotifyRefreshToken` - The refresh token associated with the access token to allow for continuous use

`SpotifyTokenGeneratedAt` - The timestamp for when the Spotify Token was generated, represented as an ISO datetime string



## Building and Running

You can build the project using `./gradlew build`, but when you do that, it runs the unit tests beforehand.

If you want to avoid this, run `./gradlew build -x test`.

You can run this project using the gradle wrapper, but for debugging, I recommend using your IDE's run and test options. For VS code this involves setting up a launch.json file for running the app and settings.json for testing. You can specifiy your environment variables there.

**DO NOT UPLOAD LAUNCH CONFIGURATIONS  OR ENVIRONMENT VARIABLES TO SOURCE CONTROL**
