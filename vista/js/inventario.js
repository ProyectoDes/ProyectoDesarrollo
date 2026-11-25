(function () {
  var campoBuscar = document.getElementById('buscar');
  var formularioBuscar = document.getElementById('form-buscar');
  var total = document.getElementById('total-productos');
  var lista = document.getElementById('lista-productos');
  var todos = [];

  // Al regresar de editar un producto se confirma el cambio
  var aviso = document.getElementById('mensaje-inventario');
  if (aviso && new URLSearchParams(window.location.search).get('actualizado') === '1') {
    aviso.textContent = 'Producto actualizado correctamente.';
    aviso.hidden = false;
  }

  // Quita acentos y mayúsculas para que "serum" encuentre "Sérum"
  function normalizar(texto) {
    return String(texto || '')
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .toLowerCase()
      .trim();
  }

  function crearElemento(etiqueta, texto, clase) {
    var elemento = document.createElement(etiqueta);
    elemento.textContent = texto;
    if (clase) {
      elemento.className = clase;
    }
    return elemento;
  }

  function crearTarjeta(producto) {
    var item = document.createElement('li');
    var enlace = document.createElement('a');
    enlace.href = 'editar-producto.html?id=' + producto.id;

    var datos = crearElemento('span', '', 'producto');
    datos.appendChild(crearElemento('strong', producto.nombre));
    if (producto.descripcion) {
      datos.appendChild(crearElemento('span', producto.descripcion));
    }
    enlace.appendChild(datos);

    var existencias = crearElemento('span', '', 'existencias');
    existencias.appendChild(crearElemento('strong', String(producto.cantidad)));
    existencias.appendChild(crearElemento('span', producto.cantidad === 1 ? 'unidad' : 'unidades'));
    enlace.appendChild(existencias);

    item.appendChild(enlace);
    return item;
  }

  function mostrar() {
    var buscado = normalizar(campoBuscar.value);
    var visibles = todos.filter(function (p) {
      return !buscado || normalizar(p.nombre + ' ' + p.descripcion).indexOf(buscado) !== -1;
    });

    total.textContent = visibles.length + (visibles.length === 1 ? ' producto' : ' productos');
    lista.innerHTML = '';
    if (visibles.length === 0) {
      lista.appendChild(crearElemento('li', buscado
        ? 'No hay productos que coincidan con la búsqueda.'
        : 'Aún no hay productos en el inventario.', 'vacio'));
      return;
    }
    visibles.forEach(function (p) {
      lista.appendChild(crearTarjeta(p));
    });
  }

  fetch('/api/productos')
    .then(function (respuesta) {
      if (!respuesta.ok) {
        throw new Error('Error ' + respuesta.status);
      }
      return respuesta.json();
    })
    .then(function (productos) {
      todos = productos;
      mostrar();
    })
    .catch(function () {
      total.textContent = '';
      lista.innerHTML = '';
      lista.appendChild(crearElemento('li', 'No se pudo cargar el inventario.', 'vacio'));
    });

  campoBuscar.addEventListener('input', mostrar);
  formularioBuscar.addEventListener('submit', function (evento) {
    evento.preventDefault();
    mostrar();
  });
})();
