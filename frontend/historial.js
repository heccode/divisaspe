const usuario = protegerPagina();
montarMenu("/historial");

async function cargarHistorial() {
  const estado = $("hist-estado");
  limpiar("hist-error", "hist-tabla");
  estado.hidden = false;
  estado.replaceChildren("Cargando tu historial...");
  try {
    const ops = await conCarga($("hist-recargar"), () => api(`/api/v1/usuarios/${usuario.id}/historial`));
    $("hist-filas").replaceChildren(...ops.map((o) => {
      const tr = document.createElement("tr");
      [o.fechaOperacion.replace("T", " ").slice(0, 19), `${dinero(o.monto)} ${o.monedaOrigen}`,
        o.tipoCambioAplicado, `${dinero(o.resultado)} ${o.monedaDestino}`]
        .forEach((c) => tr.append(el("td", String(c))));
      return tr;
    }));
    if (ops.length === 0) {
      const enlace = el("a", "Ir a convertir");
      enlace.href = "/";
      estado.replaceChildren("Todavía no has guardado ninguna conversión. ", enlace);
    } else {
      estado.hidden = true;
      $("hist-tabla").hidden = false;
    }
  } catch (e) {
    estado.hidden = true;
    mostrarError("hist-error", e.message);
  }
}

$("hist-recargar").addEventListener("click", cargarHistorial);
if (usuario) cargarHistorial();
