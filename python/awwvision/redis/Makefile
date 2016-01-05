.PHONY: all
all: deploy

.PHONY: deploy
deploy:
	kubectl create -f spec.yaml

.PHONY: delete
delete:
	kubectl delete --ignore-not-found -f spec.yaml
