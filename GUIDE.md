# Guide technique — MyFood

Application Android de commande de repas.  
Stack : **Kotlin 2.1.0 · Jetpack Compose · Hilt · Supabase · Firebase FCM**

---

## Table des matières

1. [Structure des dossiers](#1-structure-des-dossiers)
2. [Couche données — `data/`](#2-couche-données--data)
3. [Injection de dépendances — `di/`](#3-injection-de-dépendances--di)
4. [Navigation — `ui/navigation/`](#4-navigation--uinavigation)
5. [ViewModels — `ui/viewmodel/`](#5-viewmodels--uiviewmodel)
6. [Écrans — `ui/screens/`](#6-écrans--uiscreens)
7. [Composants partagés — `ui/components/`](#7-composants-partagés--uicomponents)
8. [Thème — `ui/theme/`](#8-thème--uitheme)
9. [Push notifications — `push/`](#9-push-notifications--push)
10. [Backend Supabase](#10-backend-supabase)
11. [Flux essentiels](#11-flux-essentiels)
12. [Rôles utilisateur](#12-rôles-utilisateur)

---

## 1. Structure des dossiers

```
app/src/main/java/com/si/myfoodordering/
│
├── MainActivity.kt              ← Point d'entrée Android
├── FoodApplication.kt           ← @HiltAndroidApp + canaux notifs
│
├── data/
│   ├── SupabaseConfig.kt        ← Client Supabase (URL + clé anon)
│   ├── model/
│   │   └── Models.kt            ← Tous les modèles de données (DTOs)
│   ├── repository/
│   │   ├── AuthRepository.kt
│   │   ├── PlatRepository.kt
│   │   ├── OrderRepository.kt
│   │   ├── FavoriRepository.kt
│   │   ├── AvisRepository.kt
│   │   └── PushTokenRepository.kt
│   └── local/
│       └── PreferenceManager.kt ← DataStore (thème, préférences)
│
├── di/
│   └── NetworkModule.kt         ← Hilt : fourniture des dépendances
│
├── push/
│   ├── MyFirebaseMessagingService.kt
│   └── NotificationChannels.kt
│
└── ui/
    ├── navigation/
    │   ├── NavGraph.kt           ← Routes + NavHost + FloatingBottomBar
    │   └── MainScreen.kt         ← (legacy, non utilisé)
    ├── viewmodel/
    │   ├── SplashViewModel.kt
    │   ├── AuthViewModel.kt
    │   ├── PlatViewModel.kt
    │   ├── CartViewModel.kt
    │   ├── OrderViewModel.kt
    │   ├── CheckoutViewModel.kt
    │   ├── FavorisViewModel.kt
    │   ├── AvisViewModel.kt
    │   ├── ThemeViewModel.kt
    │   ├── AdminMainViewModel.kt
    │   └── AdminStatsViewModel.kt
    ├── screens/
    │   ├── splash/SplashScreen.kt
    │   ├── auth/          LoginScreen · RegisterScreen
    │   ├── home/          HomeScreen · PlatDetailScreen
    │   ├── cart/          CartScreen
    │   ├── checkout/      CheckoutScreen · OrderSuccessScreen
    │   ├── orders/        OrdersScreen · OrderDetailScreen
    │   ├── favoris/       FavorisScreen
    │   ├── profile/       ProfileScreen
    │   └── admin/         AdminMainScreen · AdminDashboardScreen
    │                      AdminOrdersTab · AdminPlatsScreen
    │                      EditPlatScreen · CategoryManagementSection
    │                      AdminProfileTab · AdminSwipeableOrderCard
    ├── components/        Composables réutilisables
    ├── theme/             Couleurs · Typography · Theme
    └── util/
        └── UiMessageBus.kt ← Toast global via SharedFlow
```

---

## 2. Couche données — `data/`

### 2.1 `SupabaseConfig.kt`

Crée et expose le **client Supabase singleton** avec les trois plugins installés :

```kotlin
object SupabaseConfig {
    const val SUPABASE_URL = "https://xxx.supabase.co"
    const val SUPABASE_KEY = "eyJ..."   // clé anon publique

    val client by lazy {
        createSupabaseClient(SUPABASE_URL, SUPABASE_KEY) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}
```

> ⚠️ En production, déplacer `URL` et `KEY` dans `local.properties` + `BuildConfig`.

---

### 2.2 `Models.kt` — tous les DTOs

| Classe | Table Supabase | Rôle |
|--------|---------------|------|
| `Category` | `categories` | Catégorie de plat |
| `Plat` | `plats` | Plat du menu |
| `UserProfile` | `profiles` | Profil utilisateur (nom, email, rôle, téléphone, adresse) |
| `Order` | `commandes` | Commande passée |
| `OrderItem` | `commande_items` | Ligne d'une commande |
| `CartItem` | *(mémoire)* | Plat + quantité dans le panier |
| `Favori` | `favoris` | Plat favori d'un user |
| `Avis` | `avis` | Note (1–5) d'un user sur un plat |
| `PushTokenRow` | `push_tokens` | Token FCM enregistré |
| `OrderWithItems` | jointure | Ligne commande avec plat embarqué |
| `PlatPopularite` | *(calculé)* | Plat + nb commandes + note moyenne |

Tous sont annotés `@Serializable` pour la désérialisation Supabase.

---

### 2.3 Repositories

Chaque repository reçoit le `SupabaseClient` par injection Hilt et effectue les appels réseau sur `Dispatchers.IO`.

#### `AuthRepository`
| Méthode | Action |
|---------|--------|
| `signUp(email, password, name)` | Crée le compte + tente une connexion auto |
| `signIn(email, password)` | Connexion GoTrue (email) |
| `signOut()` | Déconnexion + invalidation session |
| `getUserProfile()` | Récupère la ligne `profiles` de l'utilisateur connecté |
| `updateProfile(name, phone, address)` | Met à jour `profiles` |
| `changePassword(current, new)` | Re-auth + `updateUser` |
| `insertProfileIfMissing(nom, email)` | Fallback si le trigger SQL est lent |
| `getCurrentUser()` | Vérification locale de session (pas de réseau) |

#### `PlatRepository`
| Méthode | Action |
|---------|--------|
| `loadMenu()` | Charge plats + catégories en parallèle |
| `addPlat(plat)` | Insère un nouveau plat |
| `updatePlat(plat)` | Met à jour un plat existant |
| `deletePlat(platId)` | Supprime un plat |
| `addCategory / updateCategory / deleteCategory` | CRUD catégories |
| `uploadPlatCoverImage(bytes, mimeType)` | Upload sur bucket `plats` → URL publique |

#### `OrderRepository`
| Méthode | Action |
|---------|--------|
| `createOrder(order, items)` | Crée la commande + ses items (2 requêtes) |
| `getMyOrders()` | Commandes de l'utilisateur connecté |
| `getAllOrdersForAdmin()` | Toutes les commandes (RLS admin) |
| `getOrderItems(orderId)` | Items d'une commande avec plat embarqué |
| `getAllOrderItems()` | Tous les items toutes commandes (stats admin) |
| `updateOrderStatus(orderId, status)` | Change le statut d'une commande |

#### `FavoriRepository`
| Méthode | Action |
|---------|--------|
| `getFavoris()` | Plats favoris de l'utilisateur (jointure) |
| `getFavoriIds()` | Set des IDs favoris |
| `addFavori(platId)` | Ajoute un favori |
| `removeFavori(platId)` | Retire un favori |
| `toggleFavori(platId, currentlyFavori)` | Bascule |

#### `AvisRepository`
| Méthode | Action |
|---------|--------|
| `getAvisForPlat(platId)` | Notes d'un plat (lecture publique) |
| `getAllAvis()` | Tous les avis tous plats (stats admin) |
| `getUserAvisForPlat(platId)` | Note personnelle sur un plat |
| `upsertAvis(platId, note)` | Insère ou met à jour la note |

---

## 3. Injection de dépendances — `di/`

`NetworkModule.kt` est un module Hilt `@Singleton` qui expose :

```
SupabaseClient  ←  provideSupabaseClient()
     │
     ├── AuthRepository
     ├── PlatRepository
     ├── OrderRepository
     ├── FavoriRepository
     ├── AvisRepository
     └── PushTokenRepository
```

Toutes les classes annotées `@HiltViewModel` reçoivent leurs repositories via `@Inject constructor(...)`.

---

## 4. Navigation — `ui/navigation/`

### 4.1 Routes (`Screen` sealed class)

| Objet | Route | Rôle |
|-------|-------|------|
| `Splash` | `splash` | Page de démarrage |
| `Login` | `login` | Connexion |
| `Register` | `register` | Inscription |
| `Home` | `home` | Accueil / menu |
| `Favoris` | `favoris` | Plats favoris |
| `Cart` | `cart` | Panier |
| `Orders` | `orders` | Historique commandes |
| `Profile` | `profile` | Profil utilisateur |
| `PlatDetail` | `plat_detail/{platId}` | Détail d'un plat |
| `Checkout` | `checkout` | Page de commande |
| `OrderSuccess` | `order_success` | Confirmation |
| `OrderDetail` | `order_detail/{orderId}?admin={bool}` | Détail commande |
| `AdminMain` | `admin_main` | Shell admin |
| `EditPlat` | `edit_plat/{platId}` | Ajout/édition plat |

### 4.2 `SetupNavGraph()`

- `startDestination` = `splash`
- Après le splash : `popUpTo(splash) { inclusive = true }` → le splash est retiré de la pile
- `FloatingAppBottomBar` affiché uniquement sur les 5 onglets clients (`Home`, `Favoris`, `Cart`, `Orders`, `Profile`)
- Transitions : `slideInHorizontally` + `fadeIn` (500 ms)
- `UiMessageBus` collecté ici pour afficher les toasts globaux

---

## 5. ViewModels — `ui/viewmodel/`

### `SplashViewModel`
Vérifie la session au démarrage et émet une destination :
- `getCurrentUser()` → pas de réseau (local)
- Si session valide → fetch profil → `Home` ou `Admin`
- Durée minimale d'affichage : **1 800 ms**

### `AuthViewModel`
Gère tout le cycle d'authentification :
- `login()` : signIn + fetch profil (avec 5 tentatives car le trigger SQL peut être lent)
- `register()` : signUp + attente trigger + fallback `insertProfileIfMissing`
- `logout()` : révocation token FCM + signOut
- `updateProfile()`, `changePassword()`

### `PlatViewModel`
Cache local des plats et catégories :
- Chargé au `init` via `loadData()`
- `savePlat()` : add ou update selon `plat.id == null`
- `uploadPlatCoverImage()` : délègue à `PlatRepository`
- `saveCategory()`, `deleteCategory()`

### `CartViewModel`
Panier **en mémoire uniquement** (non persisté) :
- `addToCart(plat)` : incrémente si déjà présent, sinon ajoute
- `removeFromCart(plat)` : décrémente ou retire
- `totalAmount` : Flow calculé (somme prix × quantité)
- `itemsCount` : Flow du badge panier

### `OrderViewModel`
- `loadMyOrders()` / `loadAllOrdersForAdmin()`
- `loadOrderItems(orderId)` : items avec plat embarqué
- `updateOrderStatus()` : admin uniquement

### `CheckoutViewModel`
Orchestre la création de commande :
- Valide les champs (adresse, téléphone)
- Appelle `orderRepository.createOrder()`
- Émet `isSuccess`

### `FavorisViewModel`
- Charge les favoris au `init`
- `toggleFavori(platId)` : optimistic update sur `favoriIds`

### `AvisViewModel`
- `loadAvis(platId)` + `loadUserAvis(platId)`
- `submitAvis(platId, note)`

### `ThemeViewModel`
- Lit/écrit `isDarkTheme` dans **DataStore**
- Utilisé dans `MainActivity` pour appliquer le thème globalement

### `AdminMainViewModel`
- Contient uniquement `selectedTab: StateFlow<Int>`
- `selectTab(index)` pour changer d'onglet admin

### `AdminStatsViewModel`
Calcule les statistiques du tableau de bord admin :
- Charge **en parallèle** (`async/await`) : plats, items de commandes, avis
- Calcule `PlatPopularite` : nb commandes commandées + note moyenne
- Tri dynamique : `PAR_COMMANDES` ou `PAR_NOTE`
- Top 5 uniquement

---

## 6. Écrans — `ui/screens/`

### Splash — `SplashScreen.kt`
- Animation `scale` + `fade-in` sur le logo
- `CircularProgressIndicator` pendant la vérification de session
- Navigation automatique dès que `SplashViewModel.destination` change

### Auth
| Écran | Rôle |
|-------|------|
| `LoginScreen` | Formulaire email/password → `AuthViewModel.login()` |
| `RegisterScreen` | Formulaire nom/email/password → `AuthViewModel.register()` |

### Home
| Écran | Rôle |
|-------|------|
| `HomeScreen` | Liste des plats avec filtre par catégorie + recherche texte |
| `PlatDetailScreen` | Photo, description, prix, notation (étoiles), ajout panier |

### Cart → Checkout
| Écran | Rôle |
|-------|------|
| `CartScreen` | Liste des items, quantités, total |
| `CheckoutScreen` | Adresse, téléphone, résumé → création commande |
| `OrderSuccessScreen` | Animation de confirmation |

### Orders
| Écran | Rôle |
|-------|------|
| `OrdersScreen` | Historique des commandes de l'utilisateur |
| `OrderDetailScreen` | Détail d'une commande (items + statut). Partagé client/admin |

### Favoris — `FavorisScreen`
Liste des plats mis en favori. Bouton toggle cœur + ajout au panier.

### Profile — `ProfileScreen`
- Édition nom, téléphone, adresse
- Toggle thème clair/sombre
- Bouton "Changer le mot de passe" (dialog)
- Bouton "Interface Admin" si `role == "admin"`
- Déconnexion

### Admin

| Fichier | Rôle |
|---------|------|
| `AdminMainScreen` | Shell avec `FloatingAppBottomBar` (4 onglets : Accueil, Commandes, Menu, Profil) |
| `AdminDashboardScreen` | Stats (CA, nb commandes, statuts), **Top 5 plats populaires**, commandes urgentes |
| `AdminOrdersTab` | Liste complète des commandes avec actions de statut |
| `AdminSwipeableOrderCard` | Carte commande avec swipe pour changer le statut |
| `AdminPlatsScreen` | Liste du menu avec suppression |
| `EditPlatScreen` | Formulaire add/edit plat + upload image |
| `CategoryManagementSection` | CRUD catégories (emoji + nom) |
| `AdminProfileTab` | Profil admin + bouton "Accès client" + déconnexion |

---

## 7. Composants partagés — `ui/components/`

| Fichier | Rôle |
|---------|------|
| `FloatingAppBottomBar` | Barre de navigation flottante animée (client et admin) |
| `AppHeader` | TopBar avec titre centré, icône de retour optionnelle, slot d'actions |
| `CategoryFilterChip` | Chip de filtre de catégorie (avec emoji) |
| `PlatCoverAsyncImage` | Image Coil avec placeholder shimmer pour les photos de plats |
| `RatingBar` | Affichage et saisie des étoiles (1–5) |
| `AuthLoadingOverlay` | Overlay semi-transparent + spinner pendant auth |
| `ChangePasswordDialog` | Dialog modale pour changer le mot de passe |
| `BrandedCircleIcon` | Icône circulaire avec couleur primaire (décoration) |

---

## 8. Thème — `ui/theme/`

### Palette (`Color.kt`)

| Token | Valeur | Utilisation |
|-------|--------|-------------|
| `PrimaryOrange` | `#FF6B01` | Couleur principale (boutons, accents) |
| `SecondaryOrange` | `#FF8E42` | Variante secondaire |
| `SuccessGreen` | `#4CAF50` | Statut "Livré", confirmations |
| `DeepBlack` | `#0F0F0F` | Background dark mode |
| `SurfaceDark` | `#1E1E1E` | Surface dark mode |

### `Theme.kt`
- `MyFoodOrderingTheme(darkTheme: Boolean)` wraps `MaterialTheme`
- Gère automatiquement `statusBarColor` et `navigationBarColor`
- Expose `LocalAppDarkTheme` via `CompositionLocalProvider`

### `Typography.kt`
Typographie Material 3 personnalisée.

---

## 9. Push notifications — `push/`

### `MyFirebaseMessagingService`
- Hérite de `FirebaseMessagingService`
- `onNewToken(token)` → enregistre/rafraîchit le token dans `push_tokens`
- `onMessageReceived(message)` → affiche la notification via `NotificationManager`

### `PushTokenRepository`
- `registerCurrentToken()` : supprime tous les anciens tokens de l'user puis insère le nouveau
- `revokeLocalTokens()` : appelé au logout pour supprimer le token de la DB

### Edge Function Supabase (`supabase/functions/push-order-notification/`)
Fonction Deno déclenchée par webhook Supabase sur insert/update de `commandes` :
1. Récupère les tokens FCM de l'utilisateur depuis `push_tokens`
2. Envoie la notification via l'API Firebase v1 (service account)

---

## 10. Backend Supabase

### Tables

```
profiles          ← créée automatiquement par trigger handle_new_user
categories
plats             ← image_url pointe vers bucket "plats"
commandes
commande_items
favoris
avis
push_tokens
```

### RLS (Row Level Security)

| Table | Règle |
|-------|-------|
| `profiles` | Lecture/écriture sur sa propre ligne uniquement |
| `categories` | Lecture publique · Écriture admin |
| `plats` | Lecture publique · Écriture admin |
| `commandes` | Lecture/écriture sur ses propres commandes · Admin : tout |
| `commande_items` | Même règle que commandes |
| `favoris` | Sa propre ligne uniquement |
| `avis` | Lecture publique · Écriture sur sa propre note |
| `push_tokens` | Sa propre ligne uniquement |

### Storage

Bucket `plats` : lecture publique, écriture admin uniquement.  
Les URLs sont de la forme `https://<project>.supabase.co/storage/v1/object/public/plats/covers/<uuid>.jpg`

### Trigger SQL

```sql
-- Créé automatiquement dans supabase_setup.sql
CREATE FUNCTION handle_new_user()
RETURNS trigger AS $$
BEGIN
  INSERT INTO profiles (id, nom, email, role)
  VALUES (NEW.id, NEW.raw_user_meta_data->>'nom', NEW.email, 'user')
  ON CONFLICT (id) DO NOTHING;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION handle_new_user();
```

---

## 11. Flux essentiels

### Démarrage de l'app

```
MainActivity.onCreate()
    └── SetupNavGraph()  ← startDestination = splash
            └── SplashScreen
                    └── SplashViewModel.checkSession()
                            ├── getCurrentUser() == null  →  Login
                            ├── getUserProfile().role == "admin"  →  AdminMain
                            └── getUserProfile().role == "user"   →  Home
```

### Inscription

```
RegisterScreen
    └── AuthViewModel.register(email, password, name)
            ├── authRepository.signUp()          ← crée le compte
            ├── delay(1500ms)                    ← trigger SQL lent
            ├── authRepository.getUserProfile()  ← 5 tentatives
            ├── [si null] insertProfileIfMissing()
            └── onSuccess(isAdmin)  →  NavGraph navigue vers Home ou Admin
```

### Passer une commande

```
CartScreen  →  CheckoutScreen
                    └── CheckoutViewModel.placeOrder()
                            ├── orderRepository.createOrder(order, cartItems)
                            │       ├── INSERT INTO commandes  →  orderId
                            │       └── INSERT INTO commande_items (bulk)
                            ├── cartViewModel.clearCart()
                            └── navigate → OrderSuccess
                                    └── [webhook Supabase] Edge Function FCM
```

### Cycle de vie d'une commande (admin)

```
"En attente"  →  "En préparation"  →  "En livraison"  →  "Livré"
    │                   │                   │
    └── AdminOrderCard : swipe ou bouton → orderViewModel.updateOrderStatus()
```

---

## 12. Rôles utilisateur

| Fonctionnalité | `user` | `admin` |
|----------------|--------|---------|
| Parcourir le menu | ✅ | ✅ |
| Ajouter au panier | ✅ | — |
| Passer une commande | ✅ | — |
| Voir ses commandes | ✅ | ✅ (toutes) |
| Favoris | ✅ | — |
| Notation (avis) | ✅ | — |
| Modifier son profil | ✅ | ✅ |
| Gérer le menu (CRUD) | — | ✅ |
| Gérer les catégories | — | ✅ |
| Changer le statut commande | — | ✅ |
| Tableau de bord stats | — | ✅ |
| Top plats populaires | — | ✅ |
| Upload image plat | — | ✅ |

---

*Guide généré le 14 mai 2026 — MyFood v1.0*
