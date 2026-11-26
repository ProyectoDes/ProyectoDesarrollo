// Reporte de citas por periodo: cuenta las citas registradas entre dos fechas
// y las agrupa por servicio.
(function () {
  var MESES = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio',
               'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];

  var formulario = document.getElementById('form-periodo');
  var inputDesde = document.getElementById('desde');
  var inputHasta = document.getElementById('hasta');
  var total = document.getElementById('total-reporte');
  var periodo = document.getElementById('periodo-reporte');
  var tabla = document.getElementById('tabla-servicios');
  var nota = document.getElementById('nota-reporte');
  var citas = [];

  function dos(numero) {
    return numero < 10 ? '0' + numero : String(numero);
  }

  function iso(fecha) {
    return fecha.getFullYear() + '-' + dos(fecha.getMonth() + 1) + '-' + dos(fecha.getDate());
  }

  // "2026-11-02" -> "2 de noviembre de 2026"
  function textoFecha(valor) {
    var partes = valor.split('-');
    return Number(partes[2]) + ' de ' + MESES[Number(partes[1]) - 1] + ' de ' + partes[0];
  }

  function fila(servicio, cantidad) {
    var tr = document.createElement('tr');
    var th = document.createElement('th');
    th.scope = 'row';
    th.textContent = servicio;
    var td = document.createElement('td');
    td.textContent = String(cantidad);
    tr.appendChild(th);
    tr.appendChild(td);
    return tr;
  }

  function mostrar() {
    var desde = inputDesde.value;
    var hasta = inputHasta.value;
    tabla.innerHTML = '';

    if (!desde || !hasta) {
      total.textContent = 'Elige las fechas';
      periodo.textContent = '';
      return;
    }
    if (desde > hasta) {
      total.textContent = 'Revisa las fechas';
      periodo.textContent = 'La fecha "Desde" no puede ser posterior a "Hasta".';
      return;
    }

    // Las fechas vienen como "AAAA-MM-DD", así que se pueden comparar como texto
    var enRango = citas.filter(function (cita) {
      return cita.fecha >= desde && cita.fecha <= hasta;
    });

    total.textContent = enRango.length === 1 ? '1 cita' : enRango.length + ' citas';
    periodo.textContent = desde === hasta
      ? textoFecha(desde)
      : 'Del ' + textoFecha(desde) + ' al ' + textoFecha(hasta);

    var conteo = {};
    enRango.forEach(function (cita) {
      var servicio = cita.servicio || 'Sin servicio';
      conteo[servicio] = (conteo[servicio] || 0) + 1;
    });

    var servicios = Object.keys(conteo).sort(function (a, b) {
      return conteo[b] - conteo[a] || a.localeCompare(b, 'es');
    });
    servicios.forEach(function (servicio) {
      tabla.appendChild(fila(servicio, conteo[servicio]));
    });

    nota.textContent = servicios.length === 0
      ? 'No hay citas registradas en el rango seleccionado.'
      : 'Incluye todas las citas registradas en el rango seleccionado.';
  }

  // Por defecto muestra el mes actual
  var hoy = new Date();
  inputDesde.value = iso(new Date(hoy.getFullYear(), hoy.getMonth(), 1));
  inputHasta.value = iso(new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0));

  inputDesde.addEventListener('change', mostrar);
  inputHasta.addEventListener('change', mostrar);
  formulario.addEventListener('submit', function (evento) {
    evento.preventDefault();
    mostrar();
  });

  fetch('/api/citas')
    .then(function (respuesta) {
      if (!respuesta.ok) {
        throw new Error('sin citas');
      }
      return respuesta.json();
    })
    .then(function (datos) {
      citas = datos;
      mostrar();
    })
    .catch(function () {
      total.textContent = 'No se pudo cargar el reporte';
      periodo.textContent = 'Revisa tu conexión e intenta de nuevo.';
    });
})();
