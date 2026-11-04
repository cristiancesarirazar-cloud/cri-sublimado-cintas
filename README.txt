CRI SUBLIMADO - ARMADOR DE CINTAS (proyecto Android)

Este paquete es el proyecto fuente para generar una APK Android. No es todavía una APK instalada: se compila en GitHub Actions.

PASOS:
1. En GitHub, crear un repositorio nuevo llamado cri-sublimado-cintas (puede ser público).
2. Subir TODO el contenido de esta carpeta a la raíz del repositorio, conservando la carpeta .github.
3. En GitHub abrir la pestaña Actions y habilitar workflows si GitHub lo solicita.
4. Ejecutar el workflow “Construir APK Cri Sublimado” con Run workflow (o esperar a que termine el primer push).
5. Al finalizar, abrir la ejecución y descargar el artefacto “Cri-Sublimado-Cintas-APK”.
6. Extraer el ZIP del artefacto en el celular y abrir app-debug.apk. Android pedirá permiso para instalar aplicaciones de esa fuente.

La APK contiene el HTML localmente, por eso el armado básico funciona sin Netlify ni conexión a Internet. La selección de fotos usa el selector de archivos de Android. La impresión/PDF utiliza el cuadro de impresión de Android; revisar el tamaño personalizado y seleccionar tamaño real/100% si el controlador lo ofrece.

NOTA: Es una APK de depuración para uso personal, no una publicación de Play Store. Para actualizar el programa hay que reemplazar app/src/main/assets/index.html y volver a ejecutar el workflow.
