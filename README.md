# Cards4Play

Backend Java para gestionar productos coleccionables, clientes, compras, boosters y torneos. La API REST usa Spark Java, guarda los datos en JSON e integra ExchangeRate-API para consultar la tasa USD/COP.

## Integrantes

- David Santiago Sierra Mora
- Juan Felipe Arias Orozco

## Requisitos

- Java 25 o superior (LTS).
- Docker Desktop para ejecutar el contenedor.

## API REST

Las rutas `/estado` y `/tasa` son públicas. Las demás usan autenticación HTTP Basic:

- Administrador inicial: `admin` / `admin123`.
- Las acciones de clientes requieren las credenciales del cliente registrado.

| Método | Ruta | Acceso |
|---|---|---|
| GET | `/health` | Público |
| GET | `/estado` | Público |
| GET | `/tasa` | Público |
| GET | `/auth/me` | Autenticado |
| GET | `/productos` | Autenticado |
| GET | `/productos/:id` | Autenticado |
| POST | `/productos/cartas` | ADMIN |
| POST | `/productos/sellados` | ADMIN |
| POST | `/productos/accesorios` | ADMIN |
| POST | `/productos/boosters` | ADMIN |
| PUT | `/productos/cartas/:id` | ADMIN |
| PUT | `/productos/sellados/:id` | ADMIN |
| PUT | `/productos/accesorios/:id` | ADMIN |
| DELETE | `/productos/:id` | ADMIN |
| POST | `/clientes` | ADMIN |
| GET | `/clientes` | ADMIN |
| GET, PUT, DELETE | `/clientes/:id` | ADMIN |
| GET, POST | `/carrito` | CLIENTE |
| DELETE | `/carrito/:productoId` | CLIENTE |
| POST | `/compras` | CLIENTE |
| GET | `/compras` | CLIENTE |
| POST | `/boosters/:id/abrir` | CLIENTE comprador |
| GET | `/torneos` | Autenticado |
| GET | `/torneos/:id` | Autenticado |
| POST | `/torneos` | ADMIN |
| POST | `/torneos/:id/inscripciones` | CLIENTE |

Las respuestas de error usan JSON con la propiedad `error`. La API contempla, entre otros, los estados `400`, `401`, `403`, `404` y `409`.

## ExchangeRate-API

La aplicación consulta ExchangeRate-API para obtener la tasa de USD a COP. La clave se lee desde `EXCHANGE_API_KEY`; no debe guardarse en el repositorio. Si la consulta falla, usa `TASA_DEFECTO` (por defecto `4000`) y las respuestas con precios incluyen `fuenteTasa` (`API` o `DEFECTO`).

## Ejecutar con Docker

Construye la imagen desde la raíz del proyecto:

```powershell
docker build -t cards4play .
```

Crea un volumen persistente:

```powershell
docker volume create cards4play-data
```

Ejecuta el contenedor, sustituyendo el marcador por la clave de ExchangeRate-API:

```powershell
docker run --name cards4play -p 4567:4567 `
  -e EXCHANGE_API_KEY="TU_CLAVE" `
  -e TASA_DEFECTO="4000" `
  -v cards4play-data:/app/data `
  cards4play
```

La API queda disponible en `http://localhost:4567`. Los datos JSON se guardan en `/app/data/datos.json` dentro del volumen `cards4play-data`, y se conservan al eliminar y volver a crear el contenedor.

Para detener y eliminar el contenedor sin borrar el volumen:

```powershell
docker stop cards4play
docker rm cards4play
```

## Probar con Postman

Importa la colección exportada `CardsPlay.postman_collection.json`. Configura Basic Auth para cada solicitud protegida. La clave de ExchangeRate-API se configura en el servidor (Docker o PaaS), no en Postman.

## Despliegue en PaaS

Construye/despliega el proyecto como servicio Docker y configura las variables `EXCHANGE_API_KEY` y `TASA_DEFECTO` en la plataforma. La aplicación escucha el puerto indicado por `PORT` cuando la plataforma lo proporciona. La persistencia local del volumen no aplica necesariamente en PaaS.
