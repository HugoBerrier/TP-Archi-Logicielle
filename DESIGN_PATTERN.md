# Design Pattern - Reservation Service

## Pattern choisi : State (behavioral)

Le cycle de vie d'une reservation suit des transitions simples mais strictes :

- `CONFIRMED -> CANCELLED`
- `CONFIRMED -> COMPLETED`
- `CANCELLED` et `COMPLETED` sont des etats finaux

Ce contexte est adapte au **State Pattern**, car le comportement (`cancel`, `complete`) depend directement de l'etat courant.

## Implementation

Dans `reservation-service`, le pattern est implemente via :

- `ReservationStateHandler` : contrat des transitions
- `ConfirmedReservationState` : autorise les transitions vers `CANCELLED` et `COMPLETED`
- `FinalReservationState` : bloque les transitions une fois l'etat final atteint
- `ReservationStateResolver` : selectionne le handler selon `ReservationStatus`

Le service `ReservationService` delegue maintenant les transitions de statut au resolver, ce qui evite les `if` repetitifs et centralise les regles.

## Benefices

- regles de transition explicites et testables
- code plus lisible dans le service metier
- extension plus simple si un nouvel etat est ajoute
