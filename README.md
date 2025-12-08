
1. **Cấu hình database**

Linux
```
sudo docker compose -f postgreSQL.yaml up -d --build
```
Windows

```
docker compose -f postgreSQL.yaml up -d --build
```
**Reset database**
```
docker volume rm webproject_postgres_data
sudo docker compose -f postgreSQL.yaml up -d --build
```
**Thao tác với DB**
```
docker exec -it postgres bash
psql -U cps -d cps_db
```

2. **Build toàn bộ project**



```bash
mvn clean install -DskipTests
```

## Chạy dự án

```bash
mvn spring-boot:run
```

Ứng dụng sẽ chạy mặc định tại : http://localhost:8080

## Swagger UI

Giao diện tài liệu API:

```
http://localhost:8080/swagger-ui/index.html
```
