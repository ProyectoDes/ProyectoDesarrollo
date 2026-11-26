// Listas de hora en lapsos de media hora y hora de fin automática.
(function () {
    var HORA_APERTURA = 8;   // primera hora en que se puede iniciar una cita
    var HORA_CIERRE = 21;    // última hora disponible (solo como hora de fin)
    var PASO = 30;           // minutos entre una opción y la siguiente
    var DURACION = 60;       // duración que se propone al elegir la hora de inicio

    var inicio = document.getElementById('hora-inicio');
    var fin = document.getElementById('hora-fin');

    function dos(numero) {
        return numero < 10 ? '0' + numero : String(numero);
    }

    function texto(minutos) {
        return dos(Math.floor(minutos / 60)) + ':' + dos(minutos % 60);
    }

    function llenar(select, desde, hasta) {
        var seleccionada = select.value;
        select.innerHTML = '';

        var vacia = document.createElement('option');
        vacia.value = '';
        vacia.textContent = 'Selecciona una hora';
        select.appendChild(vacia);

        for (var minutos = desde; minutos <= hasta; minutos += PASO) {
            var valor = texto(minutos);
            var opcion = document.createElement('option');
            opcion.value = valor;
            opcion.textContent = window.FormatoHora ? window.FormatoHora.hora(valor) : valor;
            select.appendChild(opcion);
        }
        select.value = seleccionada;
    }

    // Al elegir la hora de inicio, la de fin se acomoda una hora después
    function proponerFin() {
        if (!inicio.value) {
            fin.value = '';
            return;
        }
        var partes = inicio.value.split(':');
        var minutosInicio = Number(partes[0]) * 60 + Number(partes[1]);
        var minutosFin = Math.min(minutosInicio + DURACION, HORA_CIERRE * 60);
        fin.value = texto(minutosFin);
    }

    if (inicio) {
        llenar(inicio, HORA_APERTURA * 60, HORA_CIERRE * 60 - PASO);
    }
    if (fin) {
        llenar(fin, HORA_APERTURA * 60 + PASO, HORA_CIERRE * 60);
    }
    if (inicio && fin) {
        inicio.addEventListener('change', proponerFin);
    }
})();