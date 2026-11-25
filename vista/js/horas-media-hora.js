// Llena las listas de hora con lapsos de media hora.
(function () {
    var HORA_APERTURA = 8;   // primera hora disponible
    var HORA_CIERRE = 21;    // última hora disponible (se incluye como hora de fin)

    function dos(numero) {
        return numero < 10 ? '0' + numero : String(numero);
    }

    function llenar(select) {
        if (!select) { return; }
        var seleccionada = select.value;
        select.innerHTML = '';

        var vacia = document.createElement('option');
        vacia.value = '';
        vacia.textContent = 'Selecciona una hora';
        select.appendChild(vacia);

        for (var minutos = HORA_APERTURA * 60; minutos <= HORA_CIERRE * 60; minutos += 30) {
            var valor = dos(Math.floor(minutos / 60)) + ':' + dos(minutos % 60);
            var opcion = document.createElement('option');
            opcion.value = valor;
            opcion.textContent = window.FormatoHora ? window.FormatoHora.hora(valor) : valor;
            select.appendChild(opcion);
        }
        select.value = seleccionada;
    }

    llenar(document.getElementById('hora-inicio'));
    llenar(document.getElementById('hora-fin'));
})();