(function () {
  var MESES = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio',
               'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];
  var DIAS = ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];

  var formulario = document.getElementById('form-editar-cita');
  var campoId = document.getElementById('id-cita');
  var selectCliente = document.getElementById('cliente');
  var campoFecha = document.getElementById('fecha');
  var campoInicio = document.getElementById('hora-inicio');
  var campoFin = document.getElementById('hora-fin');
  var selectServicio = document.getElementById('servicio');
  var campoPersonalizado = document.getElementById('servicio-personalizado');
  var botonGuardar = document.getElementById('btn-guardar');
  var textoCliente = document.querySelector('main > p strong');
  var textoDetalle = document.querySelector('main > p small');
  var avisoHorario = document.getElementById('aviso-horario-ocupado');

  // Espacio para mostrar errores encima del boton Guardar
  var aviso = document.createElement('p');
  aviso.className = 'error';
  aviso.setAttribute('role', 'alert');
  aviso.hidden = true;
  formulario.insertBefore(aviso, botonGuardar);

  function mostrarError(mensaje) {
    aviso.textContent = mensaje;
    aviso.hidden = false;
  }

  // El id de la cita llega en la direccion: editar-cita.html?id=5
  var idCita = new URLSearchParams(window.location.search).get('id');
  campoId.value = idCita || '';

  function minutos(hora) {
    var partes = hora.split(':');
    return Number(partes[0]) * 60 + Number(partes[1]);
  }

  function describirDuracion(inicio, fin) {
    var total = minutos(fin) - minutos(inicio);
    var horas = Math.floor(total / 60);
    var resto = total % 60;
    var texto = [];
    if (horas > 0) texto.push(horas + (horas === 1 ? ' hora' : ' horas'));
    if (resto > 0) texto.push(resto + ' min');
    return texto.join(' ');
  }

  function describirFecha(iso) {
    var partes = iso.split('-');
    var fecha = new Date(Number(partes[0]), Number(partes[1]) - 1, Number(partes[2]));
    return DIAS[fecha.getDay()] + ' ' + fecha.getDate() + ' de ' + MESES[fecha.getMonth()];
  }

  function llenarClientes(clientes, idSeleccionado) {
    selectCliente.innerHTML = '<option value="">Selecciona un cliente</option>';
    clientes.forEach(function (cliente) {
      var opcion = document.createElement('option');
      opcion.value = cliente.id;
      opcion.textContent = cliente.nombre;
      if (cliente.id === idSeleccionado) {
        opcion.selected = true;
      }
      selectCliente.appendChild(opcion);
    });
  }

  function llenarServicio(servicio) {
    var existe = Array.prototype.some.call(selectServicio.options, function (opcion) {
      return opcion.value !== '' && opcion.value === servicio;
    });
    if (existe) {
      selectServicio.value = servicio;
      campoPersonalizado.value = '';
    } else {
      selectServicio.value = '';
      campoPersonalizado.value = servicio;
    }
  }

  function llenarFormulario(cita, clientes) {
    llenarClientes(clientes, cita.idCliente);
    campoFecha.value = cita.fecha;
    campoInicio.value = cita.horaInicio;
    campoFin.value = cita.horaFin;
    llenarServicio(cita.servicio);

    textoCliente.textContent = cita.cliente;
    var duracion = describirDuracion(cita.horaInicio, cita.horaFin);
    textoDetalle.textContent = (duracion ? 'Duración: ' + duracion + ' · ' : '')
      + describirFecha(cita.fecha);
  }

  function leerJson(url) {
    return fetch(url).then(function (respuesta) { return respuesta.json(); });
  }

  function cargarCita() {
    if (!idCita) {
      mostrarError('No se indicó qué cita editar.');
      botonGuardar.disabled = true;
      return;
    }
    Promise.all([leerJson('/api/citas'), leerJson('/api/clientes')])
      .then(function (respuestas) {
        var citas = respuestas[0];
        var clientes = respuestas[1];
        var cita = citas.find(function (c) { return String(c.id) === idCita; });
        if (!cita) {
          mostrarError('La cita ya no existe.');
          botonGuardar.disabled = true;
          return;
        }
        llenarFormulario(cita, clientes);
      })
      .catch(function () {
        mostrarError('No se pudo cargar la cita.');
      });
  }

  formulario.addEventListener('submit', function (evento) {
    evento.preventDefault();
    aviso.hidden = true;
    avisoHorario.hidden = true;
    botonGuardar.disabled = true;

    var datos = new URLSearchParams();
    datos.set('id_cliente', selectCliente.value);
    datos.set('fecha', campoFecha.value);
    datos.set('hora_inicio', campoInicio.value);
    datos.set('hora_fin', campoFin.value);
    datos.set('servicio', selectServicio.value);
    datos.set('servicio_personalizado', campoPersonalizado.value);

    fetch('/api/citas/' + encodeURIComponent(campoId.value), {
      method: 'PUT',
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
          window.location.href = 'citas.html?fecha=' + encodeURIComponent(campoFecha.value);
        } else {
          if (resultado.cuerpo.campo === 'horario') {
            avisoHorario.hidden = false;
          } else {
            mostrarError(resultado.cuerpo.error || 'No se pudo guardar la cita.');
          }
          botonGuardar.disabled = false;
        }
      })
      .catch(function () {
        mostrarError('No se pudo conectar con el servidor.');
        botonGuardar.disabled = false;
      });
  });

  cargarCita();
})();
