# Proyecto Introducción a Sistemas Distribuidos
## Integrantes
- *César Andrés Olarte Marín*
- *Paola Benitez Ruiz*
- *Laura Isabel Montero Blanco*
## Descripción
Las instancias a ejecutar son las presentadas a continuación. En el orden en el que se presentan se deben ejecutar para que el sistema funcione correctamente.
### Quality System
[QualitySystem](src/main/java/com/forest/server/QualitySystem.java) es el encargado de recibir por cada capa las alertas y publicarlas en consola. Para ejecutar este programa se debe correr el siguiente comando en cada capa:
_java QualitySystem_

### Cloud
[Cloud](src/main/java/com/forest/server/cloud/CloudServer.java) es el encargado de recibir las alertas, mediciones y promedios de la capa de proxy y enviarlas a la base de datos. Para ejecutar este programa se debe correr el siguiente comando:
_java CloudServer_

### Proxy
[Proxy](src/main/java/com/forest/server/proxy/ProxyServer.java) es el encargado de recibir las alertas y mediciones de los sensores y enviarlas a la capa de cloud despues de haber filtrado datos erroneos; igualmente genera promedios por enviar al cloud. Para ejecutar este programa se debe correr el siguiente comando:
_java ProxyServer_

### HealthChecker
[HealthChecker](src/main/java/com/forest/server/HealthChecker.java) es el encargado de enviar solicitudes de hearbeat con un patronREQ/REP al servidor proxy instaurado. Por lo mismo debe correrse despues de que el proxy se haya iniciado. Se ejecuta de la siguiente manera:
_java HealthChecker_

### Sensores
[SensorServer](src/main/java/com/forest/server/sensors/SensorServer.java) se encarga de crear a los diferentes sensores que van a envviar los datos al proxy. Para ejecutar este programa hace falta especificar alguno de los siguientes tipos de sensores:
- *Temperature*
- *Humidity*
- *Fog*
Junto a estos hace falta especificar un archivo similar a [este](src/main/resources/dataArranged.txt) que contenga los porcentajes de probabilidad para generar los datos de los senores.
_java SensorServer \[Temperature|Humidity|Fog\] \[Archivo probabilidades\]_

## Opcional
Dado que los programas se pueden correr para obtener sus metricas, en caso de requerirlo se puede ejecutar en vez de los servidores sus homologos:
- [MetricsCloud](src/main/java/com/forest/server/cloud/MetricsCloud.java)
- [MetricsProxy](src/main/java/com/forest/server/proxy/MetricsProxy.java)
- [MetricsSensor](src/main/java/com/forest/server/sensors/MetricsSensors.java)
Los cuales con el objetivo de que puedan ser escuchados por Prometheus, instauran los registros de las metricas, y en su puerto respectivo crean un servidor HTTP con la dependencia com.sun.net.httpserver para hacer posible esa recoleccion de datos.