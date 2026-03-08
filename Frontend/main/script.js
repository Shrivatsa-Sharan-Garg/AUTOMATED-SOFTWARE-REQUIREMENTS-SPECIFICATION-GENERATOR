document.addEventListener('DOMContentLoaded', () => {
        const loginTab = document.getElementById('login-tab');
        const signupTab = document.getElementById('signup-tab');
        const loginSection = document.getElementById('login_section');
        const signupSection = document.getElementById('signup_section');

        const switchTab = (activeTab, inactiveTab, showSection, hideSection) => {
            activeTab.classList.add('active');
            inactiveTab.classList.remove('active');
            showSection.classList.add('active');
            hideSection.classList.remove('active');
        };

        loginTab.addEventListener('click', () => {
            switchTab(loginTab, signupTab, loginSection, signupSection);
        });

        signupTab.addEventListener('click', () => {
            switchTab(signupTab, loginTab, signupSection, loginSection);
        });

        const authForms = document.querySelectorAll('form');
        authForms.forEach(form => {
            form.addEventListener('submit', (e) => {
                e.preventDefault(); 
                console.log("Form submitted! Redirecting to Dashboard...");
                location.href = './Frontend/dashboard/dashboard.html'; 
            });
        });
    });