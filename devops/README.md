# Deployment of application

Build local image (until known problem #1 fixed):

```bash
# pwd = /
docker build -t cm/java-base:1.0.0 . -f ./devops/java-base/Dockerfile
```

Also rebuild this image each time, when you change dependencies or gradle version.

To run cluster:

```bash
# pwd = /devops
export CYBER_MARKETS_APPLICATION_DATA=/path/to/data/folder
docker-compose up -d

# or

CYBER_MARKETS_APPLICATION_DATA=/path/to/data/folder docker-compose up -d
```

Since we building images in compose file, likely you want to rebuild them often, so you have to use following commands:

```bash
# pwd = /devops
CYBER_MARKETS_APPLICATION_DATA=/path/to/data/folder docker-compose build 

#or

CYBER_MARKETS_APPLICATION_DATA=/path/to/data/folder docker-compose up -d --build
```

For development consider adding `CYBER_MARKETS_APPLICATION_DATA` to `~/.bashrc`:

```bash
export CYBER_MARKETS_APPLICATION_DATA=/home/user/path/to/data/folder
```

## Known Issues

### 1. Long build time of java containers. (moby/moby #14080)

Since docker [doesn't support](https://github.com/moby/moby/issues/14080#issuecomment-318557424) build time volumes yet, we have to build own base image with docker and our dependencies, and use it. Without this image we will download entire gradle and dependencies each time. When this issue will be fixed we can provide volume to build stages to cache gradle and their dependencies.
