(function () {
  var MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun',
               'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

  var formulario = document.getElementById('form-eliminar-cita');
  var campoId = document.getElementById('id-cita');
  var botonEliminar = document.getElementById('btn-confirmar-eliminar');
  var texto = document.getElementById('texto-eliminar');
  var resumen = document.getElementById('resumen-cita');

  // Espacio para mostrar errores encima del boton Eliminar
  var aviso = document.createElement('p');
  aviso.className = 'error';
  aviso.setAttribute('role', 'alert');
  aviso.hidden = true;
  formulario.insertBefore(aviso, botonEliminar);

  function mostrarError(mensaje) {
    aviso.textContent = mensaje;
    aviso.hidden = false;
  }

  function crearElemento(etiqueta, contenido) {
    var elemento = document.createElement(etiqueta);
    elemento.textContent = contenido;
    return elemento;
  }

  function formatearFecha(iso) {
    var partes = iso.split('-');
    return Number(partes[2]) + ' ' + MESES[Number(partes[1]) - 1] + ' ' + partes[0];
  }

  // El id de la cita llega en la direccion: confirmar-eliminar-cita.html?id=5
  var idCita = new URLSearchParams(window.location.search).get('id');
  campoId.value = idCita || '';

  function pintarCita(cita) {
    texto.textContent = '';
    texto.appendChild(document.createTextNode('Se eliminará la cita de '));
    texto.appendChild(crearElemento('strong', cita.cliente));
    texto.appendChild(document.createTextNode('. Esta acción no se puede deshacer.'));

    resumen.textContent = '';
    resumen.appendChild(crearElemento('strong', cita.servicio));
    var linea = document.createElement('p');
    var tiempo = crearElemento('time', formatearFecha(cita.fecha));
    tiempo.setAttribute('datetime', cita.fecha);
    linea.appendChild(tiempo);
    linea.appendChild(document.createTextNode(
      ' · ' + cita.horaInicio + ' – ' + cita.horaFin));
    resumen.appendChild(linea);
  }

  function cargarCita() {
    if (!idCita) {
      mostrarError('No se indicó qué cita eliminar.');
      botonEliminar.disabled = true;
      return;
    }
    fetch('/api/citas')
      .then(function (respuesta) { return respuesta.json(); })
      .then(function (citas) {
        var cita = citas.find(function (c) { return String(c.id) === idCita; });
        if (!cita) {
          mostrarError('La cita ya no existe.');
          botonEliminar.disabled = true;
          return;
        }
        pintarCita(cita);
      })
      .catch(function () {
        mostrarError('No se pudo cargar la cita.');
      });
  }

  formulario.addEventListener('submit', function (evento) {
    evento.preventDefault();
    aviso.hidden = true;
    botonEliminar.disabled = true;

    fetch('/api/citas/' + encodeURIComponent(campoId.value), { method: 'DELETE' })
      .then(function (respuesta) {
        return respuesta.json().then(function (cuerpo) {
          return { ok: respuesta.ok, cuerpo: cuerpo };
        });
      })
      .then(function (resultado) {
        if (resultado.ok) {
          window.location.href = 'citas.html';
        } else {
          mostrarError(resultado.cuerpo.error || 'No se pudo eliminar la cita.');
          botonEliminar.disabled = false;
        }
      })
      .catch(function () {
        mostrarError('No se pudo conectar con el servidor.');
        botonEliminar.disabled = false;
      });
  });

  cargarCita();
})();