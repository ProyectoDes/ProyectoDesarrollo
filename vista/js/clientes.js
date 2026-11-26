(function () {
  var campoBuscar = document.getElementById('buscar');
  var formularioBuscar = document.getElementById('form-buscar');
  var total = document.getElementById('total-clientes');
  var lista = document.getElementById('lista-clientes');
  var todosLosClientes = [];

  // Quita acentos y mayusculas para que "ramon" encuentre a "Ramón"
  function normalizar(texto) {
    return String(texto || '')
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .toLowerCase()
      .trim();
  }

  function iniciales(nombre) {
    var partes = String(nombre || '').trim().split(/\s+/).filter(Boolean);
    var letras = partes.length > 1
      ? partes[0].charAt(0) + partes[partes.length - 1].charAt(0)
      : (partes[0] || '?').charAt(0);
    return letras.toUpperCase();
  }

  // "5512345678" se muestra como "55 1234 5678"
  function formatearTelefono(telefono) {
    var digitos = String(telefono || '').replace(/\D/g, '');
    if (digitos.length === 10) {
      return digitos.slice(0, 2) + ' ' + digitos.slice(2, 6) + ' ' + digitos.slice(6);
    }
    return telefono || '';
  }

  function crearElemento(etiqueta, texto) {
    var elemento = document.createElement(etiqueta);
    elemento.textContent = texto;
    return elemento;
  }

  function crearTarjeta(cliente) {
    var item = document.createElement('li');
    var enlace = document.createElement('a');
    enlace.href = 'perfil-cliente.html?id=' + cliente.id;

    var avatar = crearElemento('span', iniciales(cliente.nombre));
    avatar.className = 'avatar';
    avatar.setAttribute('aria-hidden', 'true');
    enlace.appendChild(avatar);

    enlace.appendChild(crearElemento('strong', cliente.nombre));

    var telefono = crearElemento('span', formatearTelefono(cliente.telefono));
    telefono.className = 'telefono';
    enlace.appendChild(telefono);

    item.appendChild(enlace);
    return item;
  }

  function mostrarClientes() {
    var buscado = normalizar(campoBuscar.value);
    var soloDigitos = buscado.replace(/\D/g, '');
    var visibles = todosLosClientes.filter(function (cliente) {
      if (!buscado) {
        return true;
      }
      var coincideNombre = normalizar(cliente.nombre).indexOf(buscado) !== -1;
      var coincideTelefono = soloDigitos !== ''
        && String(cliente.telefono || '').replace(/\D/g, '').indexOf(soloDigitos) !== -1;
      return coincideNombre || coincideTelefono;
    });

    total.textContent = visibles.length + (visibles.length === 1 ? ' cliente' : ' clientes');
    lista.innerHTML = '';

    if (visibles.length === 0) {
      var vacio = crearElemento('li', buscado
        ? 'No hay clientes que coincidan con la búsqueda.'
        : 'Aún no hay clientes registrados.');
      vacio.className = 'vacio';
      lista.appendChild(vacio);
      return;
    }
    visibles.forEach(function (cliente) {
      lista.appendChild(crearTarjeta(cliente));
    });
  }

  function cargarClientes() {
    total.textContent = 'Cargando clientes...';
    fetch('/api/clientes/lista')
      .then(function (respuesta) {
        if (!respuesta.ok) {
          throw new Error('Error ' + respuesta.status);
        }
        return respuesta.json();
      })
      .then(function (clientes) {
        todosLosClientes = clientes;
        mostrarClientes();
      })
      .catch(function () {
        total.textContent = 'No se pudieron cargar los clientes.';
        lista.innerHTML = '';
      });
  }

  campoBuscar.addEventListener('input', mostrarClientes);
  formularioBuscar.addEventListener('submit', function (evento) {
    evento.preventDefault();
    mostrarClientes();
  });

  cargarClientes();
})();
