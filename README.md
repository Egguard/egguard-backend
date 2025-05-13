# egguard-backend

---------
In order to run for development, just make sure you have your .env created based on the .env.example. Then inject the .env in IntelliJ IDEA.
Now to start the database do:

docker-compose up --build    

And, finally, just run the backend with the "Run button" of the IDE. 

-------------
In order to run for production, just do: 

docker-compose --profile production up --build



---------------
In order to run in Linux, first run:

To install docker, only run once
./install_docker_linux.sh

Then run this to run whole backend:
sudo docker compose --profile production up --build

--------------
In order to use the backend with the robot:
-Install in Linux, same PC as ROS
-Start backend in other PC in same LAN