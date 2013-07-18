hacienda gcba
=============

qué y por qué
-------------

Un experimento para parsear los datos de hacienda de GCBA, en particular los que respectan a compras y licitaciones.

En un mundo (o país, o sistema de gobierno) ideal, la transparencia sobre el gasto público sería completa, y podríamos (los ciudadanos) saber exactamente cuánto se gastó y en qué.

El Gobierno de la Ciudad de Buenos Aires publica en [su sitio web] [1] un buscador con las licitaciones adjudicadas, pero los datos no son abiertos y la interfaz no permite mucho que digamos.

El propósito de este proyecto es parsear la información, procesarla y publicarla libremente.

cómo
----

El trabajo consistió principalmente en estos pasos 

1. Bajar el listado de licitaciones
2. Parsear datos generales de cada licitación
3. Bajar y parsear detalles de las licitaciones
4. Limpiar la base de datos
5. Exportar la base de datos como un archivo .csv
6. Refinar la información
7. Publicarla (en proceso)

Detallando:

#### Paso 1: Bajar el listado de licitaciones
[El sitio de hacienda] [1] tiene un buscador que muestra de a 100 resultados máximo. 
Por suerte el buscador usa **GET** lo cual expone el número de página que se está accediendo como parámetro web, 
por lo que el primer paso consistió en escribir un pequeño script en [bash] [2] que baje las 460 páginas (con 100 resultados cada una) listando las ~46.000 licitaciones adjudicadas. 

#### Paso 2: Parsear datos generales de cada licitación
El segundo paso consistió en tomar la información bajada y parsearla (procesarla) para extraer la infomación de la licitaciones propiamente dichas. El problema es que lo bajado en el paso 1 esta en formato **html**, lo cual bastante molesto para procesar, pero con la imprescindible ayuda de la librería [jsoup] [3] el asunto fue bastante sencillo.

#### Paso 3: Bajar y parsear detalles de las licitaciones
Un detalle no menor, es que en el listado bajado en el paso 1 había información sobre la licitación (quién la solicitaba, fecha, etc) pero el resto de la información (notablemente qué empresa fue la adjudicada) estaba en *otro html*, por lo que el paso 3 consistió en bajar los ~46.000 archivos de detalle y parsearlos para obtener la información complementaria 

#### Paso 4: Limpiar la base de datos
Este paso fue un mero tecnicismo, ya que el link de donde se encuentra el archivo de la adjudicacion varía según licitación, en algunos casos es pdf, en otros zip, en otros doc, etc., el problema consistió en que algunos links eran absolutos (o sea incluían http://www.buenosaires.gob.ar) y otros no, el paso 3 asumía que eran todos relativos y agregaba de prepo http://www.buenosaires.gob.ar, por lo que hubo que limpiar los que contenían el dominio 2 veces

#### Paso 5: Exportar la base de datos como un archivo .csv
Hasta ahora toda la información era guardada en una [base de datos embebida] [4], para prepararla para el paso 6 hubo que transformar los ~46k registros en un [archivo separado por comas] [5]

#### Paso 6: Refinar la información
El paso más importante (y más difícil) de todos. Tener los registros no alcanza, son muchos, y es muy largo y tedioso intentar hacer cualquier análisis sobre él.
En este paso se usó la excelente herramienta [OpenRefine] [6], que permite, entre muchísimas otras cosas, limpiar información "sucia".

Concretamente, la mayor limpieza fue dada en las empresas. Por ejemplo, algunos registros indicaban que la empresa adjudicada era *Ernesto Van Rossum y Cía S.R.L.*, mientras que otros indicaban *Ernesto Van Rossum y CIA SRL* o *Ernesto Van Rossum y Cía SRL*

A primera vista parece que no hay diferencias entre los 3 casos, pero viendo de cerca en un caso dice **S.R.L.** y en otro **SRL**, y otras diferencias sutiles. Esto es casi imperceptible para una persona, pero una computadora trata los 3 items como si fueran 3 distintos, y si quisieramos saber cuántas adjudicaciones tuvo una empresa, no incluiría en los resultados aquellas que no tienen exactamente el mismo nombre.

Por suerte [OpenRefine] [6] detecta estas diferencias sutiles y permite agruparlos bajo el mismo nombre, de forma que sea fácil la agrupación o filtrado.

Este proceso se realizó tanto para empresas como para solicitantes, licitantes y rubros.

#### Paso 7: Open Data
El paso 7 (aún en proceso) consta(rá) de publicar la información, sea como csv descargable, tanto como proveer un API para aquellos programadores/hackers que quieran usarla y también, sobre todo, una plataforma de visualización que permita ver las licitaciones y, eventualmente, detectar irregularidades.

qué sigue
---------

Más allá del paso 7, hay varias cosas por hacer.

El primero y principal es que las licitaciones no tienen, al momento, costo asociado. Con lo hecho actualmente no hay forma fácil de saber si la licitación fue por pocos pesos o por varios millones. Esta información está guardada en los archivos adjuntos de las licitaciones, que por un lado son muchos y muy pesados, y por el otro están en formatos muy disímiles (pdf, word, rtf, zips) lo cual hace muy difícl el procesamiento automático

Por otro lado, no hay un agrupamiento a nivel rubro. Estaría bueno poder ver cuánto se gasta a nivel *salud*, por ejemplo, pero los rubros indican cosas como *Compra de material estéril* y de nuevo, a priori, no hay forma facil de asociar los rubros que están escritos en las licitaciones entre sí.

contacto
--------

Dudas, consultas, sugerencias o cualquier otro menester, soy ubicable en en twitter: @quixote_arg

[1]: http://www.buenosaires.gob.ar/areas/hacienda/compras/        "Hacienda GCBA"
[2]: http://en.wikipedia.org/wiki/Bash_(Unix_shell)
[3]: http://jsoup.org/
[4]: http://hsqldb.org/
[5]: http://es.wikipedia.org/wiki/CSV
[6]: http://openrefine.org/
