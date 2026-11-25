(function () {
  // El id del producto llega en la dirección: confirmar-baja-producto.html?id=5
  var idProducto = new URLSearchParams(window.location.search).get('id');

  var formulario = document.getElementById('form-baja-producto');
  var campoId = document.getElementById('id-producto');
  var boton = document.getElementById('btn-confirmar-baja');
  var texto = document.getElementById('texto-baja');
  var cancelar = document.getElementById('btn-cancelar');
  var aviso = document.getElementById('mensaje-baja');

  function mostrarError(mensaje) {
    aviso.textContent = mensaje;
    aviso.hidden = false;
  }

  function pintarProducto(producto) {
    texto.textContent = '';
    var nombre = document.createElement('strong');
    nombre.textContent = producto.nombre;
    texto.appendChild(nombre);
    texto.appendChild(document.createTextNode(
      ' dejará de aparecer en el inventario. Seguirá apareciendo en la historia clínica'
      + ' de las citas donde ya se usó.'));
  }

  if (!/^\d+$/.test(idProducto || '')) {
    texto.textContent = 'No se indicó qué producto dar de baja.';
    boton.disabled = true;
    return;
  }

  campoId.value = idProducto;
  // Cancelar regresa a la edición del mismo producto
  cancelar.href = 'editar-producto.html?id=' + encodeURIComponent(idProducto);

  fetch('/api/productos?id=' + encodeURIComponent(idProducto))
    .then(function (respuesta) {
      return respuesta.json().then(function (cuerpo) {
        return { ok: respuesta.ok, cuerpo: cuerpo };
      });
    })
    .then(function (resultado) {
      if (!resultado.ok) {
        texto.textContent = resultado.cuerpo.error || 'Este producto ya no existe.';
        boton.disabled = true;
        cancelar.href = 'inventario.html';
        return;
      }
      pintarProducto(resultado.cuerpo);
    })
    .catch(function () {
      texto.textContent = 'No se pudieron cargar los datos del producto.';
    });

  formulario.addEventListener('submit', function (evento) {
    evento.preventDefault();
    aviso.hidden = true;
    boton.disabled = true;

    fetch('/api/productos/' + encodeURIComponent(idProducto), { method: 'DELETE' })
      .then(function (respuesta) {
        return respuesta.json().then(function (cuerpo) {
          return { ok: respuesta.ok, cuerpo: cuerpo };
        });
      })
      .then(function (resultado) {
        if (resultado.ok) {
          window.location.href = 'inventario.html?baja=1';
        } else {
          // Si está en una cita que todavía no pasa, el botón se queda desactivado
          mostrarError(resultado.cuerpo.error || 'No se pudo dar de baja el producto.');
          boton.disabled = /todavía no pasa/.test(resultado.cuerpo.error || '');
        }
      })
      .catch(function () {
        mostrarError('No se pudo conectar con el servidor.');
        boton.disabled = false;
      });
  });
})();
