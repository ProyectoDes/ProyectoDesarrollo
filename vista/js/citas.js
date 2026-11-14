(function () {
  var DIAS = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];
  var MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun',
               'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

  var inputFecha = document.getElementById('fecha-agenda');
  var etiquetaFecha = document.querySelector('label[for="fecha-agenda"]');
  var total = document.getElementById('total-citas');
  var lista = document.getElementById('lista-citas');
  var todasLasCitas = [];

  // Convierte "2026-11-02" en { texto: "2 nov 2026", dia: "Lun" }
  function formatearFecha(iso) {
    var partes = iso.split('-');
    var fecha = new Date(Number(partes[0]), Number(partes[1]) - 1, Number(partes[2]));
    return {
      texto: fecha.getDate() + ' ' + MESES[fecha.getMonth()] + ' ' + fecha.getFullYear(),
      dia: DIAS[fecha.getDay()]
    };
  }

  function crearElemento(etiqueta, texto) {
    var elemento = document.createElement(etiqueta);
    elemento.textContent = texto;
    return elemento;
  }

  function crearTarjeta(cita) {
    var fecha = formatearFecha(cita.fecha);
    var item = document.createElement('li');

    var linea = document.createElement('p');
    linea.appendChild(crearElemento('time', cita.horaInicio + ' – ' + cita.horaFin));
    linea.appendChild(document.createTextNode(' '));
    var tiempoFecha = crearElemento('time', fecha.texto);
    tiempoFecha.setAttribute('datetime', cita.fecha);
    linea.appendChild(tiempoFecha);
    item.appendChild(linea);

    item.appendChild(crearElemento('strong', cita.cliente));
    item.appendChild(crearElemento('span', cita.servicio));

    var editar = crearElemento('a', 'Editar');
    editar.href = 'editar-cita.html?id=' + cita.id;
    item.appendChild(editar);

    var eliminar = crearElemento('a', 'Eliminar');
    eliminar.href = 'confirmar-eliminar-cita.html?id=' + cita.id;
    item.appendChild(eliminar);

    return item;
  }

  function mostrarCitas() {
    var fechaElegida = inputFecha.value;
    var delDia = todasLasCitas.filter(function (cita) {
      return cita.fecha === fechaElegida;
    });

    if (fechaElegida) {
      var f = formatearFecha(fechaElegida);
      etiquetaFecha.textContent = f.dia + ', ' + f.texto;
    }

    total.textContent = delDia.length + (delDia.length === 1 ? ' cita' : ' citas');
    lista.innerHTML = '';

    if (delDia.length === 0) {
      lista.appendChild(crearElemento('li', 'No hay citas para esta fecha.'));
      return;
    }
    delDia.forEach(function (cita) {
      lista.appendChild(crearTarjeta(cita));
    });
  }

  function cargarCitas() {
    fetch('/api/citas')
      .then(function (respuesta) { return respuesta.json(); })
      .then(function (citas) {
        todasLasCitas = citas;
        mostrarCitas();
      })
      .catch(function () {
        total.textContent = 'No se pudieron cargar las citas.';
        lista.innerHTML = '';
      });
  }

  inputFecha.addEventListener('change', mostrarCitas);
  cargarCitas();
})();