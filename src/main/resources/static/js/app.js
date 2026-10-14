document.querySelectorAll('.needs-validation').forEach(function (form) {
    form.addEventListener('submit', function (event) {
        if (!form.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
            const invalid = form.querySelector(':invalid');
            Array.from(form.elements).filter(el => el.willValidate).forEach(function (el) {
                el.setAttribute('aria-invalid', String(!el.validity.valid));
                if (!el.id) return;
                const id = el.id + '-client-error';
                let message = document.getElementById(id);
                if (!el.validity.valid) {
                    if (!message) {
                        message = document.createElement('p');
                        message.id = id;
                        message.className = 'text-danger small';
                        message.setAttribute('role', 'alert');
                        el.after(message);
                    }
                    message.textContent = el.validity.valueMissing ? 'Complete este campo obligatorio.' : 'Revise el formato y la longitud solicitados para este campo.';
                    const ids = new Set((el.getAttribute('aria-describedby') || '').split(' ').filter(Boolean));
                    ids.add(id);
                    el.setAttribute('aria-describedby', Array.from(ids).join(' '));
                } else if (message) {
                    message.remove();
                    el.setAttribute('aria-describedby', (el.getAttribute('aria-describedby') || '').split(' ').filter(x => x !== id).join(' '));
                }
            });
            if (invalid) invalid.focus();
        }
        form.classList.add('was-validated');
    });
});

const serverInvalid = document.querySelector('[aria-invalid="true"]');
if (serverInvalid) serverInvalid.focus();

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
