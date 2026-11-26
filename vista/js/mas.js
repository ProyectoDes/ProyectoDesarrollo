(function () {
  // Cerrar sesión: se borra la sesión en el servidor y se regresa al inicio de sesión
  var cerrarSesion = document.getElementById('btn-cerrar-sesion');
  if (cerrarSesion) {
    cerrarSesion.addEventListener('click', function (evento) {
      evento.preventDefault();
      fetch('/api/sesion', { method: 'DELETE' })
        .catch(function () { /* aunque falle la red, se manda al inicio de sesión */ })
        .then(function () {
          window.location.replace('inicio-sesion.html');
        });
    });
  }

  var opciones = document.querySelectorAll('input[name="formato-hora"]');
  var actual = window.FormatoHora.leer();

  opciones.forEach(function (opcion) {
    opcion.checked = opcion.value === actual;
    opcion.addEventListener('change', function () {
      if (opcion.checked) {
        window.FormatoHora.guardar(opcion.value);
      }
    });
  });
})();
