const usuario = protegerPagina();
montarMenu("/perfil");

function pintarDatos(u) {
  $("dat-nombre").value = u.nombre ?? "";
  $("dat-apellidos").value = u.apellidos ?? "";
  $("dat-tipo").value = u.tipoDocumento ?? "";
  $("dat-numero").value = u.numeroDocumento ?? "";
  $("dat-ruc").value = u.ruc ?? "";
  $("dat-email").value = u.email;
}

async function cargarDatos() {
  pintarDatos(usuario);
  try {
    const u = await api("/api/v1/auth/me");
    guardarSesion(u);
    pintarDatos(u);
    montarMenu("/perfil");
  } catch (e) {
    mostrarError("dat-error", e.message);
  }
}

const FORMATO_DOCUMENTO = {
  DNI: [/^\d{8}$/, "El DNI debe tener 8 dígitos."],
  CE: [/^\d{9,12}$/, "El carné de extranjería debe tener entre 9 y 12 dígitos."],
  PASAPORTE: [/^[A-Z0-9]{6,12}$/, "El pasaporte debe tener entre 6 y 12 letras o números."],
};

async function guardarDatos(ev) {
  ev.preventDefault();
  limpiar("dat-error");
  const nombre = $("dat-nombre").value.trim();
  const apellidos = $("dat-apellidos").value.trim();
  const tipoDocumento = $("dat-tipo").value;
  const numeroDocumento = $("dat-numero").value.trim().toUpperCase();
  const ruc = $("dat-ruc").value.trim();
  if (!nombre) return mostrarError("dat-error", "Escribe tu nombre para continuar.");
  if (tipoDocumento && !numeroDocumento) return mostrarError("dat-error", "Escribe el número de tu documento.");
  if (!tipoDocumento && numeroDocumento) return mostrarError("dat-error", "Elige el tipo de documento.");
  if (tipoDocumento && !FORMATO_DOCUMENTO[tipoDocumento][0].test(numeroDocumento)) {
    return mostrarError("dat-error", FORMATO_DOCUMENTO[tipoDocumento][1]);
  }
  if (ruc && !/^\d{11}$/.test(ruc)) return mostrarError("dat-error", "El RUC debe tener 11 dígitos, solo números.");

  const cuerpo = { nombre, email: $("dat-email").value };
  if (apellidos) cuerpo.apellidos = apellidos;
  if (tipoDocumento) { cuerpo.tipoDocumento = tipoDocumento; cuerpo.numeroDocumento = numeroDocumento; }
  if (ruc) cuerpo.ruc = ruc;
  try {
    const u = await conCarga(botonEnvio(ev), () => putJson(`/api/v1/usuarios/${usuario.id}`, cuerpo));
    guardarSesion(u);
    pintarDatos(u);
    montarMenu("/perfil");
    mostrarExito("Datos actualizados", "Tu información personal quedó guardada.");
  } catch (e) {
    mostrarError("dat-error", e.message);
  }
}

async function cambiarPassword(ev) {
  ev.preventDefault();
  limpiar("pass-error");
  const passwordActual = $("pass-actual").value;
  const passwordNueva = $("pass-nueva").value;
  if (!passwordActual) return mostrarError("pass-error", "Escribe tu contraseña actual.");
  if (passwordNueva.length < 6 || passwordNueva.length > 72) {
    return mostrarError("pass-error", "La contraseña nueva debe tener al menos 6 caracteres.");
  }
  if (passwordNueva !== $("pass-repite").value) return mostrarError("pass-error", "Las contraseñas nuevas no coinciden.");
  if (passwordNueva === passwordActual) return mostrarError("pass-error", "La contraseña nueva debe ser distinta de la actual.");
  try {
    await conCarga(botonEnvio(ev), () => putJson("/api/v1/auth/password", { passwordActual, passwordNueva }));
    $("form-pass").reset();
    mostrarExito("Contraseña actualizada", "Usa tu nueva contraseña la próxima vez que inicies sesión.");
  } catch (e) {
    mostrarError("pass-error", e.message);
  }
}

$("form-datos").addEventListener("submit", guardarDatos);
$("form-pass").addEventListener("submit", cambiarPassword);
if (usuario) cargarDatos();
