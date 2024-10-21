# Application Android de Gestion des Étudiants

## Description

Ce projet consiste à développer une application mobile Android capable d'interagir avec une base de données MySQL via des services web en PHP. L'application permet d'ajouter, de modifier, de supprimer et d'afficher des informations sur des étudiants en utilisant la bibliothèque Volley pour gérer les requêtes réseau. Elle inclut également des fonctionnalités de filtrage et de partage des informations des étudiants.

## Fonctionnalités

1. **Récupération et Affichage des Étudiants**
   - Utilisation de Volley pour récupérer la liste des étudiants depuis un serveur PHP.
   - Affichage des étudiants dans une `RecyclerView`.

2. **Ajout d'Étudiants**
   - Envoi des données d'un nouvel étudiant via une requête POST avec Volley.
   - Insertion de l'étudiant dans la base de données MySQL.

3. **Modification d'Étudiants**
   - Mise à jour des informations d'un étudiant via un popup de modification.
   - Envoi des modifications via une requête POST ou PUT.

4. **Suppression d'Étudiants**
   - Suppression d'un étudiant en swipant l'élément correspondant dans la liste.
   - Requête DELETE envoyée avec Volley.

5. **Filtrage des Étudiants**
   - Envoi des critères de recherche à un endpoint PHP pour filtrer les résultats.
   - Mise à jour de la liste en conséquence.

6. **Partage d'Informations**
   - Partage des détails d'un étudiant via des applications tierces grâce à un Intent.

## Technologies Utilisées

- **Android** : Application mobile native.
- **PHP** : Backend pour les services web.
- **MySQL** : Base de données pour stocker les informations des étudiants.
- **Volley** : Bibliothèque utilisée pour gérer les requêtes HTTP.
- **RecyclerView** : Composant d'Android utilisé pour afficher les listes de manière optimisée.

  ## Auteur

- **SAKHR Niama** - Étudiante à l'Université Chouaib Doukkali, Ecole Nationale des Sciences Appliquées d'El Jadida.

## Encadré par

- **Pr. M. LACHGAR**

## Démo Vidéo

Voici une démonstration vidéo de l'application Android en action :



