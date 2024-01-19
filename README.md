`*`*La version en français suit la version en anglais*`*`  
`*`*French version follows*`*`

# Natural language processing program

## Description

Implementation of two types of Map to create a natural language processing program that finds the most relevant document for a given user query (TFIDF).The program supports spelling errors (Levenshtein distance) and suggests the next most likely word that appears after a word given by the user.

## How to Install

* Clone or download the project from this GitHub repository.
* **Java Version :** OpenJDK version 20
* **External libraries:** StanfordNLP ~ version 4.5.1

## How run

* **If you have downloaded the complete project:**
	 1. To run the project, make sure you have an integrated development environment (IDE) installed.
	2. Open the IDE, import the project, then locate the class (Main) and the main method (main). 
	3. Then run the program by clicking on the corresponding button in the IDE.
	
* **If you've downloaded the .jar file:**
  * Open your computer's console, then write the following command: "java -jar NLP.jar".
  * Make sure you're in the same folder as your .jar file (use the "cd" command to navigate through your folders).

The project already includes a few examples of possible entries and query:

* `Data` and `query.txt`.

*-* If you want to create your own entries, make sure you respect the format of the entries already provided. Make sure your text files are located in the `Data` folder.
*-* To modify the query, make sure you respect the format of the query already supplied and modify the `query.txt` file directly.

## Credits

This project is a practical work for the course IFT2015 (Data Structures). It was made in collaboration with [Étienne Mitchell-Bouchard](https://github.com/DarkZant).

<br><br>
___

<br>

# Programme de traitement du langage naturel

## Description

Implémentation de deux types de Map afin de créer un programme de traitement du langage naturel qui permet de trouver le document le plus pertinent pour une recherche de l‘utilisateur (TFIDF). Le programme supporte les fautes d‘orthographe (distance de Levenshtein) et suggère le mot suivant le plus probable qui apparaît après un mot donné par l‘utilisateur.

## Comment installer

* Cloner ou télécharger le projet depuis ce dépôt GitHub.
* Vous pouvez aussi simplement télécharger le fichier "NLP.jar".
* **Version Java :** OpenJDK version 20
* **Librairies externes :** StanfordNLP ~ version 4.5.1

## Comment exécuter

* **Si vous avez téléchargé le projet au complet :**
	 1. Pour exécuter le projet, assurez-vous d'avoir un environnement de développement intégré (IDE) installé.
	2. Ouvrez l'IDE, importez le projet, puis localisez la classe (Main) et la méthode principale (main). 
	3. Ensuite, lancez l'exécution du programme en cliquant sur le bouton correspondant dans l'IDE.
	
* **Si vous avez téléchargé le fichier .jar :**
  * Ouvrez la console de votre ordinateur, ensuite écrivez la commande suivante : "java -jar NLP.jar".
  *Assurez-vous de bien être dans le dossier dans lequel vous avez placé votre fichier .jar (pour naviguer dans votre console utilisez la commande "cd").*

Le projet inclus déjà quelques exemples d'entrées et requête possibles :

* `Data` et `query.txt`

*-* Si vous voulez créer vos propres entrées assurez vous de respecter le format des entrées déjà fournies. Assurez vous que vos fichiers de texte se trouvent dans le dossier `Data`.
*-* Pour modifier la requête, assurez vous de respecter le format des entrées déjà fournies et veuillez modifier directement le fichier `query.txt`.


## Crédits

Ce projet ce projet est un travail pratique du cours IFT2015 (Structures de données). Il a été effectué en groupe avec [Étienne Mitchell-Bouchard](https://github.com/DarkZant).
