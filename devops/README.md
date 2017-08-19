# Run local kafka

To run local kafka:

```bash
cd devops
docker-compose up -d
```

Than, all local kafka data will be stored in *devops/out/* folder.

# Image deployment

Image deployment process is handled by Circle CI:
https://github.com/cyberFund/cyber-markets/blob/development/.circleci/config.yml

# e2e image testing

End-to-end image builds and testing are maintained in:
https://github.com/cyberFund/cybernode/tree/master/images
