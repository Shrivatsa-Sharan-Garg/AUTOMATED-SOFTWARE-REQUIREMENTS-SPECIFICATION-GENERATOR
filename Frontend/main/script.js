document.addEventListener('DOMContentLoaded', () => {
        const loginTab = document.getElementById('login-tab');
        const signupTab = document.getElementById('signup-tab');
        const loginSection = document.getElementById('login_section');
        const signupSection = document.getElementById('signup_section');

        // 1. Switch between Login and Signup tabs
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

        // 2. Mock Redirection for Demo Purpose
        // In a real app, the Java Servlet handles this, 
        // but for your frontend demo, we can show the "flow"
        const authForms = document.querySelectorAll('form');
        authForms.forEach(form => {
            form.addEventListener('submit', (e) => {
                // Only prevent default if you want to stay on page for testing
                e.preventDefault(); 
                console.log("Form submitted! Redirecting to Dashboard...");
                location.href = './Frontend/dashboard/dashboard.html'; // Uncomment to test redirection without backend
            });
        });
    });