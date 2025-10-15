# Lưu ý trước khi code

1. Khi chạy eureka server thì làm các bước sau:

- Vào edit configuration
- Bật VM Options
- Thêm dòng sau:

```bash
# port: 9001, 9002, 9003, áp dụng cho từng instance
-Dspring.profiles.active=instance-(số thứ tự)
```

2. Cổng của APIGateway là 8080, service khác quy định cổng random bằng cách set cho server.port=0. Khi đó nếu muốn chạy nhiều instance của 1 service, chỉ cần copy configuration là được

3. Tiền tố của api là v1/api/
    Ví dụ: account service: localhost:8081/v1/api/accounts/**
           notification service: localhost:8082/v1/api/notifications/**

4. Các services sau sẽ được triển khai:

- Paper service (paper, annotation, tag, citation)
- Account service (user, role)
- Notification
- Reading workflow service (reading list, reading workflow)
- Group service (collection, team)
