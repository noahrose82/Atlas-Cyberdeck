(() => {
  const config = window.ATLAS_CONFIG || {};
  const form = document.querySelector("#launch-form");
  const email = document.querySelector("#email");
  const status = document.querySelector("#form-status");
  const kickstarter = document.querySelector("#kickstarter-link");

  // Reveal animations
  const revealItems = document.querySelectorAll(".reveal");
  const prefersReducedMotion =
    window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  if (prefersReducedMotion) {
    revealItems.forEach((el) => el.classList.add("is-visible"));
  } else {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("is-visible");
            observer.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.12 }
    );

    revealItems.forEach((el) => observer.observe(el));
  }

  // Enable Kickstarter button later when the real URL exists.
  if (config.kickstarterUrl) {
    kickstarter.href = config.kickstarterUrl;
    kickstarter.textContent = "Notify Me on Kickstarter";
    kickstarter.classList.remove("is-disabled");
    kickstarter.removeAttribute("aria-disabled");
  }

  // Launch-list signup
  form?.addEventListener("submit", async (event) => {
    event.preventDefault();

    status.textContent = "";
    status.className = "form-status";

    if (!email.checkValidity()) {
      status.textContent = "Enter a valid email address.";
      status.classList.add("error");
      email.focus();
      return;
    }

    if (!config.signupEndpoint) {
      status.textContent = "Launch-list signup is not configured yet.";
      status.classList.add("error");
      return;
    }

    const button = form.querySelector("button[type='submit']");
    const originalText = button.textContent;

    button.disabled = true;
    button.textContent = "Joining…";

    const payload = {
      email: email.value.trim(),
      source: "atlas-landing-page"
    };

    try {
      await fetch(config.signupEndpoint, {
        method: "POST",
        mode: "no-cors",
        headers: {
          "Content-Type": "text/plain;charset=utf-8"
        },
        body: JSON.stringify(payload)
      });

      status.textContent =
        config.successMessage || "You're on the Atlas launch list.";

      status.classList.add("success");
      form.reset();
    } catch (error) {
      console.error("Atlas launch-list signup failed:", error);

      status.textContent =
        "Signup could not be completed. Please try again.";

      status.classList.add("error");
    } finally {
      button.disabled = false;
      button.textContent = originalText;
    }
  });
})();