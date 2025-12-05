// Profile Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('profileForm');
    const editBtn = document.getElementById('editBtn');
    const cancelBtn = document.getElementById('cancelBtn');
    const actionButtons = document.getElementById('actionButtons');
    const passwordSection = document.getElementById('passwordSection');
    const toggleCurrentPassword = document.getElementById('toggleCurrentPassword');
    const toggleNewPassword = document.getElementById('toggleNewPassword');
    const currentPasswordInput = document.getElementById('currentPassword');
    const newPasswordInput = document.getElementById('newPassword');
    const phoneInput = document.getElementById('phone');
    const phoneDisplay = document.getElementById('phoneDisplay');
    const passwordAlert = document.getElementById('passwordAlert');
    const saveBtn = document.getElementById('saveBtn');

    // Function to format phone number
    function formatPhone(phone) {
        if (!phone) return '';
        let digits = phone.replace(/\D/g, '');

        if (digits.length === 11) {
            return digits.replace(/^(\d{2})(\d{5})(\d{4})$/, '($1) $2-$3');
        } else if (digits.length === 10) {
            return digits.replace(/^(\d{2})(\d{4})(\d{4})$/, '($1) $2-$3');
        }
        return phone;
    }

    // Format phone display on page load
    if (phoneDisplay) {
        phoneDisplay.textContent = formatPhone(phoneDisplay.textContent);
    }

    // Format phone input on page load
    if (phoneInput.value) {
        phoneInput.value = formatPhone(phoneInput.value);
    }

    // Store original values (already formatted)
    let originalValues = {
        fullName: document.getElementById('fullName').value,
        phone: phoneInput.value
    };

    // Edit button click handler
    editBtn.addEventListener('click', function() {
        form.classList.remove('view-mode');
        form.classList.add('edit-mode');
        passwordSection.classList.remove('view-mode');
        passwordSection.classList.add('edit-mode');
        editBtn.disabled = true;
        cancelBtn.disabled = false;
        saveBtn.disabled = false;
        passwordAlert.style.display = 'block';
    });

    // Cancel button click handler
    cancelBtn.addEventListener('click', function() {
        // Restore original values
        document.getElementById('fullName').value = originalValues.fullName;
        phoneInput.value = originalValues.phone;
        currentPasswordInput.value = '';
        newPasswordInput.value = '';

        // Switch back to view mode
        form.classList.remove('edit-mode');
        form.classList.add('view-mode');
        passwordSection.classList.remove('edit-mode');
        passwordSection.classList.add('view-mode');
        editBtn.disabled = false;
        cancelBtn.disabled = true;
        saveBtn.disabled = true;
        passwordAlert.style.display = 'none';
    });

    // Toggle password visibility
    toggleCurrentPassword.addEventListener('click', function() {
        const type = currentPasswordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        currentPasswordInput.setAttribute('type', type);
        const icon = this.querySelector('i');
        icon.classList.toggle('bi-eye-fill');
        icon.classList.toggle('bi-eye-slash-fill');
    });

    toggleNewPassword.addEventListener('click', function() {
        const type = newPasswordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        newPasswordInput.setAttribute('type', type);
        const icon = this.querySelector('i');
        icon.classList.toggle('bi-eye-fill');
        icon.classList.toggle('bi-eye-slash-fill');
    });

    // Phone mask
    phoneInput.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');

        if (value.length > 11) {
            value = value.slice(0, 11);
        }

        if (value.length > 6) {
            value = value.replace(/^(\d{2})(\d{5})(\d{0,4}).*/, '($1) $2-$3');
        } else if (value.length > 2) {
            value = value.replace(/^(\d{2})(\d{0,5})/, '($1) $2');
        } else if (value.length > 0) {
            value = value.replace(/^(\d*)/, '($1');
        }

        e.target.value = value;
    });

    // Form validation
    form.addEventListener('submit', function(e) {
        const fullName = document.getElementById('fullName').value.trim();
        const phone = phoneInput.value.trim();
        const currentPassword = currentPasswordInput.value.trim();
        const newPassword = newPasswordInput.value.trim();

        // Validate full name
        if (!fullName) {
            e.preventDefault();
            alert('Nome completo é obrigatório');
            return false;
        }

        // Validate phone
        if (!phone) {
            e.preventDefault();
            alert('Telefone é obrigatório');
            return false;
        }

        // Validate phone format
        const phoneDigits = phone.replace(/\D/g, '');
        if (phoneDigits.length < 10) {
            e.preventDefault();
            alert('Telefone inválido. Digite um telefone válido com DDD.');
            return false;
        }

        // Validate password change
        if (newPassword) {
            if (!currentPassword) {
                e.preventDefault();
                alert('Para alterar a senha, informe a senha atual');
                return false;
            }
            if (newPassword.length < 8) {
                e.preventDefault();
                alert('A nova senha deve ter no mínimo 8 caracteres');
                return false;
            }
        }
    });

    // Auto-dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert-profile');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });
});
