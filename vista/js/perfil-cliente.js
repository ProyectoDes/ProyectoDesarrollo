(function () {
  var MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun',
               'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

  // El id del cliente llega en la dirección: perfil-cliente.html?id=5
  var idCliente = new URLSearchParams(window.location.search).get('id');

  var nombre = document.getElementById('nombre-cliente');
  var iniciales = document.getElementById('iniciales-cliente');
  var telefono = document.getElementById('telefono-cliente');
  var edad = document.getElementById('edad-cliente');
  var botonEditar = document.getElementById('btn-editar-perfil');
  var botonHistoria = document.getElementById('btn-historia-clinica');
  var notaCitas = document.getElementById('nota-citas');
  var lista = document.getElementById('lista-citas-cliente');

  function sacarIniciales(texto) {
    var partes = String(texto || '').trim().split(/\s+/).filter(Boolean);
    var letras = partes.length > 1
      ? partes[0].charAt(0) + partes[partes.length - 1].charAt(0)
      : (partes[0] || '?').charAt(0);
    return letras.toUpperCase();
  }

  // "5512345678" se muestra como "55 1234 5678"
  function formatearTelefono(texto) {
    var digitos = String(texto || '').replace(/\D/g, '');
    if (digitos.length === 10) {
      return digitos.slice(0, 2) + ' ' + digitos.slice(2, 6) + ' ' + digitos.slice(6);
    }
    return texto || '';
  }

  function formatearFecha(iso) {
    var partes = iso.split('-');
    return Number(partes[2]) + ' ' + MESES[Number(partes[1]) - 1] + ' ' + partes[0];
  }

  function hoyIso() {
    var hoy = new Date();
    var mes = String(hoy.getMonth() + 1).padStart(2, '0');
    var dia = String(hoy.getDate()).padStart(2, '0');
    return hoy.getFullYear() + '-' + mes + '-' + dia;
  }

  function crearElemento(etiqueta, texto, clase) {
    var elemento = document.createElement(etiqueta);
    elemento.textContent = texto;
    if (clase) {
      elemento.className = clase;
    }
    return elemento;
  }

  function crearTarjetaCita(cita) {
    var item = document.createElement('li');
    var enlace = document.createElement('a');
    enlace.href = 'editar-cita.html?id=' + cita.id;
    enlace.className = 'tarjeta tarjeta-cita';

    var horario = document.createElement('span');
    horario.className = 'horario';
    horario.appendChild(crearElemento('time', cita.horaInicio + ' – ' + cita.horaFin));
    var fecha = crearElemento('time', formatearFecha(cita.fecha));
    fecha.setAttribute('datetime', cita.fecha);
    horario.appendChild(fecha);
    enlace.appendChild(horario);

    enlace.appendChild(crearElemento('strong', cita.cliente));
    enlace.appendChild(crearElemento('span', cita.servicio, 'servicio'));

    item.appendChild(enlace);
    return item;
  }

  function mostrarMensaje(texto) {
    lista.innerHTML = '';
    var item = crearElemento('li', texto, 'tarjeta-vacia');
    lista.appendChild(item);
  }

  function mostrarCliente(cliente) {
    nombre.textContent = cliente.nombre;
    iniciales.textContent = sacarIniciales(cliente.nombre);
    telefono.textContent = formatearTelefono(cliente.telefono);
    edad.textContent = cliente.edad ? cliente.edad + ' años' : '';
    botonEditar.href = 'editar-cliente.html?id=' + cliente.id;
    botonHistoria.href = 'historia-clinica.html?id=' + cliente.id;
    document.title = cliente.nombre + ' - Bambú y Brillo';
  }

  // Primero las citas que vienen (de la más cercana a la más lejana) y luego las pasadas
  function mostrarCitas(citas) {
    var hoy = hoyIso();
    var proximas = citas.filter(function (c) { return c.fecha >= hoy; })
      .sort(function (a, b) { return (a.fecha + a.horaInicio).localeCompare(b.fecha + b.horaInicio); });
    var pasadas = citas.filter(function (c) { return c.fecha < hoy; })
      .sort(function (a, b) { return (b.fecha + b.horaInicio).localeCompare(a.fecha + a.horaInicio); });

    if (citas.length === 0) {
      notaCitas.textContent = '';
      mostrarMensaje('Este cliente aún no tiene citas.');
      return;
    }
    notaCitas.textContent = proximas.length ? 'Próxima cita' : 'Citas anteriores';
    lista.innerHTML = '';
    proximas.concat(pasadas).forEach(function (cita) {
      lista.appendChild(crearTarjetaCita(cita));
    });
  }

  function leerJson(url) {
    return fetch(url).then(function (respuesta) {
      if (!respuesta.ok) {
        throw new Error('Error ' + respuesta.status);
      }
      return respuesta.json();
    });
  }

  if (!idCliente) {
    nombre.textContent = 'Cliente no indicado';
    mostrarMensaje('Regresa a Clientes y elige uno.');
    return;
  }

  Promise.all([leerJson('/api/clientes/lista'), leerJson('/api/citas')])
    .then(function (respuestas) {
      var cliente = respuestas[0].find(function (c) { return String(c.id) === idCliente; });
      if (!cliente) {
        nombre.textContent = 'Cliente no encontrado';
        mostrarMensaje('Este cliente ya no existe.');
        return;
      }
      mostrarCliente(cliente);
      mostrarCitas(respuestas[1].filter(function (c) { return String(c.idCliente) === idCliente; }));
    })
    .catch(function () {
      nombre.textContent = 'No se pudo cargar';
      mostrarMensaje('No se pudo conectar con el servidor.');
    });
})();
