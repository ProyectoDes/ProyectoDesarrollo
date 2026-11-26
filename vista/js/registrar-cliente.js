(function () {
    var formulario = document.getElementById('form-registrar-cliente');
    var mensaje = document.getElementById('mensaje-formulario');
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
        var texto = document.getElementById('error-' + campo);
        if (texto) {
            texto.textContent = textoError;
            texto.hidden = false;
        } else {
            mensaje.className = 'error';
            mensaje.textContent = textoError;
            mensaje.hidden = false;
        }
    }

    function mostrarExito(textoExito) {
        mensaje.className = '';
        mensaje.textContent = textoExito;
        mensaje.hidden = false;
    }

    formulario.addEventListener('submit', function (evento) {
        evento.preventDefault();
        limpiarMensajes();

        var datos = new URLSearchParams();
        datos.set('nombre', document.getElementById('nombre').value);
        datos.set('edad', document.getElementById('edad').value);
        datos.set('telefono', document.getElementById('telefono').value);

        fetch('/api/clientes/registro', {
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
                    mostrarExito(resultado.cuerpo.mensaje || 'Cliente registrado correctamente.');
                    formulario.reset();
                    document.getElementById('nombre').focus();
                } else {
                    mostrarError(resultado.cuerpo.campo,
                        resultado.cuerpo.error || 'No se pudo guardar el cliente.');
                }
            })
            .catch(function () {
                mostrarError(null, 'No se pudo conectar con el servidor.');
            });
    });
})();