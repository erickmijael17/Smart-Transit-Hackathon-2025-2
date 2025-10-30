# Smart-Transit-Hackathon-2025-2
El backend es el motor que procesa y gestiona los datos en tiempo real. Está construido sobre una arquitectura robusta y eficiente basada en Spring Boot y PostgreSQL.

Tecnologías Fundamentales:
Spring Boot / Java: Marco principal para el desarrollo de los servicios RESTful y la lógica de negocio.

PostgreSQL con Spring Data JPA: Base de datos relacional robusta para la persistencia de datos de usuarios, rutas, histórico de tracking y configuración del sistema.

OpenRouteService (ORS) API: Esencial para el cálculo de rutas óptimas, la generación de ETAs fiables y la función Snap-to-Road para ajustar las coordenadas GPS de los vehículos a la red vial.

Nominatim y Photon API: Utilizados para las tareas de Geocodificación (conversión de coordenadas a direcciones legibles) y para las búsquedas rápidas en la interfaz.

Overpass API: Permite realizar consultas avanzadas a OpenStreetMap para localizar Puntos de Interés (POIs) y elementos de la infraestructura de transporte, como las paradas de autobús.

Sistema de Caché de Spring: Implementación crucial para optimizar el rendimiento, reducir la latencia de las peticiones repetidas y asegurar el cumplimiento de los límites de tasa (Rate Limits) de las APIs gratuitas de geocodificación.

## Link Frontend: https://github.com/erickmijael17/Smart-Transit-Hackathon-2025-2-Frontend.git
