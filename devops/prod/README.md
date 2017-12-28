To refresh `cybermarkets.yaml:

    kompose convert -o cybermarkets.yaml

To deploy to cluster:

    kubectl apply -f cybermarkets.yaml

To clean up resources (all data will be lost):

    kubectl delete -f cybermarkets.yaml

Access dashboard - http://127.0.0.1:8001/ui

    kubectl proxy

Find external IP:

    kubectl get svc

Inspect state of all cluster resources:

    kubectl get all

Read logs:

    kubectl logs <podname>

---

Untested. Update to new version. Update image version tag and run:

    kubectl apply -f cybermarkets.yaml
