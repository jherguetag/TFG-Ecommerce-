function finalizarCompra() {
    Swal.fire({
        title: '¡Pago procesado con éxito!',
        text: 'Tu pedido está en camino. Hemos enviado los detalles a tu correo.',
        icon: 'success',
        confirmButtonText: 'Volver a la tienda',
        confirmButtonColor: '#10ac84',
        backdrop: `rgba(0,0,123,0.4)`
    }).then((result) => {
        if (result.isConfirmed) {
            window.location.href = "/"; 
        }
    });
}