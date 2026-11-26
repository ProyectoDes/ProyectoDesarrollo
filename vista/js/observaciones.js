(function () {
  var MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun',
               'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

  // La cita llega en la dirección: observaciones-productos.html?cita=8
  var idCita = new URLSearchParams(window.location.search).get('cita');

  var regresar = document.getElementById('btn-regresar');
  var subtitulo = document.getElementById('subtitulo');
  var origenCliente = document.getElementById('origen-cliente');
  var origenDetalle = document.getElementById('origen-detalle');
  var productosRegistrados = document.getElementById('productos-registrados');
  var observacionesRegistradas = document.getElementById('observaciones-registradas');
  var listaProductos = document.getElementById('lista-productos-check');
  var formulario = document.getElementById('form-observaciones');
  var campoObservaciones = document.getElementById('observaciones');
  var mensaje = document.getElementById('mensaje-formulario');
  var botonGuardar = document.getElementById('btn-guardar');
  var urlHistoria = 'clientes.html';

  function formatearFecha(iso) {
    var partes = iso.split('-');
    return Number(partes[2]) + ' ' + MESES[Number(partes[1]) - 1] + ' ' + partes[0];
  }

  function mostrarError(texto) {
    mensaje.textContent = texto;
    mensaje.hidden = false;
  }

  function leerJson(url) {
    return fetch(url).then(function (respuesta) {
      return respuesta.json().then(function (cuerpo) {
        if (!respuesta.ok) {
          throw new Error(cuerpo.error || 'Error ' + respuesta.status);
        }
        return cuerpo;
      });
    });
  }

  function pintarRegistro(registro) {
    urlHistoria = 'historia-clinica.html?id=' + registro.idCliente;
    regresar.href = urlHistoria;
    subtitulo.textContent = 'Historia clínica · ' + registro.cliente;
    origenCliente.textContent = registro.cliente;
    origenDetalle.textContent = formatearFecha(registro.fecha) + ' · ' + registro.servicio;

    var nombres = registro.productos.map(function (p) { return p.nombre; });
    productosRegistrados.textContent = nombres.length ? nombres.join(' · ') : 'Ninguno todavía.';
    observacionesRegistradas.textContent = registro.observaciones || 'Ninguna todavía.';
  }

  // Una casilla por producto; los que ya se usaron en la cita salen marcados y bloqueados
  function pintarProductos(productos, yaUsados) {
    listaProductos.innerHTML = '';
    if (productos.length === 0) {
      var vacio = document.createElement('li');
      vacio.className = 'sin-datos';
      vacio.textContent = 'Aún no hay productos en el inventario.';
      listaProductos.appendChild(vacio);
      return;
    }
    productos.forEach(function (producto) {
      var item = document.createElement('li');
      var etiqueta = document.createElement('label');
      var casilla = document.createElement('input');
      casilla.type = 'checkbox';
      casilla.name = 'productos';
      casilla.value = producto.id;
      if (yaUsados.indexOf(producto.id) !== -1) {
        casilla.checked = true;
        casilla.disabled = true;
      }
      etiqueta.appendChild(casilla);

      var nombre = document.createElement('span');
      nombre.textContent = producto.nombre;
      etiqueta.appendChild(nombre);

      var detalle = document.createElement('small');
      detalle.textContent = casilla.disabled ? 'Ya registrado' : producto.cantidad + ' disponibles';
      etiqueta.appendChild(detalle);

      item.appendChild(etiqueta);
      listaProductos.appendChild(item);
    });
  }

  if (!idCita) {
    origenCliente.textContent = 'No se indicó la cita.';
    botonGuardar.disabled = true;
    return;
  }

  Promise.all([
    leerJson('/api/historia/cita?id=' + encodeURIComponent(idCita)),
    leerJson('/api/productos')
  ])
    .then(function (respuestas) {
      var registro = respuestas[0];
      pintarRegistro(registro);
      pintarProductos(respuestas[1], registro.productos.map(function (p) { return p.id; }));
    })
    .catch(function (error) {
      origenCliente.textContent = error.message || 'No se pudo cargar la cita.';
      listaProductos.innerHTML = '';
      botonGuardar.disabled = true;
    });

  formulario.addEventListener('submit', function (evento) {
    evento.preventDefault();
    mensaje.hidden = true;

    var datos = new URLSearchParams();
    datos.set('id_cita', idCita);
    datos.set('observaciones', campoObservaciones.value);
    listaProductos.querySelectorAll('input[name="productos"]:checked:not(:disabled)')
      .forEach(function (casilla) {
        datos.append('productos', casilla.value);
      });

    botonGuardar.disabled = true;
    fetch('/api/historia/cita', {
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
          window.location.href = urlHistoria;
        } else {
          mostrarError(resultado.cuerpo.error || 'No se pudieron guardar los cambios.');
          botonGuardar.disabled = false;
        }
      })
      .catch(function () {
        mostrarError('No se pudo conectar con el servidor.');
        botonGuardar.disabled = false;
      });
  });
})();
