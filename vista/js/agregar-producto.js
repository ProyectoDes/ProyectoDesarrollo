(function () {
  var formulario = document.getElementById('form-agregar-producto');
  var campoNombre = document.getElementById('nombre');
  var campoDescripcion = document.getElementById('descripcion');
  var campoCantidad = document.getElementById('cantidad');
  var botonGuardar = document.getElementById('btn-guardar');
  var mensaje = document.getElementById('mensaje-formulario');

  var errores = {
    nombre: document.getElementById('error-nombre'),
    descripcion: document.getElementById('error-descripcion'),
    cantidad: document.getElementById('error-cantidad')
  };

  function limpiarErrores() {
    Object.keys(errores).forEach(function (campo) {
      errores[campo].textContent = '';
      errores[campo].hidden = true;
    });
    mensaje.hidden = true;
    mensaje.className = '';
  }

  function mostrarMensaje(texto, esError) {
    mensaje.textContent = texto;
    mensaje.className = esError ? 'error' : '';
    mensaje.hidden = false;
  }

  function mostrarError(campo, texto) {
    if (campo && errores[campo]) {
      errores[campo].textContent = texto;
      errores[campo].hidden = false;
    } else {
      mostrarMensaje(texto, true);
    }
  }

  // Mismas reglas que en el servidor, para avisar antes de enviar
  function validar() {
    var valido = true;
    if (campoNombre.value.trim() === '') {
      mostrarError('nombre', 'El nombre del producto es obligatorio.');
      valido = false;
    }
    var cantidad = campoCantidad.value.trim();
    if (cantidad === '') {
      mostrarError('cantidad', 'La cantidad es obligatoria.');
      valido = false;
    } else if (!/^\d+$/.test(cantidad)) {
      mostrarError('cantidad', cantidad.charAt(0) === '-'
        ? 'La cantidad no puede ser negativa.'
        : 'La cantidad debe ser un número entero.');
      valido = false;
    }
    return valido;
  }

  // Al escribir de nuevo se quita el aviso de "registrado"
  formulario.addEventListener('input', function () {
    if (!mensaje.hidden && mensaje.className === '') {
      mensaje.hidden = true;
    }
  });

  formulario.addEventListener('submit', function (evento) {
    evento.preventDefault();
    limpiarErrores();
    if (!validar()) {
      return;
    }
    botonGuardar.disabled = true;

    var datos = new URLSearchParams();
    datos.set('nombre', campoNombre.value);
    datos.set('descripcion', campoDescripcion.value);
    datos.set('cantidad', campoCantidad.value.trim());

    fetch('/api/productos', {
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
          // Se queda en la pantalla para registrar otro producto
          formulario.reset();
          mostrarMensaje(resultado.cuerpo.mensaje || 'Producto registrado correctamente.', false);
          campoNombre.focus();
        } else {
          mostrarError(resultado.cuerpo.campo, resultado.cuerpo.error || 'No se pudo guardar el producto.');
        }
        botonGuardar.disabled = false;
      })
      .catch(function () {
        mostrarMensaje('No se pudo conectar con el servidor.', true);
        botonGuardar.disabled = false;
      });
  });
})();
