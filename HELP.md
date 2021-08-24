# Getting Started

### Reference Documentation
This api allows to calculate the maximum value that can be used from a coupon given a list of favorite items.

* How was the algorithm for calculating the maximum profit implemented?

    For the resolution of the calculation of the maximum benefit value given a set of product prices, 
    the approach to the backpack problem was taken, where the value of the weight and the value of the 
    benefit were equal to the value of the product and the capacity of the backpack was I take the value of the coupon. 
    For more information on the approach to the backpack problem visit. https://es.wikipedia.org/wiki/Problema_de_la_mochila

* What architecture approach was used?

    A hexagonal architecture approach was used for the api implementation.
     Where there is a single input adapter for the reception of rest requests. 
     There are two output adapters (repositories) for caching product items (CaffeineItemRepository and RedisItemRepository)
     and an output adapter for communicating with Mercado Libre's item api.


### Guides
The following guides illustrate how to use some features concretely:

* ###How to deploy the api in a local environment?
    
    To deploy the coupon api in a local environment, follow these steps:

 1 -  Enter the "deployment" directory located at the root of the project
        
     
```sh
    cd deployment
``` 

2 - Execute following command
    
 ```sh
     sh deploy-local.sh
``` 
3 - Send requests via http as followsSend requests via http as follows
 ```sh
       curl -i --header "Content-Type: application/json" \
         --user meli:mel1 \
         --request POST \
         --data '{ "amount": 50000, "item_ids": ["MCO609835351", "MCO600674389", "MCO600787211"] }' \
         http://localhost:9191/api/coupon
``` 

NOTE: See more information about Api contracts.
    1. Open https://editor.swagger.io/
    2. Paste content file. https://github.com/ingjosefabian/coupons_api/blob/master/src/main/resources/api.yml

* ###How to enable a particular cache output adapter?

    To enable an output adapter for cache use the property from the application.yml file.
    * Enable Redis: api.clients.cache.redis.enable = true
    * Enable local cache: api.clients.cache.redis.enable = false

### Additional Links
These additional references should also help you:

* [Hexagonal architecture](https://fideloper.com/hexagonal-architecture)
* [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
* [Reactive rest service](https://spring.io/guides/gs/reactive-rest-service/)


