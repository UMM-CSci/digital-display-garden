## Before you Start
Before starting this project you will need a couple of tools to run this project
  * [IntelliJ](https://www.jetbrains.com/idea/) or other multi language IDE
  * The latest version of [MongoDB](https://www.mongodb.com/download-center#community)
  * A grasp of [TypeScript](https://www.typescriptlang.org/docs/tutorial.html),
[Angular-2](https://angular.io/docs/ts/latest/ ) and [Java's Apache Spark Platform](https://spark.apache.org/docs/latest/api/java/)


## Running the Project in Development Mode

:exclamation::exclamation::fire:Note: If IntelliJ ever prompts you to compile typescript files into javascript say no!. Doing this will confuse webpack and break the client side of your project during build. 
No permanent damage will be done, but it's pretty annoying to deal with.

    Go to View/Tool Windows/Gradle
#### To run the server:

* Go to server/Tasks/application
    
    click ***run***
    Make sure that `config.properties` exists within the `/server/` directory. For more information on this file see [ServerConfiguration.md](./ServerConfiguration.md) and the example files within `/server/`. 
#### To run the Client:
* Go to client/Tasks/application

    click ***runClient***
    
You can now visit the running dev project at
    [http://localhost:9000/](http://localhost:9000/) :boom: 
    
    
## Simulating Production Mode
This part is pretty simple. I made a bash script that will clean, build, gradle and start the server. I also normally use a cool tool called *tmux* to start a process, be able to walk away from deploy-user, come back, re-attach to the process, and see how the server is doing. Here are some easy steps to get the project up and running:
1. `cd` into the project directory
2. `tmux` to create a *tmux* session  
3. `chmod +x buildAndLaunchProject.sh` *buildAndLaunchProject.sh* is a bash script I made to clean, build, and start the server. chmod +x makes this file executible.
4. `./buildAndLaunchProject.sh` actually starts the script. Keep watching it until you see that the server has stared. Can easily take 5 minutes.
5. `ctrl b` and then `d` to disconnect from the *tmux* session.

And now your server is running!!! You can visit the live production website at [http://localhost:2538/](http://localhost:2538/) :boom:  
     