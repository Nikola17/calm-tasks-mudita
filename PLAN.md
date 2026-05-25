# Plan projet - Application Mudita Kompakt

## Hypothese de depart

Nous partons sur une application Android native pour Mudita Kompakt, concue pour l'ecran E Ink, sans dependance Google, avec une experience calme et intentionnelle.

Nom de travail : **Calm Tasks**.

Identifiant Android : `com.yugesa.calmtasks`.

Compatibilite cible : MuditaOS K 1.5.0, base Android 12 AOSP, `minSdk 31`.

Positionnement : une application de capture et de choix de prochaines actions, plus proche d'un outil de clarte mentale que d'un gestionnaire de taches productiviste.

## Pourquoi cette app

Le forum Mudita montre deja beaucoup d'efforts sur les launchers, les notifications, les apps audio, les QR codes, les calendriers et les apps sideload classiques. Le besoin interessant n'est donc pas de refaire une app existante, mais de combler un manque plus transversal : aider l'utilisateur a savoir quoi faire ensuite, sans transformer le telephone en machine a sollicitations.

L'app doit repondre a trois problemes :

1. Capturer rapidement une idee ou une tache.
2. Reduire la charge mentale en limitant ce qui est visible.
3. Rappeler seulement ce qui merite vraiment une interruption.

## Objectif produit

Creer une application locale, simple et lisible qui permet de :

- noter une tache en quelques secondes ;
- choisir 1 a 3 priorites pour aujourd'hui par defaut ;
- ranger les taches dans quelques dossiers ;
- definir un rappel calme si necessaire ;
- consulter l'ensemble sans scroll infini, animations ou surcharge visuelle.

## Ce que l'app n'est pas

- Pas un clone de Todoist, Notion ou TickTick.
- Pas de collaboration.
- Pas de compte utilisateur.
- Pas de synchronisation cloud dans le MVP.
- Pas de gamification, streaks, badges ou scores de productivite.
- Pas de notifications agressives.
- Pas de gestion de projet complexe.

## Principes Mudita / Bright Patterns

L'application doit encourager l'intention plutot que l'engagement.

Principes a appliquer :

- **Friction utile** : si l'utilisateur ajoute trop de taches a aujourd'hui, l'app propose de choisir les plus importantes.
- **Limitation volontaire** : l'ecran Today affiche 3 priorites par defaut, avec une limite reglable.
- **Clarte** : chaque ecran a une action principale evidente.
- **Respect de l'attention** : pas de compteurs anxiogenes, pas de relance inutile.
- **Local-first** : les donnees restent sur l'appareil.
- **E Ink first** : interface noir/blanc, peu de rafraichissements, pas d'animations dependantes du mouvement.

## Parcours utilisateur MVP

### 1. Capture

L'utilisateur ouvre l'app et peut ajouter une tache depuis l'ecran principal.

Champs minimum :

- titre ;
- dossier optionnel ;
- date optionnelle ;
- rappel optionnel.

Verification : une tache peut etre creee en moins de 3 interactions apres l'ouverture de l'app.

### 2. Today

L'ecran principal affiche :

- les 1 a 3 priorites du jour ;
- un bouton d'ajout ;
- un acces aux dossiers ;
- un acces aux taches sans date.

Verification : si plus de taches que la limite choisie sont marquees pour aujourd'hui, l'app affiche un choix explicite au lieu d'allonger la liste principale.

### 3. Folders

Dossiers par defaut :

- Home ;
- Work ;
- Admin ;
- Errands ;
- Personal.

Verification : une tache peut etre filtree par dossier et deplacee d'un dossier a l'autre.

### 4. Reminders

Les rappels doivent etre sobres :

- un seul rappel par tache dans le MVP ;
- texte court ;
- action claire : Done, Later, Open.

Verification : le rappel fonctionne sans Google Services et reste lisible sur E Ink.

## Ecrans prevus

1. **Today**
   - Vue principale.
   - 3 taches maximum.
   - Actions : add, complete, later, open folders.

2. **Add Task**
   - Saisie simple.
   - Dossier.
   - Date/rappel optionnels.

3. **Folders**
   - Liste des dossiers.
   - Compteur sobre par dossier.

4. **Task Detail**
   - Modifier le titre.
   - Changer le dossier.
   - Choisir today/later.
   - Marquer comme fait.

5. **Review**
   - Ecran optionnel si le MVP avance bien.
   - Permet de reclasser les taches sans date.

6. **Settings**
   - Permet de choisir la limite de priorites Today.
   - Defaut : 3.

## Direction design

Contraintes visuelles :

- format portrait 480 x 800 ;
- noir, blanc, gris leger uniquement ;
- typographie large et lisible ;
- pas de gradient ;
- pas d'ombre decorative ;
- bordures simples ;
- icones au trait ;
- boutons rectangulaires avec coins sobres ;
- listes courtes et paginees ;
- zones tactiles confortables.

Le style doit etre proche des captures Mudita : interfaces aerées, titres nets, pictogrammes simples, beaucoup de blanc, tres peu d'informations concurrentes.

## Architecture technique

Stack proposee :

- Kotlin ;
- Android natif ;
- Jetpack Compose ;
- Mudita Mindful Design si compatible avec le setup retenu ;
- Mudita Mindful Design `com.mudita:MMD-android:1.0.0` ;
- stockage local Room ;
- notifications Android locales ;
- aucune dependance Google Play Services.

Choix initial :

- Room si nous gerons plusieurs champs, filtres et etats.
- DataStore seulement si le modele reste tres petit.

## Modele de donnees MVP

Entite `Task` :

- `id` ;
- `title` ;
- `folder` ;
- `status` : active, done ;
- `plannedDate` optionnel ;
- `reminderAt` optionnel ;
- `createdAt` ;
- `updatedAt`.

Entite `Folder` :

- `id` ;
- `name` ;
- `sortOrder`.

Entite `Settings` :

- `todayPriorityLimit`, defaut 3, bornes 1 a 5.

## Criteres de reussite

Le MVP est acceptable si :

- l'app s'installe sur Android sans Google Services ;
- l'interface reste lisible en 480 x 800 ;
- l'application cible Android 12 AOSP / MuditaOS K 1.5.0 ;
- les taches survivent a la fermeture de l'app ;
- le flux Add -> Today -> Done fonctionne ;
- la limite Today est configurable dans Settings ;
- aucun ecran ne depend de couleur pour etre compris ;
- aucune animation n'est necessaire pour comprendre l'interface ;
- le README explique clairement l'intention mindful, les Bright Patterns et les limites du MVP.

## Verification

Avant soumission :

1. Lancer les tests unitaires du modele et du stockage.
2. Verifier la compilation Android.
3. Tester l'APK sur emulateur avec resolution proche du Kompakt.
4. Verifier les ecrans en contraste noir/blanc.
5. Relire le README selon les criteres du challenge.

## Risques

- La bibliotheque MMD peut demander un setup specifique ou etre limitee.
- Les notifications peuvent varier selon MuditaOS K.
- Le clavier et la saisie texte peuvent etre le point faible de l'experience.
- Le temps avant la deadline impose un MVP tres strict.

Mitigation :

- garder peu d'ecrans ;
- privilegier l'interface texte simple ;
- eviter toute dependance externe non indispensable ;
- documenter clairement ce qui est MVP et ce qui est hors scope.

## Roadmap courte

### Phase 1 - Cadrage

- Valider le nom final.
- Valider les 4 ecrans du MVP.
- Creer les wireframes basse fidelite.
- Confirmer l'integration MMD.

### Phase 2 - Prototype

- Initialiser le projet Android.
- Creer la navigation.
- Implementer Today, Add Task et Task Detail.
- Ajouter le stockage local.

### Phase 3 - Mudita polish

- Adapter les espacements et tailles a 480 x 800.
- Supprimer les animations inutiles.
- Verifier le rendu noir/blanc.
- Ajouter les icones sobres.

### Phase 4 - Soumission

- Finaliser README.
- Ajouter captures d'ecran.
- Publier le repo public si decision de soumettre.
- Remplir le formulaire Mudita.

## Decision proposee

Nous devrions partir sur **Calm Tasks** comme MVP. C'est l'idee la plus equilibree entre utilite reelle, originalite, faisabilite rapide et alignement avec Mudita Mindful Design.

La prochaine etape logique est de produire les wireframes des 4 ecrans principaux avant de coder.
