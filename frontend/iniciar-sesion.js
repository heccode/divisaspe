if (usuarioGuardado()) location.replace("/");
montarMenu("/iniciar-sesion");

async function iniciarSesion(ev) {
  ev.preventDefault();
  limpiar("login-error");
  const email = $("login-email").value.trim();
  const password = $("login-password").value;
  if (!email || !$("login-email").checkValidity()) return mostrarError("login-error", "Revisa el correo, no parece válido.");
  if (!password) return mostrarError("login-error", "Escribe tu contraseña para continuar.");
  try {
    const r = await conCarga(botonEnvio(ev), () => postJson("/api/v1/auth/login", { email, password }));
    guardarSesion(r.usuario, r.token);
    dejarAviso(`Hola de nuevo, ${r.usuario.nombre}.`);
    location.assign("/");
  } catch (e) {
    mostrarError("login-error", e.message);
  }
}

$("form-login").addEventListener("submit", iniciarSesion);
