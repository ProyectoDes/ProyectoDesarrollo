(function () {
  // El id del cliente llega en la dirección: confirmar-baja-cliente.html?id=5
  var idCliente = new URLSearchParams(window.location.search).get('id');

  var formulario = document.getElementById('form-baja-cliente');
  var boton = document.getElementById('btn-confirmar-baja-cliente');
  var texto = document.getElementById('texto-baja');
  var cancelar = document.getElementById('btn-cancelar');

  var aviso = document.createElement('p');
  aviso.className = 'error';
  aviso.setAttribute('role', 'alert');
  aviso.hidden = true;
  formulario.insertBefore(aviso, boton);

  function mostrarError(mensaje) {
    aviso.textContent = mensaje;
    aviso.hidden = false;
  }

  function leerJson(url) {
    return fetch(url).then(function (respuesta) {
      if (!respuesta.ok) {
        throw new Error('Error ' + respuesta.status);
      }
      return respuesta.json();
    });
  }

  // Explica qué se va a borrar, con el nombre y cuántas citas tiene
  function pintarCliente(cliente, totalCitas) {
    texto.textContent = '';
    var nombre = document.createElement('strong');
    nombre.textContent = cliente.nombre;
    texto.appendChild(nombre);
    var detalle = totalCitas === 0
      ? ' dejará de aparecer en tus clientes.'
      : ' dejará de aparecer en tus clientes y se borrarán también sus '
        + (totalCitas === 1 ? 'cita' : totalCitas + ' citas') + ' y su historia clínica.';
    texto.appendChild(document.createTextNode(detalle + ' Esta acción no se puede deshacer.'));
  }

  if (!idCliente) {
    texto.textContent = 'No se indicó qué cliente dar de baja.';
    boton.disabled = true;
    return;
  }

  cancelar.href = 'perfil-cliente.html?id=' + encodeURIComponent(idCliente);

  Promise.all([leerJson('/api/clientes/lista'), leerJson('/api/citas')])
    .then(function (respuestas) {
      var cliente = respuestas[0].find(function (c) { return String(c.id) === idCliente; });
      if (!cliente) {
        texto.textContent = 'Este cliente ya no existe.';
        boton.disabled = true;
        return;
      }
      var totalCitas = respuestas[1].filter(function (c) {
        return String(c.idCliente) === idCliente;
      }).length;
      pintarCliente(cliente, totalCitas);
    })
    .catch(function () {
      texto.textContent = 'No se pudieron cargar los datos del cliente.';
    });

  formulario.addEventListener('submit', function (evento) {
    evento.preventDefault();
    aviso.hidden = true;
    boton.disabled = true;

    fetch('/api/clientes/baja/' + encodeURIComponent(idCliente), { method: 'DELETE' })
      .then(function (respuesta) {
        return respuesta.json().then(function (cuerpo) {
          return { ok: respuesta.ok, cuerpo: cuerpo };
        });
      })
      .then(function (resultado) {
        if (resultado.ok) {
          window.location.href = 'clientes.html';
        } else {
          mostrarError(resultado.cuerpo.error || 'No se pudo dar de baja al cliente.');
          boton.disabled = false;
        }
      })
      .catch(function () {
        mostrarError('No se pudo conectar con el servidor.');
        boton.disabled = false;
      });
  });
})();
