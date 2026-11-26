(function () {
  var formulario = document.getElementById('form-registrar-cita');
  var selectCliente = document.getElementById('cliente');
  var botonGuardar = document.getElementById('btn-guardar');

  var aviso = document.createElement('p');
  aviso.className = 'error';
  aviso.setAttribute('role', 'alert');
  aviso.hidden = true;
  formulario.insertBefore(aviso, botonGuardar);

  var avisoHorario = document.getElementById('aviso-horario-ocupado');

  function mostrarError(texto) {
    aviso.textContent = texto;
    aviso.hidden = false;
  }

  function cargarClientes() {
    fetch('/api/clientes')
      .then(function (respuesta) { return respuesta.json(); })
      .then(function (clientes) {
        selectCliente.innerHTML = '<option value="">Selecciona un cliente</option>';
        clientes.forEach(function (cliente) {
          var opcion = document.createElement('option');
          opcion.value = cliente.id;
          opcion.textContent = cliente.nombre;
          selectCliente.appendChild(opcion);
        });
      })
      .catch(function () {
        mostrarError('No se pudo cargar la lista de clientes.');
      });
  }

  formulario.addEventListener('submit', function (evento) {
    evento.preventDefault();
    aviso.hidden = true;
    avisoHorario.hidden = true;

    var datos = new URLSearchParams();
    datos.set('id_cliente', selectCliente.value);
    datos.set('fecha', document.getElementById('fecha').value);
    datos.set('hora_inicio', document.getElementById('hora-inicio').value);
    datos.set('hora_fin', document.getElementById('hora-fin').value);
    datos.set('servicio', document.getElementById('servicio').value);
    datos.set('servicio_personalizado',
      document.getElementById('servicio-personalizado').value);

    fetch('/api/citas', {
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
          window.location.href = 'cita-guardada.html';
        } else {
          if (resultado.cuerpo.campo === 'horario') {
            avisoHorario.hidden = false;
          } else {
            mostrarError(resultado.cuerpo.error || 'No se pudo guardar la cita.');
          }
        }
      })
      .catch(function () {
        mostrarError('No se pudo conectar con el servidor.');
      });
  });

  cargarClientes();
})();
