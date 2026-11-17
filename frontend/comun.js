const API_BASE = window.DIVISAPE_API;
const TIMEOUT_MS = 30000;
const CLAVE_SESION = "divisape.usuario";
const CLAVE_TOKEN = "divisape.token";
const CLAVE_AVISO = "divisape.aviso";

const $ = (id) => document.getElementById(id);

class ApiError extends Error {
  constructor(mensaje, status) {
    super(mensaje);
    this.status = status;
  }
}

const MENSAJES_ERROR = {
  BCRP_NO_DISPONIBLE: "No pudimos obtener el tipo de cambio en este momento. Inténtalo de nuevo en unos segundos.",
  CONVERSION_NO_DISPONIBLE: "El servicio de conversión no responde ahora mismo. Inténtalo de nuevo en un momento.",
  CREDENCIALES_INVALIDAS: "Correo o contraseña incorrectos.",
  NO_AUTENTICADO: "Tu sesión expiró. Inicia sesión de nuevo.",
  ACCESO_DENEGADO: "No tienes permiso para ver esto.",
  EMAIL_DUPLICADO: "Ya existe una cuenta con ese correo. Prueba iniciando sesión.",
  BASE_DATOS_NO_DISPONIBLE: "No pudimos acceder a los datos guardados. Inténtalo de nuevo en un momento.",
  ERROR_INTERNO: "Algo salió mal de nuestro lado. Inténtalo de nuevo.",
};

async function api(path, opciones) {
  let resp;
  const token = tokenGuardado();
  const cabeceras = { ...(opciones && opciones.headers), ...(token ? { Authorization: `Bearer ${token}` } : {}) };
  try {
    resp = await fetch(API_BASE + path, { ...opciones, headers: cabeceras, signal: AbortSignal.timeout(TIMEOUT_MS) });
  } catch (e) {
    if (e.name === "TimeoutError") {
      throw new ApiError("El servidor está tardando más de lo normal. Prueba de nuevo en unos segundos.");
    }
    throw new ApiError("No pudimos conectar con el servidor. Revisa que esté encendido.");
  }
  if (!resp.ok) {
    if (resp.status === 401 && token) {
      cerrarSesion();
      if (typeof alExpirarSesion === "function") alExpirarSesion();
    }
    let msg = `Algo salió mal (error ${resp.status}). Inténtalo de nuevo.`;
    try {
      const e = await resp.json();
      msg = MENSAJES_ERROR[e.codigo] || e.mensaje || msg;
    } catch {}
    throw new ApiError(msg, resp.status);
  }
  return resp.json();
}

function enviarJson(metodo, path, cuerpo) {
  return api(path, {
    method: metodo,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(cuerpo),
  });
}

const postJson = (path, cuerpo) => enviarJson("POST", path, cuerpo);
const putJson = (path, cuerpo) => enviarJson("PUT", path, cuerpo);

// JSON.parse descarta los ceros finales (340.10 pasa a 340.1); se fuerzan 2 decimales.
const dinero = (v) => Number(v).toFixed(2);

async function conCarga(botones, tarea) {
  const lista = [].concat(botones);
  const previos = lista.map((b) => b.disabled);
  lista.forEach((b) => { b.disabled = true; b.classList.add("cargando"); });
  try {
    return await tarea();
  } finally {
    lista.forEach((b, i) => { b.disabled = previos[i]; b.classList.remove("cargando"); });
  }
}

function mostrarError(id, texto) { const el = $(id); el.textContent = texto; el.hidden = false; }
function limpiar(...ids) { ids.forEach((id) => { $(id).hidden = true; }); }

let toastTimer;
function toast(texto, error = false) {
  const t = $("toast");
  t.textContent = texto;
  t.className = "app-toast" + (error ? " err" : "");
  t.hidden = false;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { t.hidden = true; }, 5000);
}

// ev.submitter no siempre esta disponible; se usa el boton de envio del formulario.
const botonEnvio = (ev) => ev.submitter || ev.target.querySelector('button[type="submit"]');

function el(tag, texto, clase) {
  const e = document.createElement(tag);
  if (texto !== undefined) e.textContent = texto;
  if (clase) e.className = clase;
  return e;
}

function usuarioGuardado() {
  try { return tokenGuardado() ? JSON.parse(localStorage.getItem(CLAVE_SESION)) : null; } catch { return null; }
}

function tokenGuardado() {
  try { return localStorage.getItem(CLAVE_TOKEN); } catch { return null; }
}

function guardarSesion(usuario, token) {
  try {
    localStorage.setItem(CLAVE_SESION, JSON.stringify(usuario));
    if (token) localStorage.setItem(CLAVE_TOKEN, token);
  } catch {}
}

function cerrarSesion() {
  try {
    localStorage.removeItem(CLAVE_SESION);
    localStorage.removeItem(CLAVE_TOKEN);
  } catch {}
}

function dejarAviso(texto) {
  try { sessionStorage.setItem(CLAVE_AVISO, texto); } catch {}
}

function tomarAviso() {
  try {
    const texto = sessionStorage.getItem(CLAVE_AVISO);
    sessionStorage.removeItem(CLAVE_AVISO);
    return texto;
  } catch { return null; }
}

const MENU_ANONIMO = [
  { href: "/", icono: "bi-arrow-left-right", texto: "Convertir" },
  null,
  { href: "/iniciar-sesion", icono: "bi-box-arrow-in-right", texto: "Iniciar sesión" },
  { href: "/registro", icono: "bi-person-plus", texto: "Crear cuenta" },
];

const MENU_AUTENTICADO = [
  { href: "/", icono: "bi-arrow-left-right", texto: "Convertir" },
  { href: "/historial", icono: "bi-clock-history", texto: "Mi historial" },
  { href: "/cuentas-bancarias", icono: "bi-bank", texto: "Cuentas bancarias" },
  { href: "/perfil", icono: "bi-person-vcard", texto: "Mis datos" },
  null,
  { accion: salirDeLaSesion, icono: "bi-box-arrow-right", texto: "Cerrar sesión", salir: true },
];

function salirDeLaSesion() {
  cerrarSesion();
  dejarAviso("Cerraste sesión.");
  location.assign("/");
}

function protegerPagina() {
  const u = usuarioGuardado();
  if (!u) location.replace("/iniciar-sesion");
  window.alExpirarSesion = () => {
    dejarAviso("Tu sesión expiró. Inicia sesión de nuevo.");
    location.replace("/iniciar-sesion");
  };
  return u;
}

// Menu desplegable de navegacion: las opciones dependen de si hay sesion iniciada.
function montarMenu(actual) {
  const contenedor = $("menu-principal");
  const usuario = usuarioGuardado();
  const opciones = usuario ? MENU_AUTENTICADO : MENU_ANONIMO;

  const boton = el("button", undefined, "nav-btn nav-btn-ghost");
  boton.type = "button";
  boton.setAttribute("aria-haspopup", "menu");
  boton.setAttribute("aria-expanded", "false");
  const icono = el("i", undefined, usuario ? "bi bi-person-circle" : "bi bi-list");
  const etiqueta = el("span", usuario ? usuario.nombre.split(" ")[0] : "Menú");
  boton.append(icono, etiqueta, el("i", undefined, "bi bi-chevron-down menu-flecha"));

  const panel = el("ul", undefined, "menu-panel");
  panel.setAttribute("role", "menu");
  panel.hidden = true;
  for (const o of opciones) {
    const li = el("li");
    li.setAttribute("role", "none");
    if (o === null) {
      li.className = "menu-sep";
      li.setAttribute("role", "separator");
    } else {
      const item = o.href ? el("a") : el("button");
      item.className = "menu-item" + (o.salir ? " menu-item-salir" : "");
      item.setAttribute("role", "menuitem");
      item.append(el("i", undefined, `bi ${o.icono}`), el("span", o.texto));
      if (o.href) {
        item.href = o.href;
        if (o.href === actual) item.setAttribute("aria-current", "page");
      } else {
        item.type = "button";
        item.addEventListener("click", o.accion);
      }
      li.append(item);
    }
    panel.append(li);
  }

  const items = () => [...panel.querySelectorAll(".menu-item")];
  const abrir = (abierto, enfocar) => {
    panel.hidden = !abierto;
    boton.setAttribute("aria-expanded", String(abierto));
    boton.classList.toggle("abierto", abierto);
    if (abierto && enfocar) items()[0].focus();
  };
  boton.addEventListener("click", () => abrir(panel.hidden));
  boton.addEventListener("keydown", (ev) => {
    if (ev.key === "ArrowDown") { ev.preventDefault(); abrir(true, true); }
  });
  panel.addEventListener("keydown", (ev) => {
    const lista = items(), i = lista.indexOf(document.activeElement);
    if (ev.key === "ArrowDown") { ev.preventDefault(); lista[(i + 1) % lista.length].focus(); }
    else if (ev.key === "ArrowUp") { ev.preventDefault(); lista[(i - 1 + lista.length) % lista.length].focus(); }
    else if (ev.key === "Escape") { abrir(false); boton.focus(); }
    else if (ev.key === "Tab") abrir(false);
  });
  document.addEventListener("click", (ev) => { if (!contenedor.contains(ev.target)) abrir(false); });

  contenedor.replaceChildren(boton, panel);
}

function mostrarExito(titulo, texto) {
  let dlg = $("dlg-exito");
  if (!dlg) {
    dlg = el("dialog", undefined, "modal-app modal-exito");
    dlg.id = "dlg-exito";
    dlg.setAttribute("aria-labelledby", "dlg-exito-titulo");
    const cuerpo = el("div", undefined, "modal-app-cuerpo text-center");
    const marca = el("span", undefined, "exito-icono");
    marca.append(el("i", undefined, "bi bi-check-lg"));
    const h = el("h2"); h.id = "dlg-exito-titulo";
    const p = el("p", undefined, "hint"); p.id = "dlg-exito-texto";
    const cerrar = el("button", "Entendido", "btn btn-primary mt-3");
    cerrar.type = "button";
    cerrar.addEventListener("click", () => dlg.close());
    cuerpo.append(marca, h, p, cerrar);
    dlg.append(cuerpo);
    document.body.append(dlg);
  }
  $("dlg-exito-titulo").textContent = titulo;
  $("dlg-exito-texto").textContent = texto;
  dlg.showModal();
}
