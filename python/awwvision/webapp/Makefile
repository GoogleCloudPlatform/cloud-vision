GCLOUD_PROJECT:=$(shell gcloud config list project --format="value(core.project)")

.PHONY: all
all: deploy

.PHONY: build
build:
	docker build -t gcr.io/$(GCLOUD_PROJECT)/awwvision-webapp .

.PHONY: push
push: build
	gcloud docker -- push gcr.io/$(GCLOUD_PROJECT)/awwvision-webapp

.PHONY: template
template:
	sed "s/\$$GCLOUD_PROJECT/$(GCLOUD_PROJECT)/g" spec.tmpl.yaml > spec.yaml

.PHONY: deploy
deploy: push template
	kubectl create -f spec.yaml

.PHONY: delete
delete: template
	kubectl delete --ignore-not-found -f spec.yaml
