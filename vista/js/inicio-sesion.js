(function () {
  var formulario = document.getElementById('form-inicio-sesion');
  var campoUsuario = document.getElementById('usuario');
  var campoContrasena = document.getElementById('contrasena');
  var botonVer = document.getElementById('btn-ver-contrasena');
  var botonIngresar = document.getElementById('btn-ingresar');
  var mensaje = document.getElementById('mensaje-formulario');

  var errores = {
    usuario: document.getElementById('error-usuario'),
    contrasena: document.getElementById('error-contrasena')
  };

  // Si ya hay una sesión abierta, entra directo
  fetch('/api/sesion')
    .then(function (respuesta) {
      if (respuesta.ok) {
        window.location.replace('inicio.html');
      }
    })
    .catch(function () { /* sin conexión: se queda en el formulario */ });

  // Mostrar u ocultar la contraseña
  botonVer.addEventListener('click', function () {
    var oculta = campoContrasena.type === 'password';
    campoContrasena.type = oculta ? 'text' : 'password';
    botonVer.textContent = oculta ? 'Ocultar' : 'Ver';
    botonVer.setAttribute('aria-label', oculta ? 'Ocultar contraseña' : 'Mostrar contraseña');
  });

  function limpiarErrores() {
    Object.keys(errores).forEach(function (campo) {
      errores[campo].textContent = '';
      errores[campo].hidden = true;
    });
    mensaje.textContent = '';
    mensaje.hidden = true;
  }

  function mostrarError(campo, texto) {
    if (campo && errores[campo]) {
      errores[campo].textContent = texto;
      errores[campo].hidden = false;
    } else {
      mensaje.textContent = texto;
      mensaje.hidden = false;
    }
  }

  formulario.addEventListener('submit', function (evento) {
    evento.preventDefault();
    limpiarErrores();

    // Que no se envíe con campos vacíos
    var valido = true;
    if (campoUsuario.value.trim() === '') {
      mostrarError('usuario', 'Escribe tu usuario.');
      valido = false;
    }
    if (campoContrasena.value === '') {
      mostrarError('contrasena', 'Escribe tu contraseña.');
      valido = false;
    }
    if (!valido) {
      return;
    }

    botonIngresar.disabled = true;
    var datos = new URLSearchParams();
    datos.set('usuario', campoUsuario.value.trim());
    datos.set('contrasena', campoContrasena.value);

    fetch('/api/sesion', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: datos.toString()
    })
      .then(function (respuesta) {
        return respuesta.json().then(function (cuerpo) {
          return { ok: respuesta.ok, cuerpo: cuerpo };
        });
      })
      .then(function (resultado) {
        if (resultado.ok) {
          window.location.replace('inicio.html');
          return;
        }
        // Credenciales incorrectas: se borra la contraseña para volver a escribirla
        mostrarError(resultado.cuerpo.campo, resultado.cuerpo.error || 'No se pudo iniciar sesión.');
        campoContrasena.value = '';
        campoContrasena.focus();
        botonIngresar.disabled = false;
      })
      .catch(function () {
        mostrarError(null, 'No se pudo conectar con el servidor.');
        botonIngresar.disabled = false;
      });
  });
})();
