# egguard-backend

---------
In order to run for development, just make sure you have your .env created based on the .env.example. Then inject the .env in IntelliJ IDEA.
Now to start the database do:

docker-compose up --build    

And, finally, just run the backend with the "Run button" of the IDE. 

-------------
In order to run for production, just do: 

docker-compose --profile production up --build

