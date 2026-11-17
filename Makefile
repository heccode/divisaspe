.PHONY: start todo test db stop

start:
	./scripts/iniciar.sh

todo:
	./scripts/run-todo.sh

test:
	./mvnw test

db:
	docker compose up -d mysql

stop:
	docker compose down
