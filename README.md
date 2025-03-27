mvn clean install

mvn spring-boot:run

http://localhost:8000/api/users

docker-compose up --build

docker-compose up -d

http://localhost:9100/health
http://localhost:9100/test


http://localhost:15672
Usuario: guest
Contraseña: guest

curl -X POST http://localhost:9100/api/users \
-H "Content-Type: application/json" \
-d '{"name":"Juan Perez","email":"jfarfan@tecsup.edu.pe","phone":"123456789"}'

curl -X POST http://localhost:9100/api/users \
-H "Content-Type: application/json" \
-d '{"name":"Juan Perez","email":"jfarfan@example.com","phone":"123456789"}'


## Obtener todos los usuarios
curl -X GET http://localhost:9100/api/users

## Obtener un usuario específico
curl -X GET http://localhost:9100/api/users/1


## pruebas
curl -X GET http://localhost:9100/test
curl -X POST http://localhost:9100/api/users -H "Content-Type: application/json" -d '{"name":"Juan Perez","email":"juan@example.com","phone":"123456789"}'
curl -X POST http://localhost:9100/api/users -H "Content-Type: application/json" -d '{"name":"Ana Torres","email":"ana@example.com","phone":"123456789"}'
curl -X GET http://localhost:9100/api/users

