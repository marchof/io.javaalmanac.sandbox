#/bin/bash

export AWS_ACCESS_KEY_ID=
export AWS_SECRET_ACCESS_KEY=
export AWS_REGION=
export AWS_REGISTRY=

cd "$(dirname "$0")"

docker image prune -f

/usr/local/bin/aws ecr get-login-password --region ${AWS_REGION} | docker login --password-stdin --username AWS ${AWS_REGISTRY}

docker-compose pull

docker-compose up -d

docker logout ${AWS_REGISTRY}