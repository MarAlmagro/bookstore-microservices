---
trigger: always_on
---

# 🤖 Agentic Architect Master Rules (v2026.1)

## 1. Operational Protocol: The "Silent Operator"
* **Minimalist Communication:** Do not generate narrative summaries, "Phase" updates, or walkthrough `.md` files. Communication must be "By Exception": only report errors, logic deviations, or technical debt.
* **Atomic Git Flow:** Group changes into logical, atomic commits only *after* all verification steps pass. Use the pattern: `type(scope): concise description`.
* **Session Continuity:** Maintain a `progress.md` file in the project root. Update it with a 1-line status after every task to preserve context across model switches or new sessions.

## 2. Token Economy & Context Management
* **Clean-as-you-go:** Remove redundant Javadoc/Docstrings that merely repeat method names. Focus on documenting the "Why" (business logic) only.
* **Ignore Junk:** Never read or index `target/`, `build/`, `node_modules/`, `.angular/`, or `dist/`. 
* **Ephemeral Planning:** Draft implementation plans in the chat. Do not create permanent `.md` files for temporary tasks.
* **Dependency Vetting:** Before adding any new library/package, justify its necessity and security footprint to the Architect. Prioritize lightweight, standard solutions.

## 3. Architecture & Contract-First (Source of Truth)
* **Contractual Integrity:** The `openapi.yaml` (or Swagger) is the supreme law. Validate/Update the contract *before* modifying any Back-end or Front-end code.
* **Strict Decoupling:** Every class (Spring/Python) and component (Angular) must follow the **Single Responsibility Principle**. Use interfaces to define boundaries.
* **Machine-Readable Errors:** Implement standardized error codes (e.g., `ERR_AUTH_001`) to ensure seamless Agentic integration between Front-end and Back-end.

## 4. Engineering Standards
* **Mobile-First & A11y:** All UI must be responsive and accessible (WCAG 2.1 AA, ARIA labels, keyboard navigation) using the **Project-Defined Stack**. Prioritize Flexbox/Grid for layouts.
* **Test-Forward Development (TDD):** Write or update the test suite *before* implementation.
* **Testing-Ready UI:** Every interactive element must include a `data-testid="[context]-[action]"` attribute.

## 5. Verification & Self-Healing
* **The DoD (Definition of Done):** A task is only "Done" when:
    1. The code passes the Linter.
    2. All Unit and Integration tests are green.
    3. A security scan shows no high-risk vulnerabilities.
* **Autonomous Correction:** If a test or build fails, analyze the logs, formulate a fix, and retry without human intervention. Only halt if the error is architectural or requires a "Secret" (use placeholders).