(function () {
  var MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun',
               'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

  var lista = document.getElementById('lista-citas-hoy');

  function fechaDeHoy() {
    var hoy = new Date();
    return hoy.getFullYear() + '-'
      + String(hoy.getMonth() + 1).padStart(2, '0') + '-'
      + String(hoy.getDate()).padStart(2, '0');
  }

  // Convierte "2026-11-19" en "19 nov 2026"
  function formatearFecha(iso) {
    var partes = iso.split('-');
    return Number(partes[2]) + ' ' + MESES[Number(partes[1]) - 1] + ' ' + partes[0];
  }

  function crearElemento(etiqueta, texto) {
    var elemento = document.createElement(etiqueta);
    elemento.textContent = texto;
    return elemento;
  }

  function crearTarjeta(cita) {
    var item = document.createElement('li');
    var enlace = document.createElement('a');
    enlace.href = 'editar-cita.html?id=' + cita.id;
    enlace.appendChild(crearElemento('span', cita.horaInicio + ' – ' + cita.horaFin));
    enlace.appendChild(crearElemento('span', formatearFecha(cita.fecha)));
    enlace.appendChild(crearElemento('strong', cita.cliente));
    enlace.appendChild(crearElemento('span', cita.servicio));
    item.appendChild(enlace);
    return item;
  }

  function mostrarMensaje(texto) {
    lista.innerHTML = '';
    var item = crearElemento('li', texto);
    item.className = 'vacio';
    lista.appendChild(item);
  }

  function cargarCitasDeHoy() {
    fetch('/api/citas')
      .then(function (respuesta) {
        if (!respuesta.ok) {
          throw new Error('Error ' + respuesta.status);
        }
        return respuesta.json();
      })
      .then(function (citas) {
        var hoy = fechaDeHoy();
        var deHoy = citas
          .filter(function (cita) { return cita.fecha === hoy; })
          .sort(function (a, b) { return a.horaInicio.localeCompare(b.horaInicio); });

        if (deHoy.length === 0) {
          mostrarMensaje('No hay citas para hoy.');
          return;
        }
        lista.innerHTML = '';
        deHoy.forEach(function (cita) {
          lista.appendChild(crearTarjeta(cita));
        });
      })
      .catch(function () {
        mostrarMensaje('No se pudieron cargar las citas de hoy.');
      });
  }

  cargarCitasDeHoy();
})();
