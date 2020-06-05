# Progetto Sistemi Distributi Pervasivi

Progetto di Sistemi Distribuiti E Pervasivi AA 19/20

Sebastiano Caccaro

## Running the project

To execute the project you must compile it first by executing the following command in the pom directory

```bash
mvn install
```

You will then need to start up the gateway with the following command:

```bash
placeholder command
```

Then, you can spawn nodes with the following command

```bash
mvn exec:java -Dexec.mainClass="node.NodeStarter" -Dexec.args="[node_port][node_id][node_ip][gateway_port][gateway_url]"
```

If no node_ip, gateway_port and gateway_url are provided, the default value are `localhost` and `localhost:1337`. Note that the gateway url must be provided without in a barebone fashion, for instance `localhost` and `127.0.0.1` are ok, while `http://xx.xxx.xxx.....` and `localhost/foo/` are forbidden (the latter becaouse of the backslash at the end).
