
[![Logo MyFood](https://github.com/suufiaane13/MyFood/raw/main/images/logo.webp)](https://github.com/suufiaane13/MyFood)

# MyFoodOrdering

Application Android de **commande de plats** : menu, panier, validation, suivi des commandes, profil, et un **espace restaurateur** (admin) pour gérer le menu, les catégories et le statut des commandes.

Backend : **Supabase** (authentification, base de données, stockage images). Notifications optionnelles via **Firebase**.

Ce guide est pensé pour un **développeur débutant sous Windows** qui découvre le projet pour la première fois.

---

## Sommaire

1. [Ce dont tu as besoin](#1-ce-dont-tu-as-besoin)
2. [Démarrage rapide (recommandé pour tester)](#2-démarrage-rapide-recommandé-pour-tester)
3. [Démarrage complet (ton propre Supabase)](#3-démarrage-complet-ton-propre-supabase)
4. [Lancer l’app sur Windows](#4-lancer-lapp-sur-windows)
5. [Premier compte et rôle admin](#5-premier-compte-et-rôle-admin)
6. [Structure du projet](#6-structure-du-projet)
7. [Problèmes fréquents sous Windows](#7-problèmes-fréquents-sous-windows)
8. [Stack technique & limites](#8-stack-technique--limites)

---

## 1. Ce dont tu as besoin

| Outil | Pourquoi | Où l’obtenir |
|--------|----------|--------------|
| **Windows 10 ou 11** | Environnement cible de ce guide | — |
| **Android Studio** (version récente, ex. Ladybug / Koala ou plus) | Compiler et lancer l’app, émulateur Android | [developer.android.com/studio](https://developer.android.com/studio) |
| **Git** (recommandé) | Cloner le dépôt | [git-scm.com/download/win](https://git-scm.com/download/win) |
| **Compte Supabase** | Uniquement si tu crées **ton** backend (étape 3) | [supabase.com](https://supabase.com) |

**À savoir :**

- Le projet utilise **JDK 17** : Android Studio l’installe en général tout seul au premier lancement.
- Un **téléphone Android** (USB + mode développeur) **ou** un **émulateur** créé dans Android Studio suffit pour tester.
- Le fichier `app/google-services.json` est **déjà présent** dans le dépôt : tu peux compiler sans créer Firebase tout de suite (les notifications push avancées restent optionnelles).

---

## 2. Démarrage rapide (recommandé pour tester)

Utilise cette voie si tu veux **voir l’app tourner le plus vite possible**, sans configurer Supabase toi-même.

### Étape A — Récupérer le code

**Avec Git (invite de commandes ou PowerShell) :**

```powershell
cd $env:USERPROFILE\Documents
git clone <URL_DU_DEPOT> MyFood
cd MyFood
```

Remplace `<URL_DU_DEPOT>` par l’URL Git du projet (GitHub, GitLab, etc.).

**Sans Git :** télécharge le projet en ZIP, décompresse-le (ex. `C:\Users\TonNom\Documents\MyFood`).

### Étape B — Ouvrir dans Android Studio

1. Lance **Android Studio**.
2. **File → Open** (ou « Open an Existing Project »).
3. Sélectionne le dossier **`MyFood`** (celui qui contient `gradlew.bat` et le dossier `app`).
4. Si Android Studio propose d’installer des composants (SDK, etc.), accepte et attends la fin.
5. Attends la fin de **Gradle Sync** (barre de progression en bas). La première fois peut prendre **plusieurs minutes** (téléchargement des dépendances).

### Étape C — Clés Supabase déjà dans le projet

Le fichier `app/src/main/java/com/si/myfoodordering/data/SupabaseConfig.kt` contient déjà une **URL** et une **clé anon** pointant vers un projet Supabase de démo.  
Tu n’as **rien à modifier** pour un premier test, tant que ce backend reste accessible.

> Si la connexion ou le menu ne charge pas plus tard, passe à la [section 3](#3-démarrage-complet-ton-propre-supabase) pour utiliser ton propre projet Supabase.

### Étape D — Lancer l’app

Suis la [section 4](#4-lancer-lapp-sur-windows), puis crée un compte ([section 5](#5-premier-compte-et-rôle-admin)).

---

## 3. Démarrage complet (ton propre Supabase)

À faire si tu veux **ton** backend, ou si le backend de démo ne répond plus.

### 3.1 Créer le projet Supabase

1. Va sur [supabase.com](https://supabase.com) → **New project**.
2. Note le **mot de passe** de la base et attends que le projet soit prêt (quelques minutes).

### 3.2 Exécuter le script SQL

1. Dans Supabase : **SQL Editor** → **New query**.
2. Ouvre le fichier **`supabase_setup.sql`** à la racine du dépôt (Bloc-notes ou Android Studio).
3. Copie **tout** le contenu, colle-le dans l’éditeur SQL, puis **Run**.
4. Exécute ce script **une seule fois** par projet (sinon risque de doublons sur les données de démo).

Ce script unique crée : tables, sécurité (RLS), bucket images `plats`, menu de démo, table `push_tokens`, etc.

### 3.3 Auth email (important pour l’inscription)

Dans Supabase : **Authentication → Providers → Email** :

- Active **Email**.
- Pour les tests en local, tu peux **désactiver** « Confirm email » (sinon il faut valider chaque inscription par mail).

### 3.4 Brancher l’app sur ton projet

1. Dans Supabase : **Project Settings → API**.
2. Copie :
   - **Project URL** (ex. `https://xxxxx.supabase.co`)
   - **anon public** key (pas la clé `service_role` !)
3. Dans le projet Android, édite :

   `app/src/main/java/com/si/myfoodordering/data/SupabaseConfig.kt`

   Remplace `SUPABASE_URL` et `SUPABASE_KEY` par tes valeurs.

4. **Sync** Gradle dans Android Studio : icône éléphant ou **File → Sync Project with Gradle Files**.

### 3.5 Images des plats (optionnel)

Le menu s’affiche même sans images. Pour les photos :

- **Storage** → bucket **`plats`** → dossier **`covers`**
- Tu peux t’inspirer des fichiers dans le dossier `assets/` du dépôt (noms indiqués en bas de `supabase_setup.sql`).

### 3.6 Firebase (optionnel)

Déjà fourni : `app/google-services.json`.  
Pour **tes** notifications FCM : crée un projet [Firebase Console](https://console.firebase.google.com), ajoute une app Android avec le package `com.si.myfoodordering`, télécharge un nouveau `google-services.json` et remplace celui dans `app/`.

---

## 4. Lancer l’app sur Windows

### Option 1 — Android Studio (la plus simple)

1. En haut de l’écran : choisis une **cible** :
   - **Device Manager** (icône téléphone) → **Create Device** → par ex. **Pixel 6**, image système **API 34** (télécharge si demandé) → **Finish** → démarre l’émulateur (▶).
   - Ou branche un **téléphone** : active **Options pour les développeurs** + **Débogage USB**, accepte l’autorisation sur le téléphone.
2. Dans la liste des appareils, sélectionne ton émulateur ou ton téléphone.
3. Clique sur le bouton vert **Run ▶** (module **`app`**).
4. Au premier lancement : écran **Splash**, puis **Connexion** ou **Accueil** si tu es déjà connecté.

### Option 2 — Ligne de commande (PowerShell)

Ouvre PowerShell **dans le dossier du projet** (celui où se trouve `gradlew.bat`) :

```powershell
cd C:\Users\TonNom\Documents\MyFood
.\gradlew.bat assembleDebug
```

Pour installer sur un appareil déjà connecté (USB ou émulateur allumé) :

```powershell
.\gradlew.bat installDebug
```

> Sous Windows, utilise **`gradlew.bat`**, pas `./gradlew` (syntaxe Linux/macOS).

### Fichier `local.properties`

Android Studio le crée en général automatiquement à la racine du projet :

```properties
sdk.dir=C\:\\Users\\TonNom\\AppData\\Local\\Android\\Sdk
```

Si Gradle indique que le SDK est introuvable : **File → Settings → Languages & Frameworks → Android SDK** et installe au moins **Android 14 (API 35)** ou la version demandée par le projet (`compileSdk = 35` dans `app/build.gradle.kts`).

---

## 5. Premier compte et rôle admin

### Compte client (utilisateur normal)

1. Lance l’app → **S’inscrire** (email + mot de passe, au moins 6 caractères).
2. Si l’inscription réussit mais que la connexion échoue une fois : retourne sur **Connexion** avec les mêmes identifiants.
3. Tu arrives sur l’**Accueil** : menu, panier, commandes, profil.

### Compte restaurateur (admin)

1. Inscris-toi avec ton email (comme ci-dessus).
2. Dans Supabase : **SQL Editor**, exécute (en remplaçant l’email) :

```sql
update public.profiles
set role = 'admin'
where email = 'ton.email@exemple.com';
```

3. **Déconnecte-toi** dans l’app (Profil), reconnecte-toi : tu es redirigé vers l’**espace restaurateur** (dashboard, commandes, gestion du menu).

| Rôle | Dans la base | Dans l’app |
|------|----------------|------------|
| Client | `role = 'user'` (défaut) | Menu, panier, commandes, profil |
| Admin | `role = 'admin'` | Espace restaurateur + possibilité de revenir au parcours client |

---

## 6. Structure du projet

| Chemin | Rôle |
|--------|------|
| `app/` | Code Kotlin, interface Compose, `AndroidManifest.xml` |
| `app/src/main/java/.../data/` | Supabase, modèles, repositories |
| `app/src/main/java/.../ui/` | Écrans, navigation, thème |
| `supabase_setup.sql` | **Script SQL unique** : schéma + sécurité + données de démo |
| `CAHIER_DES_CHARGES.md` | Spécifications fonctionnelles |
| `assets/` | Images d’exemple pour le menu |
| `images/` | Logo et visuels du README (`logo.webp`) |
| `.github/workflows/build-apk.yml` | CI : build APK sur push (GitHub) |

---

## 7. Problèmes fréquents sous Windows

| Symptôme | Piste de solution |
|----------|-------------------|
| **Gradle Sync failed** | Vérifie ta connexion Internet ; **File → Invalidate Caches → Restart** ; rouvre le projet. |
| **SDK location not found** | Installe le SDK via Android Studio (section 4) ; vérifie `local.properties`. |
| **Émulateur très lent** | Dans Device Manager, choisis une image **x86_64** ; active l’accélération matérielle (BIOS : virtualisation Intel/AMD). |
| **Menu vide ou erreur réseau** | Vérifie `SupabaseConfig.kt` ; exécute `supabase_setup.sql` sur **ton** projet ; désactive temporairement le pare-feu pour tester. |
| **Inscription impossible** | Supabase → Auth → Email activé ; désactive la confirmation email pour les tests. |
| **« Email ou mot de passe incorrect »** | Attends quelques secondes après l’inscription ; réessaie Connexion ; vérifie l’email dans **Authentication → Users**. |
| **`gradlew.bat` introuvable** | Ouvre PowerShell dans le bon dossier (celui qui contient `gradlew.bat`, pas seulement `app/`). |
| **Permission notifications** | Sur Android 13+, l’app demande la permission au démarrage ; tu peux refuser sans bloquer le reste. |

---

## 8. Stack technique & limites

**Stack :** Kotlin, Jetpack Compose, Material 3, Navigation Compose, Hilt, ViewModel, StateFlow, Supabase (Auth, Postgrest, Storage, Realtime), Coil, DataStore (thème), Firebase Messaging (optionnel).

**Limites connues (projet d’étude / démo) :**

- Panier en **mémoire** uniquement (perdu si Android ferme l’app).
- Peu de tests automatisés.
- Build `release` sans minification par défaut.
- Ne commite pas de clé `service_role` ; pour un dépôt public, évite d’exposer des clés Supabase sensibles.

**Documentation complémentaire :** voir `GUIDE.md` pour une analyse technique détaillée du code.

---