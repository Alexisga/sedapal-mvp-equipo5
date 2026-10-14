document.querySelectorAll('.needs-validation').forEach(function (form) {
    form.addEventListener('submit', function (event) {
        if (!form.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
            const invalid = form.querySelector(':invalid');
            if (invalid) invalid.focus();
        }
        form.classList.add('was-validated');
    });
});

const usuarioSelect = document.querySelector('[data-user-select]');
const suministroSelect = document.querySelector('[data-supply-select]');
if (usuarioSelect && suministroSelect) {
    const actualizarSuministros = function () {
        const usuarioId = usuarioSelect.value;
        Array.from(suministroSelect.options).forEach(function (option) {
            option.hidden = option.dataset.usuarioId && option.dataset.usuarioId !== usuarioId;
            if (option.hidden && option.selected) option.selected = false;
        });
        suministroSelect.disabled = !usuarioId;
    };
    usuarioSelect.addEventListener('change', actualizarSuministros);
    actualizarSuministros();
}
