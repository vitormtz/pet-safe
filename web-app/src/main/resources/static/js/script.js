/**
 * PetSafe - Custom JavaScript
 * Funcionalidades interativas para a landing page
 */

document.addEventListener('DOMContentLoaded', function() {
    // ===================================================
    // Smooth Scroll para links de navegação
    // ===================================================
    const navLinks = document.querySelectorAll('a[href^="#"]');

    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            const targetId = this.getAttribute('href');

            // Ignora links vazios
            if (targetId === '#') {
                e.preventDefault();
                return;
            }

            const targetElement = document.querySelector(targetId);

            if (targetElement) {
                e.preventDefault();

                // Fecha o menu mobile se estiver aberto
                const navbarCollapse = document.querySelector('.navbar-collapse');
                if (navbarCollapse.classList.contains('show')) {
                    const bsCollapse = new bootstrap.Collapse(navbarCollapse);
                    bsCollapse.hide();
                }

                // Scroll suave até o elemento
                const navbarHeight = document.querySelector('.navbar').offsetHeight;
                const targetPosition = targetElement.offsetTop - navbarHeight;

                window.scrollTo({
                    top: targetPosition,
                    behavior: 'smooth'
                });
            }
        });
    });

    // ===================================================
    // Navbar Scroll Effect
    // ===================================================
    const navbar = document.querySelector('.navbar');

    window.addEventListener('scroll', function() {
        if (window.scrollY > 50) {
            navbar.classList.add('shadow');
        } else {
            navbar.classList.remove('shadow');
        }
    });

    // ===================================================
    // Active Navigation Link
    // ===================================================
    const sections = document.querySelectorAll('section[id]');

    window.addEventListener('scroll', function() {
        const scrollPosition = window.scrollY + 100;

        sections.forEach(section => {
            const sectionTop = section.offsetTop;
            const sectionHeight = section.offsetHeight;
            const sectionId = section.getAttribute('id');

            if (scrollPosition >= sectionTop && scrollPosition < sectionTop + sectionHeight) {
                // Remove active de todos os links
                document.querySelectorAll('.nav-link').forEach(link => {
                    link.classList.remove('active');
                });

                // Adiciona active ao link correspondente
                const activeLink = document.querySelector(`.nav-link[href="#${sectionId}"]`);
                if (activeLink) {
                    activeLink.classList.add('active');
                }
            }
        });
    });

    // ===================================================
    // Formulário de Contato
    // ===================================================
    const contactForm = document.getElementById('contactForm');

    if (contactForm) {
        contactForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const submitButton = this.querySelector('button[type="submit"]');
            const originalText = submitButton.innerHTML;

            // Mostra loading
            submitButton.classList.add('loading');
            submitButton.disabled = true;

            // Simula envio (aqui você conectaria com sua API)
            setTimeout(() => {
                // Remove loading
                submitButton.classList.remove('loading');
                submitButton.disabled = false;

                // Mostra mensagem de sucesso
                showToast('Mensagem enviada com sucesso! Entraremos em contato em breve.', 'success');

                // Limpa o formulário
                contactForm.reset();
            }, 2000);
        });
    }

    // ===================================================
    // Animação de entrada dos elementos
    // ===================================================
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-on-scroll');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);

    // Observa cards de features
    document.querySelectorAll('.feature-card').forEach(card => {
        observer.observe(card);
    });

    // Observa cards de pricing
    document.querySelectorAll('.pricing-card').forEach(card => {
        observer.observe(card);
    });

    // ===================================================
    // Toast Notification
    // ===================================================
    function showToast(message, type = 'info') {
        // Remove toast anterior se existir
        const existingToast = document.querySelector('.custom-toast');
        if (existingToast) {
            existingToast.remove();
        }

        // Cria o toast
        const toast = document.createElement('div');
        toast.className = `custom-toast toast-${type}`;
        toast.innerHTML = `
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-check-circle-fill"></i>
                <span>${message}</span>
            </div>
        `;

        // Adiciona estilos inline
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${type === 'success' ? '#198754' : '#0d6efd'};
            color: white;
            padding: 1rem 1.5rem;
            border-radius: 10px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            z-index: 9999;
            animation: slideInRight 0.3s ease;
            max-width: 400px;
        `;

        document.body.appendChild(toast);

        // Remove após 5 segundos
        setTimeout(() => {
            toast.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 5000);
    }

    // Adiciona animações CSS para o toast
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideInRight {
            from {
                transform: translateX(400px);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }

        @keyframes slideOutRight {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(400px);
                opacity: 0;
            }
        }

        .custom-toast:hover {
            cursor: pointer;
            transform: scale(1.02);
            transition: transform 0.2s ease;
        }
    `;
    document.head.appendChild(style);

    // ===================================================
    // Newsletter Form
    // ===================================================
    const newsletterForms = document.querySelectorAll('footer form');

    newsletterForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();

            const emailInput = this.querySelector('input[type="email"]');
            const email = emailInput.value;

            if (email) {
                // Simula envio (conectar com API)
                showToast('Inscrição realizada com sucesso!', 'success');
                emailInput.value = '';
            }
        });
    });

    // ===================================================
    // Adiciona efeito hover nos botões de CTA
    // ===================================================
    const ctaButtons = document.querySelectorAll('.btn-primary, .btn-outline-primary');

    ctaButtons.forEach(button => {
        button.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px)';
        });

        button.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });

    // ===================================================
    // Mascara de telefone (formatação brasileira)
    // ===================================================
    const phoneInputs = document.querySelectorAll('input[type="tel"]');

    phoneInputs.forEach(input => {
        input.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');

            if (value.length <= 11) {
                value = value.replace(/^(\d{2})(\d)/g, '($1) $2');
                value = value.replace(/(\d)(\d{4})$/, '$1-$2');
            }

            e.target.value = value;
        });
    });

    // ===================================================
    // Console Welcome Message
    // ===================================================
    console.log('%c🐾 PetSafe Web Application', 'color: #0d6efd; font-size: 20px; font-weight: bold;');
    console.log('%cVersão 1.0 - Sistema de Monitoramento de Pets', 'color: #6c757d; font-size: 12px;');
});
