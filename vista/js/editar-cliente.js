(function () {
    // El id del cliente llega en la dirección: editar-cliente.html?id=5
    var idCliente = new URLSearchParams(window.location.search).get('id');

    var formulario = document.getElementById('form-editar-cliente');
    var mensaje = document.getElementById('mensaje-formulario');
    var botonGuardar = document.getElementById('btn-guardar');
    var botonRegresar = document.getElementById('btn-regresar');
    var nombre = document.getElementById('nombre');
    var edad = document.getElementById('edad');
    var telefono = document.getElementById('telefono');
    var CAMPOS = ['nombre', 'edad', 'telefono'];

    function limpiarMensajes() {
        mensaje.hidden = true;
        mensaje.textContent = '';
        mensaje.className = '';
        CAMPOS.forEach(function (campo) {
            var texto = document.getElementById('error-' + campo);
            if (texto) { texto.hidden = true; }
        });
    }

    function mostrarError(campo, textoError) {
        var texto = campo ? document.getElementById('error-' + campo) : null;
        if (texto) {
            texto.textContent = textoError;
            texto.hidden = false;
        } else {
            mensaje.className = 'error';
            mensaje.textContent = textoError;
            mensaje.hidden = false;
        }
    }

    function perfil() {
        return 'perfil-cliente.html?id=' + encodeURIComponent(idCliente);
    }

    if (!idCliente) {
        mostrarError(null, 'No se indicó el cliente. Regresa a Clientes y elige uno.');
        return;
    }
    botonRegresar.href = perfil();

    // Carga los datos actuales del cliente en el formulario
    fetch('/api/clientes/lista')
        .then(function (respuesta) {
            if (!respuesta.ok) { throw new Error('Error ' + respuesta.status); }
            return respuesta.json();
        })
        .then(function (clientes) {
            var cliente = clientes.find(function (c) { return String(c.id) === idCliente; });
            if (!cliente) {
                mostrarError(null, 'Este cliente ya no existe.');
                return;
            }
            nombre.value = cliente.nombre;
            edad.value = cliente.edad || '';
            telefono.value = cliente.telefono;
            botonGuardar.disabled = false;
        })
        .catch(function () {
            mostrarError(null, 'No se pudo conectar con el servidor.');
        });

    formulario.addEventListener('submit', function (evento) {
        evento.preventDefault();
        limpiarMensajes();
        botonGuardar.disabled = true;

        var datos = new URLSearchParams();
        datos.set('nombre', nombre.value);
        datos.set('edad', edad.value);
        datos.set('telefono', telefono.value);

        fetch('/api/clientes/modificar/' + encodeURIComponent(idCliente), {
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
                    // Regresa al perfil, que ya muestra los datos nuevos
                    window.location.href = perfil();
                } else {
                    botonGuardar.disabled = false;
                    mostrarError(resultado.cuerpo.campo,
                        resultado.cuerpo.error || 'No se pudieron guardar los cambios.');
                }
            })
            .catch(function () {
                botonGuardar.disabled = false;
                mostrarError(null, 'No se pudo conectar con el servidor.');
            });
    });
})();
