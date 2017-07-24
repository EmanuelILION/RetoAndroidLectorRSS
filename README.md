# RetoAndroidLectorRSS
Reto Android para validar

-Picasso (compile 'com.squareup.picasso:picasso:2.5.2'): Esta libreria la usamos para cargar las imagenes, ya que la aplicación se usará tambien de manera offline,
por lo que Picasso se encargará de obtener la iamgen mediante la url y almacenarla en la caché para luego cargarda desde ella, es decir Picasso siempre
verificará si existe la imagen en la caché del dispositivo, en caso afirmativo la cargará desde allí en caso contrario solicitará conexión a 
internet para conseguir la iamgen.

-Meterial Desing rey5137 (compile 'com.github.rey5137:material:1.2.4'): Esta librería solo la usamos para el Spinner de selección.
