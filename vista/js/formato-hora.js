// Formato de hora que eligió Celia en Más (12 o 24 horas).
// Se guarda en este dispositivo; si no ha elegido, se usa 12 horas.
(function () {
  var CLAVE = 'byb-formato-hora';

  function leer() {
    try {
      return window.localStorage.getItem(CLAVE) === '24' ? '24' : '12';
    } catch (e) {
      return '12';
    }
  }

  function guardar(formato) {
    try {
      window.localStorage.setItem(CLAVE, formato === '24' ? '24' : '12');
    } catch (e) {
      // Si el navegador no deja guardar, simplemente se queda en 12 horas
    }
  }

  // "14:30" -> "2:30 p.m." (12 h) o "14:30" (24 h)
  function hora(texto) {
    var partes = String(texto || '').split(':');
    if (partes.length < 2 || leer() === '24') {
      return texto || '';
    }
    var horas = Number(partes[0]);
    var sufijo = horas < 12 ? 'a.m.' : 'p.m.';
    var horas12 = horas % 12 === 0 ? 12 : horas % 12;
    return horas12 + ':' + partes[1] + ' ' + sufijo;
  }

  function rango(inicio, fin) {
    return hora(inicio) + ' – ' + hora(fin);
  }

  window.FormatoHora = { leer: leer, guardar: guardar, hora: hora, rango: rango };
})();
