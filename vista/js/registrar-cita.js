(function () {
  var formulario = document.getElementById('form-registrar-cita');
  var selectCliente = document.getElementById('cliente');
  var selectProductos = document.getElementById('productos');
  var ayudaProductos = document.getElementById('ayuda-productos');
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

  // Llena la lista con los productos que hay en el inventario
  function cargarProductos() {
    fetch('/api/productos')
      .then(function (respuesta) {
        if (!respuesta.ok) {
          throw new Error('sin productos');
        }
        return respuesta.json();
      })
      .then(function (productos) {
        selectProductos.innerHTML = '';
        productos.forEach(function (producto) {
          var opcion = document.createElement('option');
          opcion.value = producto.id;
          opcion.textContent = producto.nombre + ' (' + producto.cantidad + ')';
          selectProductos.appendChild(opcion);
        });
        if (productos.length === 0) {
          ayudaProductos.textContent = 'No hay productos en el inventario.';
        } else {
          ayudaProductos.textContent = 'Opcional. Puedes elegir varios; entre paréntesis está lo que hay en existencia.';
        }
      })
      .catch(function () {
        ayudaProductos.textContent = 'No se pudo cargar el inventario.';
      });
  }

  function productosElegidos() {
    var ids = [];
    for (var i = 0; i < selectProductos.options.length; i++) {
      if (selectProductos.options[i].selected) {
        ids.push(selectProductos.options[i].value);
      }
    }
    return ids.join(',');
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
    datos.set('productos', productosElegidos());

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
  cargarProductos();
})();
