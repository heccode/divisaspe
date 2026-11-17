if (usuarioGuardado()) location.replace("/");
montarMenu("/registro");

async function registrarUsuario(ev) {
  ev.preventDefault();
  limpiar("reg-error");
  const nombre = $("reg-nombre").value.trim();
  const email = $("reg-email").value.trim();
  const password = $("reg-password").value;
  const ruc = $("reg-ruc").value.trim();
  if (!nombre) return mostrarError("reg-error", "Escribe tu nombre para continuar.");
  if (!email || !$("reg-email").checkValidity()) return mostrarError("reg-error", "Revisa el correo, no parece válido.");
  if (password.length < 6 || password.length > 72) {
    return mostrarError("reg-error", "La contraseña debe tener al menos 6 caracteres.");
  }
  if (password !== $("reg-password2").value) return mostrarError("reg-error", "Las contraseñas no coinciden.");
  if (ruc && !/^\d{11}$/.test(ruc)) return mostrarError("reg-error", "El RUC debe tener 11 dígitos, solo números.");

  const cuerpo = { nombre, email, password };
  if (ruc) cuerpo.ruc = ruc;
  try {
    const r = await conCarga(botonEnvio(ev), () => postJson("/api/v1/auth/registro", cuerpo));
    guardarSesion(r.usuario, r.token);
    dejarAviso(`Cuenta creada. Ya puedes guardar tus conversiones, ${r.usuario.nombre}.`);
    location.assign("/");
  } catch (e) {
    mostrarError("reg-error", e.message);
  }
}

$("form-reg").addEventListener("submit", registrarUsuario);
