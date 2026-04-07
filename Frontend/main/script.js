document.addEventListener("DOMContentLoaded", () => {
  const loginTab = document.getElementById("login-tab");
  const signupTab = document.getElementById("signup-tab");
  const loginSection = document.getElementById("login_section");
  const signupSection = document.getElementById("signup_section");

  const switchTab = (activeTab, inactiveTab, showSection, hideSection) => {
    activeTab.classList.add("active");
    inactiveTab.classList.remove("active");
    showSection.classList.add("active");
    hideSection.classList.remove("active");
  };

  loginTab.addEventListener("click", () =>
    switchTab(loginTab, signupTab, loginSection, signupSection),
  );
  signupTab.addEventListener("click", () =>
    switchTab(signupTab, loginTab, signupSection, loginSection),
  );

  const handleAuth = async (e, type) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const data = Object.fromEntries(formData.entries());

    try {
      const response = await fetch(`http://localhost:8080/api/${type}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      const result = await response.json();

      if (response.ok) {
        localStorage.setItem("user", JSON.stringify(result.user));
        alert(result.message || "Success!");
        location.href = "./Frontend/dashboard/dashboard.html";
      } else {
        alert(result.error || "Authentication failed.");
        if (result.code === "USER_NOT_FOUND") {
          switchTab(signupTab, loginTab, signupSection, loginSection);
        }
      }
    } catch (err) {
      alert("Cannot connect to Backend Server. Is it running?");
    }
  };

  document
    .getElementById("login-form")
    .addEventListener("submit", (e) => handleAuth(e, "login"));
  document
    .getElementById("signup-form")
    .addEventListener("submit", (e) => handleAuth(e, "signup"));
});
