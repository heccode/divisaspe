let usuarioActivo = null;

function pintarResultado(id, nodos) {
  const cont = $(id);
  cont.replaceChildren(...nodos);
  cont.hidden = false;
}

async function iniciar() {
  montarMenu("/");
  try {
    const divisas = await api("/api/v1/divisas");
    for (const id of ["origen", "destino"]) {
      const sel = $(id), previo = sel.value;
      sel.replaceChildren(...divisas.map((d) => {
        const o = el("option", d.codigo);
        o.title = d.nombre;
        return o;
      }));
      sel.value = previo;
    }
  } catch (e) {}
  cargarTipoCambio();
  restaurarSesion();
  const aviso = tomarAviso();
  if (aviso) toast(aviso);
}

async function cargarTipoCambio() {
  const estado = $("tc-estado");
  $("tc-datos").hidden = true;
  estado.className = "estado";
  estado.textContent = "Consultando el tipo de cambio...";
  try {
    const tc = await conCarga($("tc-recargar"), () => api("/api/v1/tipo-cambio"));
    $("tc-compra").textContent = tc.compra;
    $("tc-venta").textContent = tc.venta;
    $("tc-fecha").textContent = tc.fechaReferencia;
    $("tc-fuente").textContent = tc.fuente;
    $("tc-datos").hidden = false;
    estado.textContent = "";
  } catch (e) {
    estado.className = "estado error";
    estado.textContent = e.message;
  }
}

async function restaurarSesion() {
  const guardado = usuarioGuardado();
  if (!guardado) return;
  usuarioActivo = guardado;
  try {
    usuarioActivo = await api("/api/v1/auth/me");
    guardarSesion(usuarioActivo);
    montarMenu("/");
  } catch (e) {
    if (e.status === 401 || e.status === 404) {
      cerrarSesion();
      usuarioActivo = null;
      montarMenu("/");
    }
  }
}

function alExpirarSesion() {
  usuarioActivo = null;
  montarMenu("/");
  toast("Tu sesión expiró. Inicia sesión de nuevo.", true);
}

function leerConversion() {
  const monto = parseFloat($("monto").value);
  const monedaOrigen = $("origen").value, monedaDestino = $("destino").value;
  if (!(monto > 0)) { mostrarError("conv-error", "El monto tiene que ser mayor que cero."); return null; }
  if (monedaOrigen === monedaDestino) { mostrarError("conv-error", "Elige dos monedas distintas para convertir."); return null; }
  return { monto, monedaOrigen, monedaDestino };
}

function lineaCambio(r, estimado) {
  return [
    el("div", `${dinero(r.montoOriginal)} ${r.monedaOrigen} = ${dinero(estimado)} ${r.monedaDestino}`, "grande"),
    el("div", `Tipo de cambio aplicado: ${r.tipoCambioAplicado} · Datos al ${r.fechaReferencia} · Fuente: ${r.fuente}`),
  ];
}

async function cotizar() {
  limpiar("conv-error", "conv-resultado");
  const datos = leerConversion();
  if (!datos) return;
  try {
    const c = await conCarga(botonesConversion(), () => postJson("/api/v1/cotizaciones/referencial", datos));
    const b = el("span", "Cotización · no se guardó", "badge neutro");
    pintarResultado("conv-resultado", [b, ...lineaCambio(c, c.montoEstimado), el("em", "Es una estimación para que te hagas una idea.")]);
  } catch (e) {
    mostrarError("conv-error", e.message);
  }
}

async function convertir(registrar) {
  limpiar("conv-error", "conv-resultado");
  const datos = leerConversion();
  if (!datos) return;
  if (registrar) {
    if (!usuarioActivo) return mostrarError("conv-error", "Para guardar la conversión, primero inicia sesión o crea tu cuenta.");
    datos.usuarioId = usuarioActivo.id;
  }
  try {
    const r = await conCarga(botonesConversion(), () => postJson("/api/v1/conversiones/referencial", datos));
    const guardada = r.operacionId != null;
    const b = el("span", guardada ? "Guardada en tu historial" : "Conversión · no se guardó",
      guardada ? "badge" : "badge neutro");
    const nodos = [b, ...lineaCambio(r, r.resultado), el("em", "Valor referencial: no es una operación real.")];
    if (guardada) {
      const enlace = el("a", "Ver mi historial", "enlace-resultado");
      enlace.href = "/historial";
      nodos.push(enlace);
    }
    pintarResultado("conv-resultado", nodos);
    if (guardada) toast("Conversión guardada en tu historial.");
  } catch (e) {
    mostrarError("conv-error", e.message);
  }
}

function botonesConversion() {
  return [$("btn-cotizar"), $("btn-convertir"), $("btn-registrar")];
}

$("tc-recargar").addEventListener("click", cargarTipoCambio);
$("btn-cotizar").addEventListener("click", cotizar);
$("btn-convertir").addEventListener("click", () => convertir(false));
$("form-conv").addEventListener("submit", (ev) => { ev.preventDefault(); convertir(true); });

iniciar();
