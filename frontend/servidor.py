import os
import sys
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer

RUTAS = {
    "/iniciar-sesion": "/iniciar-sesion.html",
    "/registro": "/registro.html",
    "/cuentas-bancarias": "/cuentas.html",
    "/historial": "/historial.html",
    "/perfil": "/perfil.html",
}


class Manejador(SimpleHTTPRequestHandler):
    def do_GET(self):
        self._reescribir()
        super().do_GET()

    def do_HEAD(self):
        self._reescribir()
        super().do_HEAD()

    def _reescribir(self):
        ruta = self.path.split("?")[0].split("#")[0].rstrip("/") or "/"
        if ruta in RUTAS:
            self.path = RUTAS[ruta]

    def end_headers(self):
        self.send_header("Cache-Control", "no-cache")
        super().end_headers()

    def log_message(self, *args):
        pass


if __name__ == "__main__":
    puerto = int(sys.argv[1]) if len(sys.argv) > 1 else 5500
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    ThreadingHTTPServer(("", puerto), Manejador).serve_forever()
