#!/bin/bash
# Чтобы Gradle/Jib находили docker (демон Gradle мог стартовать из IDE с другим PATH)
export PATH="/usr/local/bin:/opt/homebrew/bin:/Applications/Docker.app/Contents/Resources/bin:$PATH"
source ./docker.properties
export PROFILE="${PROFILE:=docker}"

# Останавливаем демон Gradle — при следующем запуске он подхватит PATH из этого скрипта (и найдёт docker)
./gradlew --stop 2>/dev/null || true

# docker compose (пробел) — актуальная команда в Docker Desktop вместо docker-compose
DOCKER_COMPOSE="docker compose"

echo '### Java version ###'
java --version

front=""
front_image=""
if [[ "$1" = "gql" ]]; then
  front="./niffler-frontend-gql/";
  front_image="${IMAGE_PREFIX}/${FRONT_IMAGE_NAME_GQL}-${PROFILE}:latest";
else
  front="./niffler-frontend/";
  front_image="${IMAGE_PREFIX}/${FRONT_IMAGE_NAME}-${PROFILE}:latest";
fi

FRONT_IMAGE="$front_image" PREFIX="${IMAGE_PREFIX}" PROFILE="${PROFILE}" $DOCKER_COMPOSE down

docker_containers="$(docker ps -a -q)"
docker_images="$(docker images --format '{{.Repository}}:{{.Tag}}' | grep 'niffler')"

if [ ! -z "$docker_containers" ]; then
  echo "### Stop containers: $docker_containers ###"
  docker stop $(docker ps -a -q)
  docker rm $(docker ps -a -q)
fi
if [ ! -z "$docker_images" ]; then
  echo "### Remove images: $docker_images ###"
  docker rmi $(docker images --format '{{.Repository}}:{{.Tag}}' | grep 'niffler')
fi

if [ "$1" = "push" ] || [ "$2" = "push" ]; then
  echo "### Build & push images (front: $front) ###"
  bash ./gradlew -Pskipjaxb jib -x :niffler-e-2-e-tests:test
  cd "$front" || exit
  bash ./docker-build.sh ${PROFILE} push
else
  echo "### Build images (front: $front) ###"
  bash ./gradlew -Pskipjaxb jibDockerBuild -x :niffler-e-2-e-tests:test
  cd "$front" || exit
  bash ./docker-build.sh ${PROFILE}
fi

cd ../
docker images
FRONT_IMAGE="$front_image" PREFIX="${IMAGE_PREFIX}" PROFILE="${PROFILE}" $DOCKER_COMPOSE up -d
docker ps -a
