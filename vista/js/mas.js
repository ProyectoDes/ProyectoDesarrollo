(function () {
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
