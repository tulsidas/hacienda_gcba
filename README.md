hacienda gcba
=============

qué y por qué
-------------

Un experimento para parsear los datos de hacienda de GCBA, en particular los que respectan a compras y licitaciones.

En un mundo (o país, o sistema de gobierno) ideal, la transparencia sobre el gasto público sería completa, y podríamos (los ciudadanos) saber exactamente cuánto se gastó y en qué.

El Gobierno de la Ciudad de Buenos Aires publica en [su sitio web] [1] un buscador con las licitaciones adjudicadas, pero los datos no son abiertos y la interfaz no permite mucho que digamos.

El propósito de este proyecto es parsear la información, procesarla y publicarla libremente.

[1]: http://www.buenosaires.gob.ar/areas/hacienda/compras/        "Hacienda GCBA"

cómo
----

El trabajo consistió principalmente en estos pasos 

1. Bajar el listado de licitaciones
2. Parsear datos generales de cada licitación
3. Bajar detalles de las licitaciones y parsearlos
4. Limpiar la base de datos
5. Exportar la base de datos como un archivo .csv
6. Refinar la información
7. Publicarla (en proceso)

Detallando:

#### Paso 1: Bajar el listado de licitaciones


qué sigue
---------
