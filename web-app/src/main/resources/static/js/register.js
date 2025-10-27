/**
 * PetSafe - Register Page JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('registerForm');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const togglePasswordBtn = document.getElementById('togglePassword');
    const toggleConfirmPasswordBtn = document.getElementById('toggleConfirmPassword');
    const phoneInput = document.getElementById('phone');
    const passwordStrengthDiv = document.getElementById('passwordStrength');

    // Toggle password visibility
    togglePasswordBtn.addEventListener('click', function() {
        const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordInput.setAttribute('type', type);
        const icon = this.querySelector('i');
        icon.classList.toggle('bi-eye-fill');
        icon.classList.toggle('bi-eye-slash-fill');
    });

    toggleConfirmPasswordBtn.addEventListener('click', function() {
        const type = confirmPasswordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        confirmPasswordInput.setAttribute('type', type);
        const icon = this.querySelector('i');
        icon.classList.toggle('bi-eye-fill');
        icon.classList.toggle('bi-eye-slash-fill');
    });

    // Phone mask
    phoneInput.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');

        if (value.length > 11) {
            value = value.substring(0, 11);
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

    // Password strength indicator
    passwordInput.addEventListener('input', function() {
        const password = this.value;
        const strength = calculatePasswordStrength(password);

        let strengthText = '';
        let strengthClass = '';

        if (password.length === 0) {
            passwordStrengthDiv.innerHTML = '';
            return;
        }

        if (strength < 2) {
            strengthText = 'Fraca';
            strengthClass = 'weak';
        } else if (strength < 3) {
            strengthText = 'Média';
            strengthClass = 'medium';
        } else if (strength < 4) {
            strengthText = 'Boa';
            strengthClass = 'good';
        } else {
            strengthText = 'Forte';
            strengthClass = 'strong';
        }

        passwordStrengthDiv.innerHTML = `
            <div class="password-strength-bar ${strengthClass}">
                <div class="password-strength-fill"></div>
            </div>
            <small class="text-muted">Força da senha: <span class="${strengthClass}">${strengthText}</span></small>
        `;
    });

    // Password strength calculation
    function calculatePasswordStrength(password) {
        let strength = 0;

        if (password.length >= 8) strength++;
        if (password.length >= 12) strength++;
        if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++;
        if (/\d/.test(password)) strength++;
        if (/[^a-zA-Z0-9]/.test(password)) strength++;

        return strength;
    }

    // Confirm password validation
    confirmPasswordInput.addEventListener('input', function() {
        if (this.value !== passwordInput.value) {
            this.setCustomValidity('As senhas não coincidem');
            document.getElementById('confirmPasswordFeedback').textContent = 'As senhas não coincidem.';
        } else {
            this.setCustomValidity('');
        }
    });

    passwordInput.addEventListener('input', function() {
        if (confirmPasswordInput.value && confirmPasswordInput.value !== this.value) {
            confirmPasswordInput.setCustomValidity('As senhas não coincidem');
        } else if (confirmPasswordInput.value === this.value) {
            confirmPasswordInput.setCustomValidity('');
        }
    });

    // Form validation
    registerForm.addEventListener('submit', function(event) {
        if (!registerForm.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
        } else {
            // Additional validation
            if (passwordInput.value !== confirmPasswordInput.value) {
                event.preventDefault();
                confirmPasswordInput.setCustomValidity('As senhas não coincidem');
            }
        }

        registerForm.classList.add('was-validated');
    });

    // Real-time email validation
    const emailInput = document.getElementById('email');
    emailInput.addEventListener('blur', function() {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (this.value && !emailRegex.test(this.value)) {
            this.setCustomValidity('Por favor, informe um e-mail válido');
        } else {
            this.setCustomValidity('');
        }
    });

    // Name validation (at least 2 words)
    const fullNameInput = document.getElementById('fullName');
    fullNameInput.addEventListener('blur', function() {
        const words = this.value.trim().split(/\s+/);
        if (words.length < 2) {
            this.setCustomValidity('Por favor, informe seu nome completo');
            this.classList.add('is-invalid');
        } else {
            this.setCustomValidity('');
            this.classList.remove('is-invalid');
        }
    });
});
