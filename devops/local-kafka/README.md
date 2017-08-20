# Run local kafka

To run local kafka:

```bash
cd devops/local-kafka
docker-compose up -d
```

To build new kafka image
```bash
cd devops/local-kafka
docker build -t cybernode/dev-kafka ./
```

Than, all local kafka data will be stored in *devops/out/* folder.