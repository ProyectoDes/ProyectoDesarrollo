(function () {
  var MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun',
               'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

  // El id del cliente llega en la dirección: historia-clinica.html?id=5
  var idCliente = new URLSearchParams(window.location.search).get('id');

  var regresar = document.getElementById('btn-regresar');
  var resumen = document.getElementById('resumen-historia');
  var lista = document.getElementById('lista-registros');

  function formatearFecha(iso) {
    var partes = iso.split('-');
    return Number(partes[2]) + ' ' + MESES[Number(partes[1]) - 1] + ' ' + partes[0];
  }

  function crearElemento(etiqueta, texto, clase) {
    var elemento = document.createElement(etiqueta);
    elemento.textContent = texto;
    if (clase) {
      elemento.className = clase;
    }
    return elemento;
  }

  function mostrarMensaje(texto) {
    lista.innerHTML = '';
    lista.appendChild(crearElemento('li', texto, 'tarjeta-vacia'));
  }

  function crearRegistro(registro) {
    var item = document.createElement('li');

    var fecha = crearElemento('time', formatearFecha(registro.fecha));
    fecha.setAttribute('datetime', registro.fecha);
    item.appendChild(fecha);

    var tarjeta = document.createElement('article');
    tarjeta.appendChild(crearElemento('h2', registro.servicio));

    tarjeta.appendChild(crearElemento('h3', 'Productos utilizados'));
    var nombres = registro.productos.map(function (p) { return p.nombre; });
    tarjeta.appendChild(nombres.length
      ? crearElemento('p', nombres.join(' · '))
      : crearElemento('p', 'Sin productos registrados.', 'sin-datos'));

    tarjeta.appendChild(crearElemento('h3', 'Observaciones'));
    tarjeta.appendChild(registro.observaciones
      ? crearElemento('p', registro.observaciones, 'observaciones')
      : crearElemento('p', 'Sin observaciones.', 'sin-datos'));

    var enlace = crearElemento('a', 'Añadir observaciones y productos');
    enlace.href = 'observaciones-productos.html?cita=' + registro.idCita;
    tarjeta.appendChild(enlace);

    item.appendChild(tarjeta);
    return item;
  }

  if (!idCliente) {
    resumen.textContent = '';
    mostrarMensaje('Regresa a Clientes y elige uno.');
    return;
  }

  regresar.href = 'perfil-cliente.html?id=' + encodeURIComponent(idCliente);

  Promise.all([
    fetch('/api/clientes/lista').then(function (r) { return r.json(); }),
    fetch('/api/historia?cliente=' + encodeURIComponent(idCliente)).then(function (r) {
      if (!r.ok) {
        throw new Error('Error ' + r.status);
      }
      return r.json();
    })
  ])
    .then(function (respuestas) {
      var cliente = respuestas[0].find(function (c) { return String(c.id) === idCliente; });
      var registros = respuestas[1];
      var nombre = cliente ? cliente.nombre : 'Cliente';
      resumen.textContent = nombre + ' · ' + registros.length
        + (registros.length === 1 ? ' registro' : ' registros');

      if (registros.length === 0) {
        mostrarMensaje('Aún no hay registros. Se crean al agendar una cita.');
        return;
      }
      lista.innerHTML = '';
      registros.forEach(function (registro) {
        lista.appendChild(crearRegistro(registro));
      });
    })
    .catch(function () {
      resumen.textContent = '';
      mostrarMensaje('No se pudo cargar la historia clínica.');
    });
})();
