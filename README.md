## Запуск базы данных

Из контейнера
```bash
docker-compose up -d
```

Для других способов необходимо настроить `hibernate.properties`, создать базу `userdb`  и запустить скрипт из `init-scripts/01-init-tables.sql`.
