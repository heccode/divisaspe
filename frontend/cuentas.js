const usuario = protegerPagina();
montarMenu("/cuentas-bancarias");

const TEXTO_FLUJO = {
  ENVIO: { titulo: "Agregar cuenta para enviar dinero", frase: "para enviar dinero a la casa de cambio" },
  RECEPCION: { titulo: "Agregar cuenta para recibir dinero", frase: "para recibir dinero de la casa de cambio" },
};
const TEXTO_MONEDA = { USD: "Dólares (USD)", PEN: "Soles (PEN)" };

let bancos = [];
let flujoActual = null;

function tarjetaCuenta(c) {
  const t = el("article", undefined, "cuenta-banco");
  const cabecera = el("div", undefined, "cuenta-banco-cab");
  cabecera.append(el("strong", c.banco.nombre), el("span", c.moneda === "USD" ? "Dólares · USD" : "Soles · PEN", `moneda-etiqueta moneda-${c.moneda}`));
  const datos = el("dl", undefined, "cuenta-banco-datos");
  [["Cuenta", c.numeroCuenta], ["CCI", c.cci], ["Titular", c.titular]].forEach(([k, v]) => {
    const fila = el("div");
    fila.append(el("dt", k), el("dd", v));
    datos.append(fila);
  });
  t.append(cabecera, datos);
  return t;
}

function pintarCuentas(cuentas) {
  for (const flujo of Object.keys(TEXTO_FLUJO)) {
    const lista = $(`lista-${flujo}`);
    const delFlujo = cuentas.filter((c) => c.flujo === flujo);
    if (delFlujo.length === 0) {
      lista.replaceChildren(el("p", "Todavía no has agregado cuentas para este uso.", "estado"));
      continue;
    }
    const grupos = ["USD", "PEN"].map((moneda) => {
      const cuentasMoneda = delFlujo.filter((c) => c.moneda === moneda);
      if (cuentasMoneda.length === 0) return null;
      const g = el("div", undefined, "grupo-moneda");
      g.append(el("h3", TEXTO_MONEDA[moneda]), ...cuentasMoneda.map(tarjetaCuenta));
      return g;
    }).filter(Boolean);
    lista.replaceChildren(...grupos);
  }
}

async function cargarCuentas() {
  limpiar("cuentas-error");
  try {
    pintarCuentas(await api(`/api/v1/usuarios/${usuario.id}/cuentas-bancarias`));
  } catch (e) {
    mostrarError("cuentas-error", e.message);
  }
}

async function cargarBancos() {
  try {
    bancos = await api("/api/v1/bancos");
    $("cta-banco").replaceChildren(...bancos.map((b) => {
      const o = el("option", b.nombre);
      o.value = b.id;
      return o;
    }));
  } catch (e) {
    mostrarError("cuentas-error", e.message);
  }
}

function abrirFormulario(flujo) {
  flujoActual = flujo;
  $("form-cuenta").reset();
  limpiar("cta-error");
  $("dlg-cuenta-titulo").textContent = TEXTO_FLUJO[flujo].titulo;
  $("dlg-cuenta").showModal();
  $("cta-numero").focus();
}

function validarFormulario() {
  const banco = bancos.find((b) => String(b.id) === $("cta-banco").value);
  const numeroCuenta = $("cta-numero").value.replace(/[\s-]/g, "");
  const cci = $("cta-cci").value.replace(/[\s-]/g, "");
  const titular = $("cta-titular").value.trim().replace(/\s+/g, " ");
  if (!banco) return { error: "Elige un banco." };
  if (!/^\d{10,20}$/.test(numeroCuenta)) return { error: "El número de cuenta debe tener entre 10 y 20 dígitos, solo números." };
  if (!/^\d{20}$/.test(cci)) return { error: "El CCI debe tener 20 dígitos, solo números." };
  if (!cci.startsWith(banco.prefijoCci)) return { error: `Este CCI no corresponde a ${banco.nombre}. Revisa el banco o el número.` };
  if (!/^\p{L}[\p{L}'.-]*( \p{L}[\p{L}'.-]*)+$/u.test(titular)) return { error: "Escribe los nombres y apellidos del titular, solo letras." };
  return { banco, datos: { flujo: flujoActual, moneda: $("cta-moneda").value, bancoId: banco.id, numeroCuenta, cci, titular } };
}

async function guardarCuenta(ev) {
  ev.preventDefault();
  limpiar("cta-error");
  const { error, banco, datos } = validarFormulario();
  if (error) return mostrarError("cta-error", error);
  try {
    const c = await conCarga(botonEnvio(ev), () => postJson(`/api/v1/usuarios/${usuario.id}/cuentas-bancarias`, datos));
    $("dlg-cuenta").close();
    mostrarExito("Cuenta agregada",
      `Tu cuenta en ${banco.nombre} (${TEXTO_MONEDA[c.moneda]}) quedó registrada ${TEXTO_FLUJO[c.flujo].frase}.`);
    cargarCuentas();
  } catch (e) {
    mostrarError("cta-error", e.message);
  }
}

document.querySelectorAll("[data-agregar]").forEach((b) => b.addEventListener("click", () => abrirFormulario(b.dataset.agregar)));
$("form-cuenta").addEventListener("submit", guardarCuenta);
$("dlg-cuenta-cerrar").addEventListener("click", () => $("dlg-cuenta").close());
$("dlg-cuenta-cancelar").addEventListener("click", () => $("dlg-cuenta").close());

if (usuario) {
  cargarBancos();
  cargarCuentas();
}
