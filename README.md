# Grupo de Teoría del Lenguaje - SCALA-2

La idea es implementar un sistema de recomendación on demand por twitter. Vamos a tener alguna instancia de un servidor ... #TODO

---

### Links útiles

[Librería oficial de twitter para Scala - twitter4s](https://github.com/DanielaSfregola/twitter4s)

[API rest con los endpoints para obtener las peliculas](https://www.themoviedb.org/documentation/api)


---

### Instalación compilador scala en entornos *nix

```sudo apt update && sudo apt install scala```

Para compilar: `scalac <archivo>`

Para ejecutar: `scala <nombre_clase>`

### Instalar sbt (el proyecto va a tener dependencias)

```
wget https://bintray.com/artifact/download/sbt/debian/sbt-0.13.9.deb
sudo dpkg -i sbt-0.13.9.deb
sudo apt-get update
sudo apt-get install sbt
```

### Correr el esqueleto

```
Correr ./sbt (la primera vez va a tardar porque descarga todas las dependencias)
Una vez en la consola de sbt (se ve un ">") escribir reStart
>reStart
```

### Hacer una request

***TWITTER***
- Mencionar a @MovieRecommen1 en un tuit con al menos un hashtag para que haga la busqueda, como resultado se verá una respuesta de 3 películas que han matcheado.

```
EJEMPLO: "@MovieRecommen1 Recomendame pelis de #zombies"
```

```
root Response for key=zombies with code=200
root 🎬 'Zombies! Zombies! Zombies!' | Score: 3
root 🎬 'Zombies paletos' | Score: 4
root 🎬 'Orgullo + Prejuicio + Zombis' | Score: 5
```

### TROUBLESHOOTING

```
Permission denied

Dar permisos de ejecucion a ./sbt

chmod u+x ./sbt
chmod u+x ./sbt-dist/bin/sbt
```
